// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.dlms.objectconfig;

public enum DlmsObjectType {
  ASSOCIATION_LN,
  CLOCK,
  DEVICE_ID_1,
  DEVICE_ID_2,
  ACTIVE_FIRMWARE_IDENTIFIER,
  ACTIVE_FIRMWARE_SIGNATURE,
  MODULE_ACTIVE_FIRMWARE_IDENTIFIER,
  MODULE_FIRMWARE_SIGNATURE,
  COMMUNICATION_MODULE_ACTIVE_FIRMWARE_IDENTIFIER,
  COMMUNICATION_MODULE_FIRMWARE_SIGNATURE,
  P1_PORT_VERSION,
  MBUS_DRIVER_ACTIVE_FIRMWARE_IDENTIFIER,
  MBUS_DRIVER_ACTIVE_FIRMWARE_SIGNATURE,
  IMAGE_TRANSFER,
  DEFINABLE_LOAD_PROFILE,
  POWER_QUALITY_PROFILE_1,
  POWER_QUALITY_PROFILE_2,
  INSTANTANEOUS_ACTIVE_POWER_IMPORT,
  INSTANTANEOUS_ACTIVE_POWER_EXPORT,
  INSTANTANEOUS_ACTIVE_POWER_IMPORT_L1,
  INSTANTANEOUS_ACTIVE_POWER_IMPORT_L2,
  INSTANTANEOUS_ACTIVE_POWER_IMPORT_L3,
  INSTANTANEOUS_ACTIVE_POWER_EXPORT_L1,
  INSTANTANEOUS_ACTIVE_POWER_EXPORT_L2,
  INSTANTANEOUS_ACTIVE_POWER_EXPORT_L3,
  INSTANTANEOUS_ACTIVE_CURRENT_TOTAL_OVER_ALL_PHASES,
  INSTANTANEOUS_CURRENT_L1,
  INSTANTANEOUS_CURRENT_L2,
  INSTANTANEOUS_CURRENT_L3,
  INSTANTANEOUS_VOLTAGE_L1,
  INSTANTANEOUS_VOLTAGE_L2,
  INSTANTANEOUS_VOLTAGE_L3,
  AVERAGE_ACTIVE_POWER_IMPORT_L1,
  AVERAGE_ACTIVE_POWER_IMPORT_L2,
  AVERAGE_ACTIVE_POWER_IMPORT_L3,
  AVERAGE_ACTIVE_POWER_EXPORT_L1,
  AVERAGE_ACTIVE_POWER_EXPORT_L2,
  AVERAGE_ACTIVE_POWER_EXPORT_L3,
  AVERAGE_REACTIVE_POWER_IMPORT_L1,
  AVERAGE_REACTIVE_POWER_IMPORT_L2,
  AVERAGE_REACTIVE_POWER_IMPORT_L3,
  AVERAGE_REACTIVE_POWER_EXPORT_L1,
  AVERAGE_REACTIVE_POWER_EXPORT_L2,
  AVERAGE_REACTIVE_POWER_EXPORT_L3,
  AVERAGE_CURRENT_L1,
  AVERAGE_CURRENT_L2,
  AVERAGE_CURRENT_L3,
  AVERAGE_VOLTAGE_L1,
  AVERAGE_VOLTAGE_L2,
  AVERAGE_VOLTAGE_L3,
  NUMBER_OF_POWER_FAILURES,
  NUMBER_OF_LONG_POWER_FAILURES,
  NUMBER_OF_VOLTAGE_SAGS_FOR_L1,
  NUMBER_OF_VOLTAGE_SAGS_FOR_L2,
  NUMBER_OF_VOLTAGE_SAGS_FOR_L3,
  NUMBER_OF_VOLTAGE_SWELLS_FOR_L1,
  NUMBER_OF_VOLTAGE_SWELLS_FOR_L2,
  NUMBER_OF_VOLTAGE_SWELLS_FOR_L3,
  CDMA_DIAGNOSTIC,
  GPRS_DIAGNOSTIC,
  LTE_DIAGNOSTIC,
  MBUS_CLIENT_SETUP,
  MBUS_DIAGNOSTIC,
  PUSH_SETUP_UDP,
  ALARM_REGISTER_1,
  ALARM_REGISTER_2,
  ALARM_REGISTER_3,
  INTERVAL_VALUES_E,
  INTERVAL_VALUES_G,
  DAILY_VALUES_E,
  DAILY_VALUES_G,
  DAILY_VALUES_COMBINED,
  MONTHLY_VALUES_E,
  MONTHLY_VALUES_G,
  MONTHLY_VALUES_COMBINED,
  AMR_PROFILE_STATUS,
  AMR_PROFILE_STATUS_HOURLY_G,
  AMR_PROFILE_STATUS_DAILY_G,
  AMR_PROFILE_STATUS_MONTHLY_G,
  MBUS_DEVICE_ID_1,
  MBUS_MASTER_VALUE_5MIN,
  MBUS_MASTER_VALUE,
  ACTIVE_ENERGY_IMPORT,
  ACTIVE_ENERGY_IMPORT_RATE_1,
  ACTIVE_ENERGY_IMPORT_RATE_2,
  ACTIVE_ENERGY_EXPORT,
  ACTIVE_ENERGY_EXPORT_RATE_1,
  ACTIVE_ENERGY_EXPORT_RATE_2,
  ALARM_FILTER_1,
  ALARM_FILTER_2,
  ALARM_FILTER_3,
  READ_MBUS_STATUS,
  MBUS_DEVICE_CONFIG_MODEL,
  MBUS_DEVICE_CONFIG_HARDWARE_VERSION,
  MBUS_DEVICE_CONFIG_METROLOGY_VERSION,
  MBUS_DEVICE_CONFIG_OTHER_FIRMWARE_VERSION,
  MBUS_DEVICE_CONFIG_SIMPLE_VERSION_INFO,
  CLEAR_MBUS_STATUS,
  PHASE_OUTAGE_TEST,
  LAST_GASP_TEST,
  ADMINISTRATIVE_IN_OUT,
  CONFIGURATION_OBJECT,
  RANDOMISATION_SETTINGS,
  ACTIVITY_CALENDAR,
  SPECIAL_DAYS_TABLE,
  STANDARD_EVENT_LOG,
  STANDARD_EVENT_CODE,
  FRAUD_DETECTION_EVENT_LOG,
  FRAUD_DETECTION_EVENT_CODE,
  COMMUNICATION_SESSION_EVENT_LOG,
  COMMUNICATION_SESSION_EVENT_CODE,
  COMMUNICATION_SESSION_EVENT_COUNTER,
  MBUS_EVENT_LOG,
  MBUS_EVENT_CODE,
  POWER_QUALITY_EVENT_LOG,
  POWER_QUALITY_EVENT_CODE,
  AUXILIARY_EVENT_LOG,
  AUXILIARY_EVENT_CODE,
  POWER_QUALITY_EXTENDED_EVENT_LOG,
  POWER_QUALITY_EXTENDED_EVENT_CODE,
  POWER_QUALITY_EXTENDED_EVENT_MAGNITUDE,
  POWER_QUALITY_EXTENDED_EVENT_DURATION,
  POWER_QUALITY_THD_EVENT_LOG,
  POWER_QUALITY_THD_EVENT_CODE,
  POWER_QUALITY_THD_EVENT_MAGNITUDE,
  POWER_QUALITY_THD_EVENT_DURATION,
  MAGNITUDE_OF_LAST_VOLTAGE_SAG_IN_PHASE_L1,
  MAGNITUDE_OF_LAST_VOLTAGE_SAG_IN_PHASE_L2,
  MAGNITUDE_OF_LAST_VOLTAGE_SAG_IN_PHASE_L3,
  MAGNITUDE_OF_LAST_VOLTAGE_SWELL_IN_PHASE_L1,
  MAGNITUDE_OF_LAST_VOLTAGE_SWELL_IN_PHASE_L2,
  MAGNITUDE_OF_LAST_VOLTAGE_SWELL_IN_PHASE_L3,
  DURATION_OF_LAST_VOLTAGE_SAG_IN_PHASE_L1,
  DURATION_OF_LAST_VOLTAGE_SAG_IN_PHASE_L2,
  DURATION_OF_LAST_VOLTAGE_SAG_IN_PHASE_L3,
  DURATION_OF_LAST_VOLTAGE_SWELL_IN_PHASE_L1,
  DURATION_OF_LAST_VOLTAGE_SWELL_IN_PHASE_L2,
  DURATION_OF_LAST_VOLTAGE_SWELL_IN_PHASE_L3,
  MAGNITUDE_OF_LAST_CURRENT_OVER_LIMIT_THD_IN_PHASE_L1,
  MAGNITUDE_OF_LAST_CURRENT_OVER_LIMIT_THD_IN_PHASE_L2,
  MAGNITUDE_OF_LAST_CURRENT_OVER_LIMIT_THD_IN_PHASE_L3,
  THD_VALUE_THRESHOLD,
  THD_VALUE_HYSTERESIS,
  THD_MIN_DURATION_NORMAL_TO_OVER,
  THD_MIN_DURATION_OVER_TO_NORMAL,
  THD_TIME_THRESHOLD,
  THD_INSTANTANEOUS_CURRENT_L1,
  THD_INSTANTANEOUS_CURRENT_L2,
  THD_INSTANTANEOUS_CURRENT_L3,
  THD_INSTANTANEOUS_CURRENT_FINGERPRINT_L1,
  THD_INSTANTANEOUS_CURRENT_FINGERPRINT_L2,
  THD_INSTANTANEOUS_CURRENT_FINGERPRINT_L3,
  THD_CURRENT_OVER_LIMIT_COUNTER_L1,
  THD_CURRENT_OVER_LIMIT_COUNTER_L2,
  THD_CURRENT_OVER_LIMIT_COUNTER_L3,
  PUSH_SETUP_ALARM,
  PUSH_SETUP_SMS;

  public String value() {
    return this.name();
  }

  public static DlmsObjectType fromValue(final String v) {
    return valueOf(v);
  }
}
