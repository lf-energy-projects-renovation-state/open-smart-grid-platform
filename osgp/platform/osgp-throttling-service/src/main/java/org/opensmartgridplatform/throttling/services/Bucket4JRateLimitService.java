// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.throttling.services;

import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import java.util.function.Supplier;
import org.opensmartgridplatform.throttling.model.ThrottlingSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Bucket4JRateLimitService implements RateLimitService {

  private static final String BUCKET_KEY_FORMAT = "%s_%s";

  private static final Logger log = LoggerFactory.getLogger(Bucket4JRateLimitService.class);

  private final ProxyManager<String> proxyManager;

  public Bucket4JRateLimitService(final ProxyManager<String> proxyManager) {
    this.proxyManager = proxyManager;
  }

  @Override
  public boolean isNewConnectionRequestAllowed(
      final int baseTransceiverStationId,
      final int cellId,
      final ThrottlingSettings throttlingSettings) {

    if (throttlingSettings.getMaxNewConnections() < 0) {
      log.debug("Rate-limiter is disabled, new connections are always allowed");
      return true;
    } else if (throttlingSettings.getMaxNewConnections() == 0) {
      log.warn("ThrottlingSettings.getMaxNewConnections is set to 0.");
      return false;
    }

    final Bucket bucket = this.resolveBucket(baseTransceiverStationId, cellId, throttlingSettings);
    final ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
    return probe.isConsumed();
  }

  private Bucket resolveBucket(
      final int btsId, final int cellId, final ThrottlingSettings throttlingSettings) {
    final String bucketKey = String.format(BUCKET_KEY_FORMAT, btsId, cellId);
    return this.proxyManager
        .builder()
        .build(bucketKey, this.bucketConfiguration(throttlingSettings));
  }

  private Supplier<BucketConfiguration> bucketConfiguration(
      final ThrottlingSettings throttlingSettings) {
    return () ->
        BucketConfiguration.builder()
            .addLimit(
                BandwidthBuilder.builder()
                    .capacity(throttlingSettings.getMaxNewConnections())
                    .refillGreedy(
                        throttlingSettings.getMaxNewConnections(),
                        Duration.ofMillis(throttlingSettings.getMaxNewConnectionsResetTimeInMs()))
                    .build())
            .build();
  }
}
