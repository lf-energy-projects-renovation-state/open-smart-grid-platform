// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.throttling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opensmartgridplatform.throttling.repositories.PermitRepository;
import org.opensmartgridplatform.throttling.repositories.PermitRepository.PermitCountByNetworkSegment;
import org.opensmartgridplatform.throttling.service.PermitReleasedNotifier;

@ExtendWith(MockitoExtension.class)
class PermitsPerNetworkSegmentTest {
  private static final int MAX_WAIT_FOR_HIGH_PRIO = 1000;
  @Mock private PermitRepository permitRepository;
  @Mock private PermitReleasedNotifier permitReleasedNotifier;
  private PermitsPerNetworkSegment permitsPerNetworkSegment;

  @BeforeEach
  void setUp() {
    this.permitsPerNetworkSegment =
        new PermitsPerNetworkSegment(
            this.permitRepository, this.permitReleasedNotifier, this.MAX_WAIT_FOR_HIGH_PRIO);
  }

  @Test
  void testInitializeEmpty() {
    final short throttlingConfigId = Short.parseShort("1");

    when(this.permitRepository.permitsByNetworkSegment(throttlingConfigId))
        .thenReturn(Lists.emptyList());

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment()).isEmpty();
  }

  @Test
  void testInitialize() {
    final int btsId = 1;
    final int cellId = 2;
    final int numberOfPermits = 3;

    final short throttlingConfigId = Short.parseShort("1");

    final PermitCountByNetworkSegment permitCountByNetworkSegment =
        this.newPermitCountByNetworkSegment(btsId, cellId, numberOfPermits);
    when(this.permitRepository.permitsByNetworkSegment(throttlingConfigId))
        .thenReturn(List.of(permitCountByNetworkSegment));

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment()).hasSize(1);
    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment().get(btsId).get(cellId))
        .isEqualTo(numberOfPermits);
  }

  @Test
  void testInitializeUpdate() {
    final int btsId = 1;
    final int cellId = 2;
    final int numberOfPermits = 3;
    final short throttlingConfigId = Integer.valueOf(1).shortValue();

    this.preparePermits(btsId, cellId, numberOfPermits, throttlingConfigId);

    // Update number of permits in database
    final int newNumberOfPermits = 4;

    final PermitCountByNetworkSegment permitCountByNetworkSegmentUpdate =
        this.newPermitCountByNetworkSegment(btsId, cellId, newNumberOfPermits);
    when(this.permitRepository.permitsByNetworkSegment(throttlingConfigId))
        .thenReturn(List.of(permitCountByNetworkSegmentUpdate));

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment()).hasSize(1);
    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment().get(btsId).get(cellId))
        .isEqualTo(newNumberOfPermits);
  }

  @Test
  void testInitializeAdd() {
    final int btsId = 1;
    final int cellId = 2;
    final int numberOfPermits = 3;
    final short throttlingConfigId = Integer.valueOf(1).shortValue();

    this.preparePermits(btsId, cellId, numberOfPermits, throttlingConfigId);

    // Update number of permits in database
    final int newBtsId = 4;

    final PermitCountByNetworkSegment permitCountByNetworkSegmentUpdate =
        this.newPermitCountByNetworkSegment(newBtsId, cellId, numberOfPermits);
    when(this.permitRepository.permitsByNetworkSegment(throttlingConfigId))
        .thenReturn(List.of(permitCountByNetworkSegmentUpdate));

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment()).hasSize(1);
    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment().containsKey(btsId))
        .isFalse();
    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment().get(newBtsId).get(cellId))
        .isEqualTo(numberOfPermits);
  }

  @Test
  void testInitializeDelete() {
    final int btsId = 1;
    final int cellId = 2;
    final int numberOfPermits = 3;
    final short throttlingConfigId = Integer.valueOf(1).shortValue();

    this.preparePermits(btsId, cellId, numberOfPermits, throttlingConfigId);

    // No permits in database
    when(this.permitRepository.permitsByNetworkSegment(throttlingConfigId))
        .thenReturn(Lists.emptyList());

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 2000})
  void testHighPrioPoolTime(final int maxWaitForHighPrio) {
    this.permitsPerNetworkSegment =
        new PermitsPerNetworkSegment(
            this.permitRepository, this.permitReleasedNotifier, maxWaitForHighPrio);

    final int btsId = 1;
    final int cellId = 2;
    final int numberOfPermits = 3;
    final short throttlingConfigId = Integer.valueOf(1).shortValue();
    final int clientId = 4;
    final int requestId = 5;
    final int priority = 6;
    final int maxConcurrency = numberOfPermits;

    this.preparePermits(btsId, cellId, numberOfPermits, throttlingConfigId);

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    final long start = System.currentTimeMillis();
    final boolean permitGranted =
        this.permitsPerNetworkSegment.requestPermit(
            throttlingConfigId, clientId, btsId, cellId, requestId, priority, maxConcurrency);
    assertThat(permitGranted).isFalse();
    assertThat(System.currentTimeMillis() - start).isGreaterThanOrEqualTo(maxWaitForHighPrio);

    verify(this.permitRepository, never())
        .grantPermit(throttlingConfigId, clientId, btsId, cellId, requestId);
  }

  @Test
  void testHighPrioPool() {
    final int maxWaitForHighPrio = 10000;
    final int waitBeforeRelease = 1000;
    this.permitsPerNetworkSegment =
        new PermitsPerNetworkSegment(
            this.permitRepository, this.permitReleasedNotifier, maxWaitForHighPrio);

    final int btsId = 1;
    final int cellId = 2;
    final int otherCellId = cellId + 1;
    final int numberOfPermits = 3;
    final short throttlingConfigId = Integer.valueOf(1).shortValue();
    final int clientId = 4;
    final int requestId = 5;
    final int priority = 6;
    final int maxConcurrency = numberOfPermits;

    this.preparePermits(btsId, cellId, numberOfPermits, throttlingConfigId);

    when(this.permitRepository.grantPermit(throttlingConfigId, clientId, btsId, cellId, requestId))
        .thenReturn(true);
    when(this.permitRepository.grantPermit(
            throttlingConfigId, clientId, btsId, otherCellId, requestId))
        .thenReturn(true);

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    final long start = System.currentTimeMillis();

    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);
    executor.schedule(
        () -> {
          PermitsPerNetworkSegmentTest.this.permitsPerNetworkSegment.releasePermit(
              throttlingConfigId, clientId, btsId, cellId, requestId);
          when(this.permitReleasedNotifier.waitForAvailablePermit(btsId, cellId, 1000))
              .thenReturn(false)
              .thenReturn(true);
          verify(this.permitReleasedNotifier, times(1)).notifyPermitReleased(btsId, cellId);
        },
        waitBeforeRelease,
        TimeUnit.MILLISECONDS);

    final boolean permitGrantedOtherCell =
        this.permitsPerNetworkSegment.requestPermit(
            throttlingConfigId, clientId, btsId, otherCellId, requestId, priority, maxConcurrency);
    assertThat(permitGrantedOtherCell).isTrue();
    assertThat((int) (System.currentTimeMillis() - start)).isBetween(0, waitBeforeRelease);

    final boolean permitGranted =
        this.permitsPerNetworkSegment.requestPermit(
            throttlingConfigId, clientId, btsId, cellId, requestId, priority, maxConcurrency);
    assertThat(permitGranted).isTrue();
    assertThat((int) (System.currentTimeMillis() - start))
        .isBetween(waitBeforeRelease, maxWaitForHighPrio);
  }

  private void preparePermits(
      final int btsId,
      final int cellId,
      final int numberOfPermits,
      final short throttlingConfigId) {
    final PermitCountByNetworkSegment permitCountByNetworkSegment =
        this.newPermitCountByNetworkSegment(btsId, cellId, numberOfPermits);
    when(this.permitRepository.permitsByNetworkSegment(throttlingConfigId))
        .thenReturn(List.of(permitCountByNetworkSegment));

    this.permitsPerNetworkSegment.initialize(throttlingConfigId);

    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment()).hasSize(1);
    assertThat(this.permitsPerNetworkSegment.permitsPerNetworkSegment().get(btsId).get(cellId))
        .isEqualTo(numberOfPermits);
  }

  private PermitCountByNetworkSegment newPermitCountByNetworkSegment(
      final int btsId, final int cellId, final int numberOfPermits) {
    final PermitCountByNetworkSegment permitCountByNetworkSegment =
        mock(PermitCountByNetworkSegment.class);
    when(permitCountByNetworkSegment.getBaseTransceiverStationId()).thenReturn(btsId);
    when(permitCountByNetworkSegment.getCellId()).thenReturn(cellId);
    when(permitCountByNetworkSegment.getNumberOfPermits()).thenReturn(numberOfPermits);
    return permitCountByNetworkSegment;
  }
}
