// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.application.threads;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.SecurityKeyType.E_METER_AUTHENTICATION;
import static org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.SecurityKeyType.E_METER_ENCRYPTION;

import java.io.IOException;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmuc.jdlms.DlmsConnection;
import org.opensmartgridplatform.adapter.protocol.dlms.application.config.ThrottlingClientConfig;
import org.opensmartgridplatform.adapter.protocol.dlms.application.services.DeviceKeyProcessingService;
import org.opensmartgridplatform.adapter.protocol.dlms.application.services.DomainHelperService;
import org.opensmartgridplatform.adapter.protocol.dlms.application.services.SecretManagementService;
import org.opensmartgridplatform.adapter.protocol.dlms.application.throttling.LocalThrottlingServiceImpl;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.entities.DlmsDevice;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.factories.Hls5Connector;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.repositories.DlmsDeviceRepository;
import org.opensmartgridplatform.adapter.protocol.dlms.exceptions.RecoverKeyException;
import org.opensmartgridplatform.shared.exceptionhandling.ComponentType;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalException;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalExceptionType;
import org.opensmartgridplatform.shared.exceptionhandling.OsgpException;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.opensmartgridplatform.throttling.api.Permit;

@ExtendWith(MockitoExtension.class)
class RecoverKeyProcessTest {

  private static final String DEVICE_IDENTIFICATION = "E000123456789";

  @InjectMocks RecoverKeyProcess recoverKeyProcess;

  @Mock DomainHelperService domainHelperService;
  @Mock Hls5Connector hls5Connector;
  @Mock SecretManagementService secretManagementService;
  @Mock LocalThrottlingServiceImpl throttlingService;
  @Mock DlmsDeviceRepository dlmsDeviceRepository;
  @Mock ThrottlingClientConfig throttlingClientConfig;
  @Mock DeviceKeyProcessingService deviceKeyProcessingService;

  @Mock DlmsDevice dlmsDevice;
  @Mock MessageMetadata messageMetadata;

  @BeforeEach
  public void before() {
    this.recoverKeyProcess.setDeviceIdentification(DEVICE_IDENTIFICATION);
    this.recoverKeyProcess.setMessageMetadata(this.messageMetadata);
  }

  @Test
  void testWhenDeviceNotFoundThenException() throws OsgpException {

    when(this.domainHelperService.findDlmsDevice(DEVICE_IDENTIFICATION))
        .thenThrow(
            new FunctionalException(
                FunctionalExceptionType.UNKNOWN_DEVICE, ComponentType.PROTOCOL_DLMS));

    assertThrows(RecoverKeyException.class, () -> this.recoverKeyProcess.run());

    verify(this.secretManagementService, never()).activateNewKeys(any(), any(), any());
  }

  @Test
  void testWhenHasNoNewKeysToConnectWith() throws OsgpException, IOException {
    final int btsId = 1;
    final int cellId = 2;
    final int priority = 3;

    when(this.domainHelperService.findDlmsDevice(DEVICE_IDENTIFICATION))
        .thenReturn(this.dlmsDevice);

    when(this.secretManagementService.hasNewSecret(
            eq(this.messageMetadata), eq(DEVICE_IDENTIFICATION)))
        .thenReturn(false);

    this.recoverKeyProcess.run();

    verify(this.secretManagementService, times(1))
        .hasNewSecret(this.messageMetadata, DEVICE_IDENTIFICATION);
    verify(this.domainHelperService).findDlmsDevice(DEVICE_IDENTIFICATION);
    verify(this.throttlingService, never()).requestPermit(btsId, cellId, priority);
  }

  @Test
  void testWhenNotAbleToConnectWithNewKeys() throws OsgpException, IOException {

    when(this.domainHelperService.findDlmsDevice(DEVICE_IDENTIFICATION))
        .thenReturn(this.dlmsDevice);
    when(this.hls5Connector.connectUnchecked(
            eq(this.messageMetadata), eq(this.dlmsDevice), any(), any()))
        .thenReturn(null);

    when(this.secretManagementService.hasNewSecret(
            eq(this.messageMetadata), eq(DEVICE_IDENTIFICATION)))
        .thenReturn(true);

    this.recoverKeyProcess.run();

    verify(this.secretManagementService, times(1))
        .hasNewSecret(this.messageMetadata, DEVICE_IDENTIFICATION);
    verify(this.domainHelperService).findDlmsDevice(DEVICE_IDENTIFICATION);
    verify(this.secretManagementService, never()).activateNewKeys(any(), any(), any());
  }

  @Test
  void testThrottlingServiceCalledAndKeysActivated() throws Exception {
    final int btsId = 1;
    final int cellId = 2;
    final int priority = 3;
    final Permit permit = mock(Permit.class);

    when(this.domainHelperService.findDlmsDevice(DEVICE_IDENTIFICATION))
        .thenReturn(this.dlmsDevice);
    when(this.dlmsDevice.needsInvocationCounter()).thenReturn(true);
    when(this.hls5Connector.connectUnchecked(
            eq(this.messageMetadata), eq(this.dlmsDevice), any(), any()))
        .thenReturn(mock(DlmsConnection.class));

    when(this.secretManagementService.hasNewSecret(
            eq(this.messageMetadata), eq(DEVICE_IDENTIFICATION)))
        .thenReturn(true);

    when(this.messageMetadata.getBaseTransceiverStationId()).thenReturn(btsId);
    when(this.messageMetadata.getCellId()).thenReturn(cellId);
    when(this.messageMetadata.getMessagePriority()).thenReturn(priority);
    when(this.throttlingService.requestPermit(btsId, cellId, priority)).thenReturn(permit);

    this.recoverKeyProcess.run();

    final InOrder inOrder = inOrder(this.throttlingService, this.hls5Connector);

    inOrder.verify(this.throttlingService).requestPermit(btsId, cellId, priority);
    inOrder
        .verify(this.hls5Connector)
        .connectUnchecked(eq(this.messageMetadata), eq(this.dlmsDevice), any(), any());
    inOrder.verify(this.throttlingService).releasePermit(permit);

    verify(this.secretManagementService)
        .activateNewKeys(
            this.messageMetadata,
            DEVICE_IDENTIFICATION,
            Arrays.asList(E_METER_ENCRYPTION, E_METER_AUTHENTICATION));
    verify(this.dlmsDeviceRepository)
        .updateInvocationCounter(
            this.dlmsDevice.getDeviceIdentification(), this.dlmsDevice.getInvocationCounter());
  }

  @Test
  void testWhenConnectionFailedThenConnectionClosedAtThrottlingService() throws Exception {
    final int btsId = 1;
    final int cellId = 2;
    final int priority = 3;
    final Permit permit = mock(Permit.class);

    when(this.domainHelperService.findDlmsDevice(DEVICE_IDENTIFICATION))
        .thenReturn(this.dlmsDevice);
    when(this.hls5Connector.connectUnchecked(any(), any(), any(), any())).thenReturn(null);

    when(this.messageMetadata.getBaseTransceiverStationId()).thenReturn(btsId);
    when(this.messageMetadata.getCellId()).thenReturn(cellId);
    when(this.messageMetadata.getMessagePriority()).thenReturn(priority);
    when(this.throttlingService.requestPermit(btsId, cellId, priority)).thenReturn(permit);

    when(this.secretManagementService.hasNewSecret(
            eq(this.messageMetadata), eq(DEVICE_IDENTIFICATION)))
        .thenReturn(true);

    this.recoverKeyProcess.run();

    final InOrder inOrder = inOrder(this.throttlingService, this.hls5Connector);

    inOrder.verify(this.throttlingService).requestPermit(btsId, cellId, priority);
    inOrder.verify(this.hls5Connector).connectUnchecked(any(), any(), any(), any());
    inOrder.verify(this.throttlingService).releasePermit(permit);

    verify(this.secretManagementService, never()).activateNewKeys(any(), any(), any());
  }

  @Test
  void setsIpAddressWhenConnectingToTheDevice() throws Exception {
    when(this.domainHelperService.findDlmsDevice(DEVICE_IDENTIFICATION))
        .thenReturn(this.dlmsDevice);
    when(this.secretManagementService.hasNewSecret(
            eq(this.messageMetadata), eq(DEVICE_IDENTIFICATION)))
        .thenReturn(true);

    this.recoverKeyProcess.run();

    verify(this.domainHelperService)
        .setIpAddressFromMessageMetadataOrSessionProvider(this.dlmsDevice, this.messageMetadata);
  }
}
