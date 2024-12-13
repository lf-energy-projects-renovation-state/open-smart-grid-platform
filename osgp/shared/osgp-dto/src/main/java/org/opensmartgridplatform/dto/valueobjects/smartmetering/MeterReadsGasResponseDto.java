// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dto.valueobjects.smartmetering;

import java.util.Date;

public class MeterReadsGasResponseDto extends MeterReadsResponseWithLogTimeDto {

  private static final long serialVersionUID = -156966569210717654L;

  private final Date captureTime;
  private final DlmsMeterValueDto consumption;

  public MeterReadsGasResponseDto(
      final Date logTime, final DlmsMeterValueDto consumption, final Date captureTime) {
    super(logTime);
    if (captureTime != null) {
      this.captureTime = new Date(captureTime.getTime());
    } else {
      this.captureTime = null;
    }
    this.consumption = consumption;
  }

  public Date getCaptureTime() {
    if (this.captureTime != null) {
      return new Date(this.captureTime.getTime());
    } else {
      return null;
    }
  }

  public DlmsMeterValueDto getConsumption() {
    return this.consumption;
  }

  @Override
  public String toString() {
    return "MeterReadsGas [logTime="
        + this.getLogTime()
        + ", captureTime="
        + this.captureTime
        + ", consumption="
        + this.consumption
        + "]";
  }
}
