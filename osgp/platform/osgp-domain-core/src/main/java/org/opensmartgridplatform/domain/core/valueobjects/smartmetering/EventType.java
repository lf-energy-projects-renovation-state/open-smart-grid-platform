// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.domain.core.valueobjects.smartmetering;

public enum EventType {
  EVENTLOG_CLEARED(255),
  POWER_FAILURE(1),
  POWER_FAILURE_G(1),
  POWER_FAILURE_W(1),
  POWER_RETURNED(2),
  CLOCK_UPDATE(3),
  CLOCK_ADJUSTED_OLD_TIME(4),
  CLOCK_ADJUSTED_NEW_TIME(5),
  CLOCK_INVALID(6),
  REPLACE_BATTERY(7),
  BATTERY_VOLTAGE_LOW(8),
  TARIFF_ACTIVATED(9),
  ERROR_REGISTER_CLEARED(10),
  ALARM_REGISTER_CLEARED(11),
  HARDWARE_ERROR_PROGRAM_MEMORY(12),
  HARDWARE_ERROR_RAM(13),
  HARDWARE_ERROR_NV_MEMORY(14),
  WATCHDOG_ERROR(15),
  HARDWARE_ERROR_MEASUREMENT_SYSTEM(16),
  FIRMWARE_READY_FOR_ACTIVATION(17),
  FIRMWARE_ACTIVATED(18),
  PASSIVE_TARIFF_UPDATED(19),
  SUCCESSFUL_SELFCHECK_AFTER_FIRMWARE_UPDATE(20),
  COMMUNICATION_MODULE_REMOVED(21),
  COMMUNICATION_MODULE_INSERTED(22),
  ERROR_REGISTER_2_CLEARED(23),
  ALARM_REGISTER_2_CLEARED(24),
  LAST_GASP_TEST(25),
  TERMINAL_COVER_REMOVED(40),
  TERMINAL_COVER_CLOSED(41),
  STRONG_DC_FIELD_DETECTED(42),
  NO_STRONG_DC_FIELD_ANYMORE(43),
  METER_COVER_REMOVED(44),
  METER_COVER_CLOSED(45),
  FAILED_LOGIN_ATTEMPT(46),
  CONFIGURATION_CHANGE(47),
  MODULE_COVER_OPENED(48),
  MODULE_COVER_CLOSED(49),
  METROLOGICAL_MAINTENANCE(71),
  TECHNICAL_MAINTENANCE(72),
  RETRIEVE_METER_READINGS_E(73),
  RETRIEVE_METER_READINGS_G(74),
  RETRIEVE_INTERVAL_DATA_E(75),
  RETRIEVE_INTERVAL_DATA_G(76),
  UNDER_VOLTAGE_L1(77),
  UNDER_VOLTAGE_L2(78),
  UNDER_VOLTAGE_L3(79),
  PV_VOLTAGE_SAG_L1(80),
  PV_VOLTAGE_SAG_L2(81),
  PV_VOLTAGE_SAG_L3(82),
  PV_VOLTAGE_SWELL_L1(83),
  PV_VOLTAGE_SWELL_L2(84),
  PV_VOLTAGE_SWELL_L3(85),
  OVER_VOLTAGE_L1(80),
  OVER_VOLTAGE_L2(81),
  OVER_VOLTAGE_L3(82),
  VOLTAGE_L1_NORMAL(83),
  VOLTAGE_L2_NORMAL(84),
  VOLTAGE_L3_NORMAL(85),
  PHASE_OUTAGE_L1(86),
  PHASE_OUTAGE_L2(87),
  PHASE_OUTAGE_L3(88),
  PHASE_OUTAGE_TEST(89),
  PHASE_RETURNED_L1(90),
  PHASE_RETURNED_L2(91),
  PHASE_RETURNED_L3(92),
  VOLTAGE_SAG_IN_PHASE_L1(93),
  VOLTAGE_SAG_IN_PHASE_L2(94),
  VOLTAGE_SAG_IN_PHASE_L3(95),
  VOLTAGE_SWELL_IN_PHASE_L1(96),
  VOLTAGE_SWELL_IN_PHASE_L2(97),
  VOLTAGE_SWELL_IN_PHASE_L3(98),
  LAST_GASP(99),
  COMMUNICATION_ERROR_M_BUS_CHANNEL_1(100),
  COMMUNICATION_OK_M_BUS_CHANNEL_1(101),
  REPLACE_BATTERY_M_BUS_CHANNEL_1(102),
  FRAUD_ATTEMPT_M_BUS_CHANNEL_1(103),
  CLOCK_ADJUSTED_M_BUS_CHANNEL_1(104),
  NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_1(105),
  PERMANENT_ERROR_FROM_M_BUS_DEVICE_CHANNEL_1(106),
  DEAD_BATTERY_ERROR_M_BUS_DEVICE_CHANNEL_1(107),
  COMMUNICATION_ERROR_M_BUS_CHANNEL_2(110),
  COMMUNICATION_OK_M_BUS_CHANNEL_2(111),
  REPLACE_BATTERY_M_BUS_CHANNEL_2(112),
  FRAUD_ATTEMPT_M_BUS_CHANNEL_2(113),
  CLOCK_ADJUSTED_M_BUS_CHANNEL_2(114),
  NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_2(115),
  PERMANENT_ERROR_FROM_M_BUS_DEVICE_CHANNEL_2(116),
  DEAD_BATTERY_ERROR_M_BUS_DEVICE_CHANNEL_2(117),
  COMMUNICATION_ERROR_M_BUS_CHANNEL_3(120),
  COMMUNICATION_OK_M_BUS_CHANNEL_3(121),
  REPLACE_BATTERY_M_BUS_CHANNEL_3(122),
  FRAUD_ATTEMPT_M_BUS_CHANNEL_3(123),
  CLOCK_ADJUSTED_M_BUS_CHANNEL_3(124),
  NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_3(125),
  PERMANENT_ERROR_FROM_M_BUS_DEVICE_CHANNEL_3(126),
  DEAD_BATTERY_ERROR_M_BUS_DEVICE_CHANNEL_3(127),
  COMMUNICATION_ERROR_M_BUS_CHANNEL_4(130),
  COMMUNICATION_OK_M_BUS_CHANNEL_4(131),
  REPLACE_BATTERY_M_BUS_CHANNEL_4(132),
  FRAUD_ATTEMPT_M_BUS_CHANNEL_4(133),
  CLOCK_ADJUSTED_M_BUS_CHANNEL_4(134),
  NEW_M_BUS_DEVICE_DISCOVERED_CHANNEL_4(135),
  PERMANENT_ERROR_FROM_M_BUS_DEVICE_CHANNEL_4(136),
  DEAD_BATTERY_ERROR_M_BUS_DEVICE_CHANNEL_4(137),
  MANUFACTURER_SPECIFIC_230(230),
  MANUFACTURER_SPECIFIC_231(231),
  MANUFACTURER_SPECIFIC_232(232),
  MANUFACTURER_SPECIFIC_233(233),
  MANUFACTURER_SPECIFIC_234(234),
  MANUFACTURER_SPECIFIC_235(235),
  MANUFACTURER_SPECIFIC_236(236),
  MANUFACTURER_SPECIFIC_237(237),
  MANUFACTURER_SPECIFIC_238(238),
  MANUFACTURER_SPECIFIC_239(239),
  MANUFACTURER_SPECIFIC_240(240),
  MANUFACTURER_SPECIFIC_241(241),
  MANUFACTURER_SPECIFIC_242(242),
  MANUFACTURER_SPECIFIC_243(243),
  MANUFACTURER_SPECIFIC_244(244),
  MANUFACTURER_SPECIFIC_245(245),
  MANUFACTURER_SPECIFIC_246(246),
  MANUFACTURER_SPECIFIC_247(247),
  MANUFACTURER_SPECIFIC_248(248),
  MANUFACTURER_SPECIFIC_249(249),

  AUXILIARY_EVENTLOG_CLEARED(0xFFFF),
  MBUS_FW_UPGRADE_SUCCESSFUL_CHANNEL_1(0x1000),
  MBUS_FW_UPGRADE_BLOCK_SIZE_NOT_SUPPORTED_CHANNEL_1(0x1001),
  MBUS_FW_UPGRADE_IMAGE_SIZE_TOO_BIG_CHANNEL_1(0x1002),
  MBUS_FW_UPGRADE_INVALID_BLOCK_NUMBER_CHANNEL_1(0x1003),
  MBUS_FW_UPGRADE_DATA_RECEIVE_ERROR_CHANNEL_1(0x1004),
  MBUS_FW_UPGRADE_IMAGE_NOT_COMPLETE_ERROR_CHANNEL_1(0x1005),
  MBUS_FW_UPGRADE_INVALID_SECURITY_ERROR_CHANNEL_1(0x1006),
  MBUS_FW_UPGRADE_INVALID_FIRMWARE_FOR_THIS_DEVICE_CHANNEL_1(0x1007),
  MBUS_FW_UPGRADE_SUCCESSFUL_CHANNEL_2(0x1100),
  MBUS_FW_UPGRADE_BLOCK_SIZE_NOT_SUPPORTED_CHANNEL_2(0x1101),
  MBUS_FW_UPGRADE_IMAGE_SIZE_TOO_BIG_CHANNEL_2(0x1102),
  MBUS_FW_UPGRADE_INVALID_BLOCK_NUMBER_CHANNEL_2(0x1103),
  MBUS_FW_UPGRADE_DATA_RECEIVE_ERROR_CHANNEL_2(0x1104),
  MBUS_FW_UPGRADE_IMAGE_NOT_COMPLETE_ERROR_CHANNEL_2(0x1105),
  MBUS_FW_UPGRADE_INVALID_SECURITY_ERROR_CHANNEL_2(0x1106),
  MBUS_FW_UPGRADE_INVALID_FIRMWARE_FOR_THIS_DEVICE_CHANNEL_2(0x1107),
  MBUS_FW_UPGRADE_SUCCESSFUL_CHANNEL_3(0x1200),
  MBUS_FW_UPGRADE_BLOCK_SIZE_NOT_SUPPORTED_CHANNEL_3(0x1201),
  MBUS_FW_UPGRADE_IMAGE_SIZE_TOO_BIG_CHANNEL_3(0x1202),
  MBUS_FW_UPGRADE_INVALID_BLOCK_NUMBER_CHANNEL_3(0x1203),
  MBUS_FW_UPGRADE_DATA_RECEIVE_ERROR_CHANNEL_3(0x1204),
  MBUS_FW_UPGRADE_IMAGE_NOT_COMPLETE_ERROR_CHANNEL_3(0x1205),
  MBUS_FW_UPGRADE_INVALID_SECURITY_ERROR_CHANNEL_3(0x1206),
  MBUS_FW_UPGRADE_INVALID_FIRMWARE_FOR_THIS_DEVICE_CHANNEL_3(0x1207),
  MBUS_FW_UPGRADE_SUCCESSFUL_CHANNEL_4(0x1300),
  MBUS_FW_UPGRADE_BLOCK_SIZE_NOT_SUPPORTED_CHANNEL_4(0x1301),
  MBUS_FW_UPGRADE_IMAGE_SIZE_TOO_BIG_CHANNEL_4(0x1302),
  MBUS_FW_UPGRADE_INVALID_BLOCK_NUMBER_CHANNEL_4(0x1303),
  MBUS_FW_UPGRADE_DATA_RECEIVE_ERROR_CHANNEL_4(0x1304),
  MBUS_FW_UPGRADE_IMAGE_NOT_COMPLETE_ERROR_CHANNEL_4(0x1305),
  MBUS_FW_UPGRADE_INVALID_SECURITY_ERROR_CHANNEL_4(0x1306),
  MBUS_FW_UPGRADE_INVALID_FIRMWARE_FOR_THIS_DEVICE_CHANNEL_4(0x1307),

  MBUS_STATUS_BIT_0_BATTERY_LOW_CHANNEL_1(0x8080),
  MBUS_STATUS_BIT_1_BATTERY_CONSUMPTION_TOO_HIGH_CHANNEL_1(0x8081),
  MBUS_STATUS_BIT_2_REVERSE_FLOW_CHANNEL_1(0x8082),
  MBUS_STATUS_BIT_3_TAMPER_P2_CHANNEL_1(0x8083),
  MBUS_STATUS_BIT_4_TAMPER_P0_CHANNEL_1(0x8084),
  MBUS_STATUS_BIT_5_TAMPER_CASE_CHANNEL_1(0x8085),
  MBUS_STATUS_BIT_6_TAMPER_MAGNETIC_CHANNEL_1(0x8086),
  MBUS_STATUS_BIT_7_TEMP_OUT_OF_RANGE_CHANNEL_1(0x8087),
  MBUS_STATUS_BIT_8_CLOCK_SYNC_ERROR_CHANNEL_1(0x8088),
  MBUS_STATUS_BIT_9_SW_ERROR_CHANNEL_1(0x8089),
  MBUS_STATUS_BIT_10_WATCHDOG_ERROR_CHANNEL_1(0x808A),
  MBUS_STATUS_BIT_11_SYSTEM_HW_ERROR_CHANNEL_1(0x808B),
  MBUS_STATUS_BIT_12_CFG_CALIBRATION_ERROR_CHANNEL_1(0x808C),
  MBUS_STATUS_BIT_13_HIGH_FLOW_GREATER_THAN_QMAX_CHANNEL_1(0x808D),
  MBUS_STATUS_BIT_14_TEMP_SENSOR_ERROR_CHANNEL_1(0x808E),
  MBUS_STATUS_BIT_15_RESERVED_CHANNEL_1(0x808F),
  MBUS_STATUS_BIT_16_P0_ENABLED_CHANNEL_1(0x8090),
  MBUS_STATUS_BIT_17_NEW_KEY_ACCEPTED_CHANNEL_1(0x8091),
  MBUS_STATUS_BIT_18_NEW_KEY_REJECTED_CHANNEL_1(0x8092),
  MBUS_STATUS_BIT_19_RESERVED_CHANNEL_1(0x8093),
  MBUS_STATUS_BIT_20_MANUFACTURER_SPECIFIC_CHANNEL_1(0x8094),
  MBUS_STATUS_BIT_21_MANUFACTURER_SPECIFIC_CHANNEL_1(0x8095),
  MBUS_STATUS_BIT_22_MANUFACTURER_SPECIFIC_CHANNEL_1(0x8096),
  MBUS_STATUS_BIT_23_MANUFACTURER_SPECIFIC_CHANNEL_1(0x8097),
  MBUS_STATUS_BIT_24_MANUFACTURER_SPECIFIC_CHANNEL_1(0x8098),
  MBUS_STATUS_BIT_25_MANUFACTURER_SPECIFIC_CHANNEL_1(0x8099),
  MBUS_STATUS_BIT_26_MANUFACTURER_SPECIFIC_CHANNEL_1(0x809A),
  MBUS_STATUS_BIT_27_MANUFACTURER_SPECIFIC_CHANNEL_1(0x809B),
  MBUS_STATUS_BIT_28_MANUFACTURER_SPECIFIC_CHANNEL_1(0x809C),
  MBUS_STATUS_BIT_29_MANUFACTURER_SPECIFIC_CHANNEL_1(0x809D),
  MBUS_STATUS_BIT_30_MANUFACTURER_SPECIFIC_CHANNEL_1(0x809E),
  MBUS_STATUS_BIT_31_MANUFACTURER_SPECIFIC_CHANNEL_1(0x809F),
  KEY_SENT_TO_MBUS_DEVICE_ON_CHANNEL_1(0x80A0),
  KEY_ACKNOWLEDGED_BY_MBUS_DEVICE_ON_CHANNEL_1(0x80A1),

  MBUS_STATUS_BIT_0_BATTERY_LOW_CHANNEL_2(0x8180),
  MBUS_STATUS_BIT_1_BATTERY_CONSUMPTION_TOO_HIGH_CHANNEL_2(0x8181),
  MBUS_STATUS_BIT_2_REVERSE_FLOW_CHANNEL_2(0x8182),
  MBUS_STATUS_BIT_3_TAMPER_P2_CHANNEL_2(0x8183),
  MBUS_STATUS_BIT_4_TAMPER_P0_CHANNEL_2(0x8184),
  MBUS_STATUS_BIT_5_TAMPER_CASE_CHANNEL_2(0x8185),
  MBUS_STATUS_BIT_6_TAMPER_MAGNETIC_CHANNEL_2(0x8186),
  MBUS_STATUS_BIT_7_TEMP_OUT_OF_RANGE_CHANNEL_2(0x8187),
  MBUS_STATUS_BIT_8_CLOCK_SYNC_ERROR_CHANNEL_2(0x8188),
  MBUS_STATUS_BIT_9_SW_ERROR_CHANNEL_2(0x8189),
  MBUS_STATUS_BIT_10_WATCHDOG_ERROR_CHANNEL_2(0x818A),
  MBUS_STATUS_BIT_11_SYSTEM_HW_ERROR_CHANNEL_2(0x818B),
  MBUS_STATUS_BIT_12_CFG_CALIBRATION_ERROR_CHANNEL_2(0x818C),
  MBUS_STATUS_BIT_13_HIGH_FLOW_GREATER_THAN_QMAX_CHANNEL_2(0x818D),
  MBUS_STATUS_BIT_14_TEMP_SENSOR_ERROR_CHANNEL_2(0x818E),
  MBUS_STATUS_BIT_15_RESERVED_CHANNEL_2(0x818F),
  MBUS_STATUS_BIT_16_P0_ENABLED_CHANNEL_2(0x8190),
  MBUS_STATUS_BIT_17_NEW_KEY_ACCEPTED_CHANNEL_2(0x8191),
  MBUS_STATUS_BIT_18_NEW_KEY_REJECTED_CHANNEL_2(0x8192),
  MBUS_STATUS_BIT_19_RESERVED_CHANNEL_2(0x8193),
  MBUS_STATUS_BIT_20_MANUFACTURER_SPECIFIC_CHANNEL_2(0x8194),
  MBUS_STATUS_BIT_21_MANUFACTURER_SPECIFIC_CHANNEL_2(0x8195),
  MBUS_STATUS_BIT_22_MANUFACTURER_SPECIFIC_CHANNEL_2(0x8196),
  MBUS_STATUS_BIT_23_MANUFACTURER_SPECIFIC_CHANNEL_2(0x8197),
  MBUS_STATUS_BIT_24_MANUFACTURER_SPECIFIC_CHANNEL_2(0x8198),
  MBUS_STATUS_BIT_25_MANUFACTURER_SPECIFIC_CHANNEL_2(0x8199),
  MBUS_STATUS_BIT_26_MANUFACTURER_SPECIFIC_CHANNEL_2(0x819A),
  MBUS_STATUS_BIT_27_MANUFACTURER_SPECIFIC_CHANNEL_2(0x819B),
  MBUS_STATUS_BIT_28_MANUFACTURER_SPECIFIC_CHANNEL_2(0x819C),
  MBUS_STATUS_BIT_29_MANUFACTURER_SPECIFIC_CHANNEL_2(0x819D),
  MBUS_STATUS_BIT_30_MANUFACTURER_SPECIFIC_CHANNEL_2(0x819E),
  MBUS_STATUS_BIT_31_MANUFACTURER_SPECIFIC_CHANNEL_2(0x819F),
  KEY_SENT_TO_MBUS_DEVICE_ON_CHANNEL_2(0x81A0),
  KEY_ACKNOWLEDGED_BY_MBUS_DEVICE_ON_CHANNEL_2(0x81A1),

  MBUS_STATUS_BIT_0_BATTERY_LOW_CHANNEL_3(0x8280),
  MBUS_STATUS_BIT_1_BATTERY_CONSUMPTION_TOO_HIGH_CHANNEL_3(0x8281),
  MBUS_STATUS_BIT_2_REVERSE_FLOW_CHANNEL_3(0x8282),
  MBUS_STATUS_BIT_3_TAMPER_P2_CHANNEL_3(0x8283),
  MBUS_STATUS_BIT_4_TAMPER_P0_CHANNEL_3(0x8284),
  MBUS_STATUS_BIT_5_TAMPER_CASE_CHANNEL_3(0x8285),
  MBUS_STATUS_BIT_6_TAMPER_MAGNETIC_CHANNEL_3(0x8286),
  MBUS_STATUS_BIT_7_TEMP_OUT_OF_RANGE_CHANNEL_3(0x8287),
  MBUS_STATUS_BIT_8_CLOCK_SYNC_ERROR_CHANNEL_3(0x8288),
  MBUS_STATUS_BIT_9_SW_ERROR_CHANNEL_3(0x8289),
  MBUS_STATUS_BIT_10_WATCHDOG_ERROR_CHANNEL_3(0x828A),
  MBUS_STATUS_BIT_11_SYSTEM_HW_ERROR_CHANNEL_3(0x828B),
  MBUS_STATUS_BIT_12_CFG_CALIBRATION_ERROR_CHANNEL_3(0x828C),
  MBUS_STATUS_BIT_13_HIGH_FLOW_GREATER_THAN_QMAX_CHANNEL_3(0x828D),
  MBUS_STATUS_BIT_14_TEMP_SENSOR_ERROR_CHANNEL_3(0x828E),
  MBUS_STATUS_BIT_15_RESERVED_CHANNEL_3(0x828F),
  MBUS_STATUS_BIT_16_P0_ENABLED_CHANNEL_3(0x8290),
  MBUS_STATUS_BIT_17_NEW_KEY_ACCEPTED_CHANNEL_3(0x8291),
  MBUS_STATUS_BIT_18_NEW_KEY_REJECTED_CHANNEL_3(0x8292),
  MBUS_STATUS_BIT_19_RESERVED_CHANNEL_3(0x8293),
  MBUS_STATUS_BIT_20_MANUFACTURER_SPECIFIC_CHANNEL_3(0x8294),
  MBUS_STATUS_BIT_21_MANUFACTURER_SPECIFIC_CHANNEL_3(0x8295),
  MBUS_STATUS_BIT_22_MANUFACTURER_SPECIFIC_CHANNEL_3(0x8296),
  MBUS_STATUS_BIT_23_MANUFACTURER_SPECIFIC_CHANNEL_3(0x8297),
  MBUS_STATUS_BIT_24_MANUFACTURER_SPECIFIC_CHANNEL_3(0x8298),
  MBUS_STATUS_BIT_25_MANUFACTURER_SPECIFIC_CHANNEL_3(0x8299),
  MBUS_STATUS_BIT_26_MANUFACTURER_SPECIFIC_CHANNEL_3(0x829A),
  MBUS_STATUS_BIT_27_MANUFACTURER_SPECIFIC_CHANNEL_3(0x829B),
  MBUS_STATUS_BIT_28_MANUFACTURER_SPECIFIC_CHANNEL_3(0x829C),
  MBUS_STATUS_BIT_29_MANUFACTURER_SPECIFIC_CHANNEL_3(0x829D),
  MBUS_STATUS_BIT_30_MANUFACTURER_SPECIFIC_CHANNEL_3(0x829E),
  MBUS_STATUS_BIT_31_MANUFACTURER_SPECIFIC_CHANNEL_3(0x829F),
  KEY_SENT_TO_MBUS_DEVICE_ON_CHANNEL_3(0x82A0),
  KEY_ACKNOWLEDGED_BY_MBUS_DEVICE_ON_CHANNEL_3(0x82A1),

  MBUS_STATUS_BIT_0_BATTERY_LOW_CHANNEL_4(0x8380),
  MBUS_STATUS_BIT_1_BATTERY_CONSUMPTION_TOO_HIGH_CHANNEL_4(0x8381),
  MBUS_STATUS_BIT_2_REVERSE_FLOW_CHANNEL_4(0x8382),
  MBUS_STATUS_BIT_3_TAMPER_P2_CHANNEL_4(0x8383),
  MBUS_STATUS_BIT_4_TAMPER_P0_CHANNEL_4(0x8384),
  MBUS_STATUS_BIT_5_TAMPER_CASE_CHANNEL_4(0x8385),
  MBUS_STATUS_BIT_6_TAMPER_MAGNETIC_CHANNEL_4(0x8386),
  MBUS_STATUS_BIT_7_TEMP_OUT_OF_RANGE_CHANNEL_4(0x8387),
  MBUS_STATUS_BIT_8_CLOCK_SYNC_ERROR_CHANNEL_4(0x8388),
  MBUS_STATUS_BIT_9_SW_ERROR_CHANNEL_4(0x8389),
  MBUS_STATUS_BIT_10_WATCHDOG_ERROR_CHANNEL_4(0x838A),
  MBUS_STATUS_BIT_11_SYSTEM_HW_ERROR_CHANNEL_4(0x838B),
  MBUS_STATUS_BIT_12_CFG_CALIBRATION_ERROR_CHANNEL_4(0x838C),
  MBUS_STATUS_BIT_13_HIGH_FLOW_GREATER_THAN_QMAX_CHANNEL_4(0x838D),
  MBUS_STATUS_BIT_14_TEMP_SENSOR_ERROR_CHANNEL_4(0x838E),
  MBUS_STATUS_BIT_15_RESERVED_CHANNEL_4(0x838F),
  MBUS_STATUS_BIT_16_P0_ENABLED_CHANNEL_4(0x8390),
  MBUS_STATUS_BIT_17_NEW_KEY_ACCEPTED_CHANNEL_4(0x8391),
  MBUS_STATUS_BIT_18_NEW_KEY_REJECTED_CHANNEL_4(0x8392),
  MBUS_STATUS_BIT_19_RESERVED_CHANNEL_4(0x8393),
  MBUS_STATUS_BIT_20_MANUFACTURER_SPECIFIC_CHANNEL_4(0x8394),
  MBUS_STATUS_BIT_21_MANUFACTURER_SPECIFIC_CHANNEL_4(0x8395),
  MBUS_STATUS_BIT_22_MANUFACTURER_SPECIFIC_CHANNEL_4(0x8396),
  MBUS_STATUS_BIT_23_MANUFACTURER_SPECIFIC_CHANNEL_4(0x8397),
  MBUS_STATUS_BIT_24_MANUFACTURER_SPECIFIC_CHANNEL_4(0x8398),
  MBUS_STATUS_BIT_25_MANUFACTURER_SPECIFIC_CHANNEL_4(0x8399),
  MBUS_STATUS_BIT_26_MANUFACTURER_SPECIFIC_CHANNEL_4(0x839A),
  MBUS_STATUS_BIT_27_MANUFACTURER_SPECIFIC_CHANNEL_4(0x839B),
  MBUS_STATUS_BIT_28_MANUFACTURER_SPECIFIC_CHANNEL_4(0x839C),
  MBUS_STATUS_BIT_29_MANUFACTURER_SPECIFIC_CHANNEL_4(0x839D),
  MBUS_STATUS_BIT_30_MANUFACTURER_SPECIFIC_CHANNEL_4(0x839E),
  MBUS_STATUS_BIT_31_MANUFACTURER_SPECIFIC_CHANNEL_4(0x839F),
  KEY_SENT_TO_MBUS_DEVICE_ON_CHANNEL_4(0x83A0),
  KEY_ACKNOWLEDGED_BY_MBUS_DEVICE_ON_CHANNEL_4(0x83A1),
  UNKNOWN_EVENT_HEADEND(999);

  final int eventCode;

  EventType(final int eventCode) {
    this.eventCode = eventCode;
  }

  public static EventType fromValue(final String v) {
    return valueOf(v);
  }

  public int getEventCode() {
    return this.eventCode;
  }
}
