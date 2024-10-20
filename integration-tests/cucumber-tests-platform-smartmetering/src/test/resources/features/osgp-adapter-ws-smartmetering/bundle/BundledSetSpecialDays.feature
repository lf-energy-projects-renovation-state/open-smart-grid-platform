# SPDX-FileCopyrightText: Contributors to the GXF project
#
# SPDX-License-Identifier: Apache-2.0

@SmartMetering @Platform
Feature: SmartMetering Bundle - SetSpecialDays
  As a grid operator 
  I want to be able to set special days on a meter via a bundle request

  Scenario Outline: Set special days on a <protocol> <version> device in a bundle request
    Given a dlms device
      | DeviceIdentification | <deviceIdentification> |
      | DeviceType           | SMART_METER_E          |
      | Protocol             | <protocol>             |
      | ProtocolVersion      | <version>              |
    And a bundle request
      | DeviceIdentification | <deviceIdentification> |
    And the bundle request contains a set special days action with parameters
      | SpecialDayCount  | 1          |
      | SpecialDayId_1   | 3          |
      | SpecialDayDate_1 | FFFFFFFFFF |
    When the bundle request is received
    Then the bundle response should contain a set special days response with values
      | Result | OK |
    Examples:
      | deviceIdentification | protocol | version |
      | TEST1024000000001    | DSMR     | 2.2     |
      | TEST1024000000001    | DSMR     | 4.2.2   |
      | TEST1031000000001    | SMR      | 4.3     |
      | TEST1027000000001    | SMR      | 5.0.0   |
      | TEST1028000000001    | SMR      | 5.1     |
      | TEST1029000000001    | SMR      | 5.2     |
      | TEST1030000000001    | SMR      | 5.5     |