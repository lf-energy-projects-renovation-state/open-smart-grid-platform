// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dto.valueobjects.smartmetering;

import java.util.Date;

public class MeterReadsResponseDto extends MeterReadsResponseWithLogTimeDto {
  private static final long serialVersionUID = -297320204916085999L;

  private final ActiveEnergyValuesDto activeEnergyValues;

  public MeterReadsResponseDto(final Date logTime, final ActiveEnergyValuesDto activeEnergyValues) {
    super(logTime);
    this.activeEnergyValues = activeEnergyValues;
  }

  public DlmsMeterValueDto getActiveEnergyImportTariffOne() {
    return this.activeEnergyValues.getActiveEnergyImportTariffOne();
  }

  public DlmsMeterValueDto getActiveEnergyImportTariffTwo() {
    return this.activeEnergyValues.getActiveEnergyImportTariffTwo();
  }

  public DlmsMeterValueDto getActiveEnergyExportTariffOne() {
    return this.activeEnergyValues.getActiveEnergyExportTariffOne();
  }

  public DlmsMeterValueDto getActiveEnergyExportTariffTwo() {
    return this.activeEnergyValues.getActiveEnergyExportTariffTwo();
  }

  public DlmsMeterValueDto getActiveEnergyImport() {
    return this.activeEnergyValues.getActiveEnergyImport();
  }

  public DlmsMeterValueDto getActiveEnergyExport() {
    return this.activeEnergyValues.getActiveEnergyExport();
  }

  public ActiveEnergyValuesDto getActiveEnergyValues() {
    return this.activeEnergyValues;
  }

  @Override
  public String toString() {
    return "MeterReads[logTime="
        + this.getLogTime()
        + ", "
        + this.activeEnergyValues
        + ", activeEnergyExport="
        + this.getActiveEnergyExport()
        + ", activeEnergyImportTariffOne="
        + this.getActiveEnergyImportTariffOne()
        + ", activeEnergyImportTariffTwo="
        + this.getActiveEnergyImportTariffTwo()
        + ", activeEnergyExportTariffOne="
        + this.getActiveEnergyExportTariffOne()
        + ", activeEnergyExportTariffTwo="
        + this.getActiveEnergyExportTariffTwo()
        + "]";
  }
}
