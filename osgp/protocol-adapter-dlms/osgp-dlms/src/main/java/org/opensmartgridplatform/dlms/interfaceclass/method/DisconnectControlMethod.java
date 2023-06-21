// Copyright 2012-20 Fraunhofer ISE
// Copyright 2020 Alliander N.V.
// SPDX-FileCopyrightText: Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dlms.interfaceclass.method;

import org.opensmartgridplatform.dlms.interfaceclass.InterfaceClass;

/** This class contains the methods defined for IC DisconnectControl. */
public enum DisconnectControlMethod implements MethodClass {
  REMOTE_DISCONNECT(1, true),
  REMOTE_RECONNECT(2, true);

  static final InterfaceClass INTERFACE_CLASS = InterfaceClass.DISCONNECT_CONTROL;

  private final int methodId;
  private final boolean mandatory;

  private DisconnectControlMethod(final int methodId, final boolean mandatory) {
    this.methodId = methodId;
    this.mandatory = mandatory;
  }

  @Override
  public int getMethodId() {
    return this.methodId;
  }

  @Override
  public InterfaceClass getInterfaceClass() {
    return INTERFACE_CLASS;
  }

  @Override
  public boolean isMandatory() {
    return this.mandatory;
  }

  @Override
  public String getMethodName() {
    return this.name();
  }
}
