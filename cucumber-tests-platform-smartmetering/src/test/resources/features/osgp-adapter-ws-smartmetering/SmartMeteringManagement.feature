@SmartMetering @Platform
Feature: SmartMetering Management
  As a grid operator
  I want to be able to perform SmartMeteringManagement operations on a device
  In order to ...

  Background: 
    Given a dlms device
      | DeviceIdentification | TEST1024000000001 |
      | DeviceType           | SMART_METER_E     |

  Scenario: find standard events from a device for a period without events
    When receiving a find standard events request
      | DeviceIdentification | TEST1024000000001        |
      | BeginDate            | 2015-08-15T00:00:00.000Z |
      | EndDate              | 2015-08-25T00:00:00.000Z |
    Then 0 standard events should be returned
      | DeviceIdentification | TEST1024000000001 |
      | Result               | OK                |

  Scenario: find standard events from a device within a period
    When receiving a find standard events request
      | DeviceIdentification | TEST1024000000001        |
      | BeginDate            | 2015-09-01T00:00:00.000Z |
      | EndDate              | 2015-09-05T00:00:00.000Z |
    Then 21 standard events should be returned
      | DeviceIdentification | TEST1024000000001 |
      | Result               | OK                |

  Scenario: find fraud events from a device
    When receiving a find fraud events request
      | DeviceIdentification | TEST1024000000001        |
      | BeginDate            | 2014-09-02T00:00:00.000Z |
      | EndDate              | 2015-09-03T00:00:00.000Z |
    Then 9 fraud events should be returned
      | DeviceIdentification | TEST1024000000001 |
      | Result               | OK                |

  Scenario: find communication events from a device
    When receiving a find communication events request
      | DeviceIdentification | TEST1024000000001        |
      | BeginDate            | 2014-09-02T00:00:00.000Z |
      | EndDate              | 2015-09-03T00:00:00.000Z |
    Then 9 communication events should be returned
      | DeviceIdentification | TEST1024000000001 |
      | Result               | OK                |

  Scenario: find mbus events from a device
    When receiving a find mbus events request
      | DeviceIdentification | TEST1024000000001        |
      | BeginDate            | 2015-09-01T00:00:00.000Z |
      | EndDate              | 2015-09-05T00:00:00.000Z |
    Then 29 mbus events should be returned
      | DeviceIdentification | TEST1024000000001 |
      | Result               | OK                |
