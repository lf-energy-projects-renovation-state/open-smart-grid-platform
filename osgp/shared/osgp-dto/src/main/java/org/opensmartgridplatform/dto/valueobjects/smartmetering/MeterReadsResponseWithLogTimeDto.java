// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dto.valueobjects.smartmetering;

import java.util.Date;

public class MeterReadsResponseWithLogTimeDto extends ActionResponseDto {
  private static final long serialVersionUID = -1911131107399785630L;

  private final Date logTime;

  public MeterReadsResponseWithLogTimeDto(final Date logTime) {
    this.logTime = new Date(logTime.getTime());
  }

  public Date getLogTime() {
    return new Date(this.logTime.getTime());
  }

  @Override
  public String toString() {
    return "MeterReadsWithLogTime[logTime=" + this.logTime + "]";
  }
}
