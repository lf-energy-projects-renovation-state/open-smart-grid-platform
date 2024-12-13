// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dlms.objectconfig;

import org.opensmartgridplatform.dlms.exceptions.ObjectConfigException;

public enum CommunicationMethod {
  GPRS("GPRS"),
  CDMA("CDMA"),
  LTE("LTE");

  private final String methodName;

  CommunicationMethod(final String methodName) {
    this.methodName = methodName;
  }

  public String getMethodName() {
    return this.methodName;
  }

  public static CommunicationMethod getCommunicationMethod(final String method)
      throws ObjectConfigException {
    return switch (method) {
      case "GPRS" -> GPRS;
      case "CDMA" -> CDMA;
      case "LTE" -> LTE;
      default -> throw new ObjectConfigException("Unknown communication method: " + method);
    };
  }
}
