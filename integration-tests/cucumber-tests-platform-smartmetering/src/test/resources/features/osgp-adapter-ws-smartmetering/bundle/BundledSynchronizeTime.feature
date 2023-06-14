# SPDX-FileCopyrightText: Contributors to the GXF project
#
# SPDX-License-Identifier: Apache-2.0

@SmartMetering @Platform 
Feature: SmartMetering Bundle - SynchronizeTime
  As a grid operator 
  I want to be able to synchronize time on a meter via a bundle request

  Background: 
    Given a dlms device
      | DeviceIdentification | TEST1024000000001 |
      | DeviceType           | SMART_METER_E     |

  Scenario: Synchronize time
    Given a bundle request
      | DeviceIdentification | TEST1024000000001 |
    And the bundle request contains a valid synchronize time action for timezone "Europe/Amsterdam"
    When the bundle request is received
    Then the bundle response should contain a synchronize time response with values
      | Result | OK |
