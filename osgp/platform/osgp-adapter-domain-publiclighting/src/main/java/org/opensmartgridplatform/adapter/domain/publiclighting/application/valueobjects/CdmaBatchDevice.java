// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.domain.publiclighting.application.valueobjects;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.opensmartgridplatform.domain.core.valueobjects.CdmaDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CdmaBatchDevice {

  private static final Logger LOGGER = LoggerFactory.getLogger(CdmaBatchDevice.class);

  private final String deviceIdentification;
  private final String networkAddress;

  public CdmaBatchDevice(final String deviceIdentification, final String networkAddress) {
    this.deviceIdentification = deviceIdentification;
    this.networkAddress = networkAddress;
  }

  public CdmaBatchDevice(final CdmaDevice cdmaDevice) {
    this.deviceIdentification = cdmaDevice.getDeviceIdentification();
    this.networkAddress = cdmaDevice.getNetworkAddress();
  }

  public String getDeviceIdentification() {
    return this.deviceIdentification;
  }

  public InetAddress getInetAddress() {
    try {
      return InetAddress.getByName(this.networkAddress);
    } catch (final UnknownHostException e) {
      LOGGER.info("Cannot convert network address: {} to Inetaddress", this.networkAddress);
      return null;
    }
  }

  @Override
  public String toString() {
    return "CdmaBatchDevice [deviceIdentification="
        + this.deviceIdentification
        + ", networkAddress="
        + this.networkAddress
        + "]";
  }
}
