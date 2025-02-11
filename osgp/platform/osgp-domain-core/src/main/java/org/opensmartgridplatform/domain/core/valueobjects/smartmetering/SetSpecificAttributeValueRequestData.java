// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.domain.core.valueobjects.smartmetering;

import java.io.Serializable;
import org.opensmartgridplatform.domain.core.valueobjects.DeviceFunction;
import org.opensmartgridplatform.shared.exceptionhandling.FunctionalException;

public class SetSpecificAttributeValueRequestData implements Serializable, ActionRequest {

  private static final long serialVersionUID = -7326169764207317011L;

  private final String dlmsObjectTag;
  private final int attributeId;
  private final int intValue;

  public SetSpecificAttributeValueRequestData(
      final String dlmsObjectType, final int attributeId, final int intValue) {
    super();
    this.dlmsObjectTag = dlmsObjectType;
    this.attributeId = attributeId;
    this.intValue = intValue;
  }

  public String getDlmsObjectTag() {
    return this.dlmsObjectTag;
  }

  public int getAttributeId() {
    return this.attributeId;
  }

  public int getIntValue() {
    return this.intValue;
  }

  @Override
  public void validate() throws FunctionalException {
    // not needed here
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.attributeId;
    result = (prime * result) + this.intValue;
    result = (prime * result) + this.dlmsObjectTag.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (this.getClass() != obj.getClass()) {
      return false;
    }
    final SetSpecificAttributeValueRequestData other = (SetSpecificAttributeValueRequestData) obj;
    if (this.attributeId != other.attributeId) {
      return false;
    }
    if (this.intValue != other.intValue) {
      return false;
    }
    if (this.dlmsObjectTag == null) {
      if (other.dlmsObjectTag != null) {
        return false;
      }
    } else if (!this.dlmsObjectTag.equals(other.dlmsObjectTag)) {
      return false;
    }
    return true;
  }

  @Override
  public DeviceFunction getDeviceFunction() {
    return DeviceFunction.SET_SPECIFIC_ATTRIBUTE_VALUE;
  }
}
