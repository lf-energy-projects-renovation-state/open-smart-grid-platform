// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dto.valueobjects.smartmetering;

import java.io.Serial;
import lombok.Getter;

@Getter
public class SetSpecificAttributeValueRequestDto implements ActionRequestDto {

  @Serial private static final long serialVersionUID = 6091630820323702494L;

  private final String dlmsObjectTag;
  private final int attributeId;
  private final Integer intValue;

  public SetSpecificAttributeValueRequestDto(
      final String dlmsObjectTag, final int attributeId, final Integer intValue) {
    super();
    this.dlmsObjectTag = dlmsObjectTag;
    this.attributeId = attributeId;
    this.intValue = intValue;
  }
}
