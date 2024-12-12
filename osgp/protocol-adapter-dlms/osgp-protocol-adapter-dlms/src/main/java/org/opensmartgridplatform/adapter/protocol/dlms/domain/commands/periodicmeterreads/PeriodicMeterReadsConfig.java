// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.periodicmeterreads;

import lombok.Getter;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.dlmsobjectconfig.model.Medium;
import org.opensmartgridplatform.dlms.objectconfig.DlmsObjectType;

@Getter
public class PeriodicMeterReadsConfig {
  private final String readDataObjectDescription;
  private final String formatDescription;
  private final Medium medium;
  private final DlmsObjectType intervalObjectType;
  private final DlmsObjectType dailyObjectType;
  private final DlmsObjectType monthlyObjectType;

  public PeriodicMeterReadsConfig(
      final String readDataObjectDescription,
      final String formatDescription,
      final Medium medium,
      final DlmsObjectType intervalObjectType,
      final DlmsObjectType dailyObjectType,
      final DlmsObjectType monthlyObjectType) {
    this.readDataObjectDescription = readDataObjectDescription;
    this.formatDescription = formatDescription;
    this.medium = medium;
    this.intervalObjectType = intervalObjectType;
    this.dailyObjectType = dailyObjectType;
    this.monthlyObjectType = monthlyObjectType;
  }
}
