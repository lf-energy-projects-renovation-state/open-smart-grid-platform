# SPDX-FileCopyrightText: Contributors to the GXF project
#
# SPDX-License-Identifier: Apache-2.0

@SmartMetering @Platform @NightlyBuildOnly
Feature: SmartMetering Configuration - Configure Definable Load Profile
  As a grid operator
  I want to be able to change the definable load profile
  So I can define the values to be monitored

  Scenario Outline: Set capture objects clock and Instantaneous voltage for phase 1 in definable load profile for a <protocol> <version> device
    Given a dlms device
      | DeviceIdentification | <deviceIdentification> |
      | DeviceType           | SMART_METER_E          |
      | Protocol             | <protocol>             |
      | ProtocolVersion      | <version>              |
    When a Configure Definable Load Profile request is received
      | DeviceIdentification           | <deviceIdentification> |
      | NumberOfCaptureObjects         |                      1 |
      | CaptureObject_ClassId_1        |                      3 |
      | CaptureObject_LogicalName_1    | 1.0.32.7.0.255         |
      | CaptureObject_AttributeIndex_1 |                      2 |
      | CaptureObject_DataIndex_1      |                      0 |
    Then the Configure Definable Load Profile response should be returned
      | DeviceIdentification | <deviceIdentification> |
      | Result               | <response>             |
    And the Definable Load Profile of "<deviceIdentification>" contains
      | NumberOfCaptureObjects         |              2 |
      | CaptureObject_ClassId_1        |              8 |
      | CaptureObject_LogicalName_1    | 0.0.1.0.0.255  |
      | CaptureObject_AttributeIndex_1 |              2 |
      | CaptureObject_DataIndex_1      |              0 |
      | CaptureObject_ClassId_2        |              3 |
      | CaptureObject_LogicalName_2    | 1.0.32.7.0.255 |
      | CaptureObject_AttributeIndex_2 |              2 |
      | CaptureObject_DataIndex_2      |              0 |

    Examples:
      | deviceIdentification | protocol | version | response |
      | TEST1024000000001    | DSMR     | 4.2.2   | OK       |
    @NightlyBuildOnly
    Examples:
      | deviceIdentification | protocol | version | response |
      | TEST1024000000001    | DSMR     | 2.2     | NOT_OK   |
      | TEST1031000000001    | SMR      | 4.3     | OK       |
      | TEST1027000000001    | SMR      | 5.0.0   | OK       |
      | TEST1028000000001    | SMR      | 5.1     | OK       |
      | TEST1029000000001    | SMR      | 5.2     | OK       |
      | TEST1030000000001    | SMR      | 5.5     | OK       |

  Scenario Outline: Set capture period to 1 hour in definable load profile for a <protocol> <version> device
    Given a dlms device
      | DeviceIdentification | <deviceIdentification> |
      | DeviceType           | SMART_METER_E          |
      | Protocol             | <protocol>             |
      | ProtocolVersion      | <version>              |
    When a Configure Definable Load Profile request is received
      | DeviceIdentification | <deviceIdentification> |
      | CapturePeriod        |                   3600 |
    Then the Configure Definable Load Profile response should be returned
      | DeviceIdentification | TEST1024000000001 |
      | Result               | <response>        |
    And the Definable Load Profile of "<deviceIdentification>" contains
      | CapturePeriod | 3600 |

    Examples:
      | deviceIdentification | protocol | version | response |
      | TEST1024000000001    | DSMR     | 4.2.2   | OK       |
    @NightlyBuildOnly
    Examples:
      | deviceIdentification | protocol | version | response |
      | TEST1024000000001    | DSMR     | 2.2     | NOT_OK   |
      | TEST1031000000001    | SMR      | 4.3     | OK       |
      | TEST1027000000001    | SMR      | 5.0.0   | OK       |
      | TEST1028000000001    | SMR      | 5.1     | OK       |
      | TEST1029000000001    | SMR      | 5.2     | OK       |
      | TEST1030000000001    | SMR      | 5.5     | OK       |
