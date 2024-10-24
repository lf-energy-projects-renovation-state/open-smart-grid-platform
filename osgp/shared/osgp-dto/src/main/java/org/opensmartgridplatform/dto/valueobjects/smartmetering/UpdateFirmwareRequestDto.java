// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dto.valueobjects.smartmetering;

public class UpdateFirmwareRequestDto implements ActionRequestDto {

  private static final long serialVersionUID = 4779593744529504287L;

  private final String deviceIdentification;
  private final UpdateFirmwareRequestDataDto updateFirmwareRequestDataDto;

  public UpdateFirmwareRequestDto(
      final String deviceIdentification,
      final UpdateFirmwareRequestDataDto updateFirmwareRequestDataDto) {
    this.updateFirmwareRequestDataDto = updateFirmwareRequestDataDto;
    this.deviceIdentification = deviceIdentification;
  }

  public UpdateFirmwareRequestDataDto getUpdateFirmwareRequestDataDto() {
    return this.updateFirmwareRequestDataDto;
  }

  public String getDeviceIdentification() {
    return this.deviceIdentification;
  }
}
