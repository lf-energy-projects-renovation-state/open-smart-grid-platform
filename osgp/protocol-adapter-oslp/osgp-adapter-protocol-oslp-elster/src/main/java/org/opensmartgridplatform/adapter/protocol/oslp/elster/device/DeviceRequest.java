// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.oslp.elster.device;

import java.io.Serial;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.opensmartgridplatform.adapter.protocol.oslp.elster.exceptions.ConnectionFailureException;
import org.opensmartgridplatform.shared.infra.jms.MessageMetadata;
import org.opensmartgridplatform.shared.wsheaderattribute.priority.MessagePriorityEnum;

public class DeviceRequest implements Serializable {

  @Serial private static final long serialVersionUID = -9137084878269344459L;

  private final String organisationIdentification;
  private final String deviceIdentification;
  private final String correlationUid;
  private String domain = null;
  private String domainVersion = null;
  private String messageType = null;
  private final int messagePriority;
  private String networkAddress = null;
  private int retryCount = 0;
  private boolean isScheduled = false;

  public DeviceRequest(
      final String organisationIdentification,
      final String deviceIdentification,
      final String correlationUid,
      final int messagePriority) {
    this.organisationIdentification = organisationIdentification;
    this.deviceIdentification = deviceIdentification;
    this.correlationUid = correlationUid;
    this.messagePriority = messagePriority;
  }

  public DeviceRequest(final Builder builder) {
    this.organisationIdentification = builder.organisationIdentification;
    this.deviceIdentification = builder.deviceIdentification;
    this.correlationUid = builder.correlationUid;
    this.domain = builder.domain;
    this.domainVersion = builder.domainVersion;
    this.messageType = builder.messageType;
    this.messagePriority = builder.messagePriority;
    this.networkAddress = builder.ipAddress;
    this.retryCount = builder.retryCount;
    this.isScheduled = builder.isScheduled;
  }

  public static class Builder {
    private String organisationIdentification = null;
    private String deviceIdentification = null;
    private String correlationUid = null;
    private String domain = null;
    private String domainVersion = null;
    private String messageType = null;
    private int messagePriority = MessagePriorityEnum.DEFAULT.getPriority();
    private String ipAddress = null;
    private int retryCount = 0;
    private boolean isScheduled = false;

    public Builder messageMetaData(final MessageMetadata messageMetadata) {
      this.organisationIdentification = messageMetadata.getOrganisationIdentification();
      this.deviceIdentification = messageMetadata.getDeviceIdentification();
      this.correlationUid = messageMetadata.getCorrelationUid();
      this.domain = messageMetadata.getDomain();
      this.domainVersion = messageMetadata.getDomainVersion();
      this.messageType = messageMetadata.getMessageType();
      this.messagePriority = messageMetadata.getMessagePriority();
      this.ipAddress = messageMetadata.getIpAddress();
      this.retryCount = messageMetadata.getRetryCount();
      this.isScheduled = messageMetadata.isScheduled();
      return this;
    }

    public Builder organisationIdentification(final String organisationIdentification) {
      this.organisationIdentification = organisationIdentification;
      return this;
    }

    public Builder deviceIdentification(final String deviceIdentification) {
      this.deviceIdentification = deviceIdentification;
      return this;
    }

    public Builder correlationUid(final String correlationUid) {
      this.correlationUid = correlationUid;
      return this;
    }

    public Builder domain(final String domain) {
      this.domain = domain;
      return this;
    }

    public Builder domainVersion(final String domainVersion) {
      this.domainVersion = domainVersion;
      return this;
    }

    public Builder messageType(final String messageType) {
      this.messageType = messageType;
      return this;
    }

    public Builder messagePriority(final int messagePriority) {
      this.messagePriority = messagePriority;
      return this;
    }

    public Builder ipAddress(final String ipAddress) {
      this.ipAddress = ipAddress;
      return this;
    }

    public Builder retryCount(final int retryCount) {
      this.retryCount = retryCount;
      return this;
    }

    public Builder isScheduled(final boolean isScheduled) {
      this.isScheduled = isScheduled;
      return this;
    }

    public DeviceRequest build() {
      return new DeviceRequest(this);
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getOrganisationIdentification() {
    return this.organisationIdentification;
  }

  public String getDeviceIdentification() {
    return this.deviceIdentification;
  }

  public String getCorrelationUid() {
    return this.correlationUid;
  }

  public String getDomain() {
    return this.domain;
  }

  public String getDomainVersion() {
    return this.domainVersion;
  }

  public String getMessageType() {
    return this.messageType;
  }

  public int getMessagePriority() {
    return this.messagePriority;
  }

  public String getIpAddress() throws ConnectionFailureException {
    try {
      return InetAddress.getByName(this.networkAddress).getHostAddress();
    } catch (final UnknownHostException e) {
      throw new ConnectionFailureException("Cannot resolve IP address", e);
    }
  }

  public String getNetworkAddress() {
    return this.networkAddress;
  }

  public int getRetryCount() {
    return this.retryCount;
  }

  public boolean isScheduled() {
    return this.isScheduled;
  }
}
