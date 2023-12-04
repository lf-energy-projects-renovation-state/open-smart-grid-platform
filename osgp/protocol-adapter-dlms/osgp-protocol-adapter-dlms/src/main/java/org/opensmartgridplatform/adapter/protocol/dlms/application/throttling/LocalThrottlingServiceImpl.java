/*
 * SPDX-FileCopyrightText: Copyright Contributors to the GXF project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensmartgridplatform.adapter.protocol.dlms.application.throttling;

import java.time.Duration;
import java.time.Instant;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.opensmartgridplatform.adapter.protocol.dlms.application.config.annotation.LocalThrottlingServiceCondition;
import org.opensmartgridplatform.throttling.ThrottlingPermitDeniedException;
import org.opensmartgridplatform.throttling.api.Permit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(LocalThrottlingServiceCondition.class)
public class LocalThrottlingServiceImpl implements ThrottlingService {
  private final AtomicInteger requestIdCounter = new AtomicInteger(0);

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalThrottlingServiceImpl.class);

  @Value("${throttling.max.open.connections}")
  private int maxOpenConnections;

  @Value("${throttling.max.new.connection.requests}")
  private int maxNewConnectionRequests;

  @Value("${throttling.reset.time}")
  private int resetTime;

  @Value("${cleanup.permits.interval}")
  private int cleanupExpiredPermitsInterval;

  @Value("#{T(java.time.Duration).parse('${cleanup.permits.time-to-live:PT1H}')}")
  private Duration timeToLive;

  private Semaphore openConnectionsSemaphore;
  private Semaphore newConnectionRequestsSemaphore;
  private Timer resetNewConnectionRequestsTimer;
  private Timer cleanupExpiredPermitsTimer;
  private ReentrantLock resetTimerLock;

  private ConcurrentHashMap<Integer, Permit> permitsByRequestId;

  @PostConstruct
  public void postConstruct() {
    this.openConnectionsSemaphore = new Semaphore(this.maxOpenConnections);
    this.newConnectionRequestsSemaphore = new Semaphore(this.maxNewConnectionRequests);

    this.resetTimerLock = new ReentrantLock();

    this.resetNewConnectionRequestsTimer = new Timer();
    this.resetNewConnectionRequestsTimer.scheduleAtFixedRate(
        new ResetNewConnectionRequestsTask(), this.resetTime, this.resetTime);

    this.cleanupExpiredPermitsTimer = new Timer();
    this.cleanupExpiredPermitsTimer.scheduleAtFixedRate(
        new CleanupExpiredPermitsTask(),
        this.cleanupExpiredPermitsInterval,
        this.cleanupExpiredPermitsInterval);

    this.permitsByRequestId = new ConcurrentHashMap<>();

    LOGGER.info("Initialized ThrottlingService. {}", this);
  }

  @PreDestroy
  public void preDestroy() {
    if (this.resetNewConnectionRequestsTimer != null) {
      this.resetNewConnectionRequestsTimer.cancel();
    }
    if (this.cleanupExpiredPermitsTimer != null) {
      this.cleanupExpiredPermitsTimer.cancel();
    }
  }

  @Override
  public Permit requestPermit(final Integer baseTransceiverStationId, final Integer cellId) {

    this.newConnectionRequest();

    LOGGER.debug(
        "Requesting openConnection. available = {} ",
        this.openConnectionsSemaphore.availablePermits());

    try {
      if (this.openConnectionsSemaphore.availablePermits() == 0) {
        this.handlePermitDenied("Local: max open connections reached");
      }
      this.openConnectionsSemaphore.acquire();
      LOGGER.debug(
          "openConnection granted. available = {} ",
          this.openConnectionsSemaphore.availablePermits());
    } catch (final InterruptedException e) {
      LOGGER.warn("Unable to acquire Open Connection", e);
      Thread.currentThread().interrupt();
    }
    return this.createPermit();
  }

  @Override
  public void releasePermit(final Permit permit) {

    LOGGER.debug(
        "closeConnection(). available = {}", this.openConnectionsSemaphore.availablePermits());
    if (this.openConnectionsSemaphore.availablePermits() < this.maxOpenConnections) {
      this.openConnectionsSemaphore.release();
    }

    this.permitsByRequestId.remove(permit.getRequestId());
  }

  private void newConnectionRequest() {
    LOGGER.debug(
        "Await reset for newConnection. available = {} ",
        this.newConnectionRequestsSemaphore.availablePermits());

    this.awaitReset();

    LOGGER.debug(
        "newConnectionRequest(). available = {} ",
        this.newConnectionRequestsSemaphore.availablePermits());

    try {
      if (this.newConnectionRequestsSemaphore.availablePermits() == 0) {
        this.handlePermitDenied("Local: max new connection requests reached");
      }
      this.newConnectionRequestsSemaphore.acquire();
      LOGGER.debug(
          "Request newConnection granted. available = {} ",
          this.newConnectionRequestsSemaphore.availablePermits());
    } catch (final InterruptedException e) {
      LOGGER.warn("Unable to acquire New Connection Request", e);
      Thread.currentThread().interrupt();
    }
  }

  private synchronized void awaitReset() {
    while (this.resetTimerLock.isLocked()) {
      try {
        LOGGER.info("Wait {}ms while reset timer is locked", this.resetTime);
        this.resetTimerLock.wait(this.resetTime);
      } catch (final InterruptedException e) {
        LOGGER.warn("Unable to acquire New Connection Request Lock", e);
        Thread.currentThread().interrupt();
      }
    }
  }

  private class ResetNewConnectionRequestsTask extends TimerTask {

    @Override
    public void run() {

      try {
        LocalThrottlingServiceImpl.this.resetTimerLock.lock();

        final int nrOfPermitsToBeReleased =
            LocalThrottlingServiceImpl.this.maxNewConnectionRequests
                - LocalThrottlingServiceImpl.this.newConnectionRequestsSemaphore.availablePermits();

        LOGGER.debug(
            "releasing {} permits on newConnectionRequestsSemaphore", nrOfPermitsToBeReleased);

        LocalThrottlingServiceImpl.this.newConnectionRequestsSemaphore.release(
            nrOfPermitsToBeReleased);

        LOGGER.debug(
            "ThrottlingService - Timer Reset and Unlocking, newConnectionRequests available = {}  ",
            LocalThrottlingServiceImpl.this.newConnectionRequestsSemaphore.availablePermits());
      } finally {
        LocalThrottlingServiceImpl.this.resetTimerLock.unlock();
      }
    }
  }

  private class CleanupExpiredPermitsTask extends TimerTask {

    @Override
    public void run() {
      final Instant createdAtBefore =
          Instant.now().minus(LocalThrottlingServiceImpl.this.timeToLive);
      try {
        LocalThrottlingServiceImpl.this.resetTimerLock.lock();

        for (final Entry<Integer, Permit> permitForRequestId :
            LocalThrottlingServiceImpl.this.permitsByRequestId.entrySet()) {
          final Permit permit = permitForRequestId.getValue();
          if (permit.getCreatedAt().isBefore(createdAtBefore)) {
            LOGGER.warn("releasing expired permit: {}", permit);
            LocalThrottlingServiceImpl.this.releasePermit(permit);
          }
        }

        LOGGER.debug(
            "ThrottlingService - Timer Reset and Unlocking, openConnections available = {}  ",
            LocalThrottlingServiceImpl.this.openConnectionsSemaphore.availablePermits());
      } finally {
        LocalThrottlingServiceImpl.this.resetTimerLock.unlock();
      }
    }
  }

  @Override
  public String toString() {
    return String.format(
        "ThrottlingService. maxOpenConnections = %d, maxNewConnectionRequests=%d, resetTime=%d",
        this.maxOpenConnections, this.maxNewConnectionRequests, this.resetTime);
  }

  private void handlePermitDenied(final String message) {

    throw new ThrottlingPermitDeniedException(message);
  }

  private Permit createPermit() {
    final int requestId = this.requestIdCounter.incrementAndGet();

    final Permit permit = new Permit(requestId);
    this.permitsByRequestId.put(permit.getRequestId(), permit);

    return permit;
  }
}