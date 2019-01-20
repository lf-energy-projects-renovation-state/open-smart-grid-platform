@PublicLighting @Platform @PublicLightingSetLightSchedule
Feature: PublicLightingScheduleManagement Set Light Schedule
  In order to ... 
  As a platform 
  I want to ...

  @OslpMockServer
  Scenario Outline: Set light schedule
    Given an ssld oslp device
      | DeviceIdentification | TEST1024000000001 |
      | Protocol             | <Protocol>        |
    And the device returns a set light schedule response "OK" over "<Protocol>"
    When receiving a set light schedule request
      | DeviceIdentification | TEST1024000000001 |
      | WeekDay              | <WeekDay>         |
      | StartDay             | <StartDay>        |
      | EndDay               | <EndDay>          |
      | ActionTime           | <ActionTime>      |
      | Time                 | <Time>            |
      | LightValues          | <LightValues>     |
      | TriggerType          | <TriggerType>     |
      | TriggerWindow        | <TriggerWindow>   |
    Then the set light schedule async response contains
      | DeviceIdentification | TEST1024000000001 |
    And a set light schedule "<Protocol>" message is sent to device "TEST1024000000001"
      | WeekDay       | <WeekDay>       |
      | StartDay      | <StartDay>      |
      | EndDay        | <EndDay>        |
      | ActionTime    | <ActionTime>    |
      | Time          | <Time>          |
      | LightValues   | <LightValues>   |
      | TriggerType   | <TriggerType>   |
      | TriggerWindow | <TriggerWindow> |
    And the platform buffers a set light schedule response message for device "TEST1024000000001"
      | Result | OK |

    Examples: 
      | Protocol    | WeekDay     | StartDay   | EndDay     | ActionTime   | Time         | TriggerWindow | LightValues       | TriggerType   |
      | OSLP ELSTER | MONDAY      |            |            | ABSOLUTETIME | 18:00:00.000 |               | 0,true,           |               |
      | OSLP ELSTER | MONDAY      |            |            | ABSOLUTETIME | 18:00:00.000 |               | 1,true,;2,true,50 |               |
      | OSLP ELSTER | ABSOLUTEDAY | 2013-03-01 |            | ABSOLUTETIME | 18:00:00.000 |               | 0,true,           |               |
      | OSLP ELSTER | MONDAY      |            |            | SUNSET       |              |         30,30 | 0,true,           | LIGHT_TRIGGER |
      | OSLP ELSTER | ABSOLUTEDAY | 2016-01-01 | 2016-12-31 | ABSOLUTETIME | 18:00:00.000 |               | 0,true,           |               |


  #Note: the astronomical offsets are part of the set schedule request in the web services, 
  #      while in the oslp elster protocol adapter they are sent to the device using a set configuration message
  @OslpMockServer
  Scenario: Set light schedule with astronomical offsets
    Given an ssld oslp device
      | DeviceIdentification | TEST1024000000001 |
      | Protocol             | OSLP ELSTER       |
    And the device returns a get configuration status over "OSLP_ELSTER"
      | Status            | OK          |
      | LightType         | RELAY       |
      | DcLights          |             |
      | DcMap             |             |
      | RelayConf         |             |
      | PreferredLinkType |             |
      | MeterType         | AUX         |
      | ShortInterval     |             |
      | LongInterval      |             |
      | IntervalType      |             |
      | OsgpIpAddress     | 10.20.30.40 |
      | OsgpPort          | 12122       |
    And the device returns a set configuration status "OK" over "OSLP ELSTER"
    And the device returns a set light schedule response "OK" over "OSLP ELSTER"
    When receiving a set light schedule request with astronomical offsets
      | DeviceIdentification | TEST1024000000001 |
      | SunriseOffset        |               -15 |
      | SunsetOffset         |                45 |
    Then the set light schedule async response contains
      | DeviceIdentification | TEST1024000000001 |
    And a get configuration "OSLP ELSTER" message is sent to device "TEST1024000000001"
    And a set configuration "OSLP ELSTER" message is sent to device "TEST1024000000001"
      | SunriseOffset | -900  |
      | SunsetOffset  |  2700 |
    And a set light schedule "OSLP ELSTER" message is sent to device "TEST1024000000001"
      | WeekDay       | ALL          |
      | ActionTime    | SUNRISE      |
      | LightValues   | 0,false,     |
      | TriggerType   | ASTRONOMICAL |
    And the platform buffers a set light schedule response message for device "TEST1024000000001"
      | Result | OK |



  @OslpMockServer
  Scenario Outline: Failed set light schedule
    Given an ssld oslp device
      | DeviceIdentification | TEST1024000000001 |
      | Protocol             | <Protocol>        |
    And the device returns a set light schedule response "FAILURE" over "<Protocol>"
    When receiving a set light schedule request
      | DeviceIdentification | TEST1024000000001 |
      | WeekDay              | MONDAY            |
      | StartDay             |                   |
      | EndDay               |                   |
      | ActionTime           | ABSOLUTETIME      |
      | Time                 | 18:00:00.000      |
      | LightValues          | 0,true,           |
      | TriggerType          |                   |
      | TriggerWindow        |                   |
    Then the set light schedule async response contains
      | DeviceIdentification | TEST1024000000001 |
    And a set light schedule "<Protocol>" message is sent to device "TEST1024000000001"
      | WeekDay       | MONDAY       |
      | StartDay      |              |
      | EndDay        |              |
      | ActionTime    | ABSOLUTETIME |
      | Time          | 18:00:00.000 |
      | LightValues   | 0,true,      |
      | TriggerType   |              |
      | TriggerWindow |              |
    # Note: The platform throws a TechnicalException when the status is 'FAILURE'.
    And the platform buffers a set light schedule response message for device "TEST1024000000001" contains soap fault
      | Message | Device reports failure |

    Examples: 
      | Protocol    |
      | OSLP ELSTER |

  @OslpMockServer
  Scenario Outline: Rejected set light schedule
    Given an ssld oslp device
      | DeviceIdentification | TEST1024000000001 |
      | Protocol             | <Protocol>        |
    And the device returns a set light schedule response "REJECTED" over "<Protocol>"
    When receiving a set light schedule request
      | DeviceIdentification | TEST1024000000001 |
      | WeekDay              | MONDAY            |
      | StartDay             |                   |
      | EndDay               |                   |
      | ActionTime           | ABSOLUTETIME      |
      | Time                 | 18:00:00.000      |
      | LightValues          | 0,true,           |
      | TriggerType          |                   |
      | TriggerWindow        |                   |
    Then the set light schedule async response contains
      | DeviceIdentification | TEST1024000000001 |
    And a set light schedule "<Protocol>" message is sent to device "TEST1024000000001"
      | WeekDay       | MONDAY       |
      | StartDay      |              |
      | EndDay        |              |
      | ActionTime    | ABSOLUTETIME |
      | Time          | 18:00:00.000 |
      | LightValues   | 0,true,      |
      | TriggerType   |              |
      | TriggerWindow |              |
    # Note: The platform throws a TechnicalException when the status is 'REJECTED'.
    And the platform buffers a set light schedule response message for device "TEST1024000000001" contains soap fault
      | Message | Device reports rejected |

    Examples: 
      | Protocol    |
      | OSLP ELSTER |

  Scenario Outline: Set light schedule with invalid schedule
    Given an ssld device
      | DeviceIdentification | TEST1024000000001 |
    When receiving a set light schedule request
      | DeviceIdentification | TEST1024000000001 |
      | WeekDay              | <WeekDay>         |
      | StartDay             |                   |
      | EndDay               |                   |
      | ActionTime           | <ActionTime>      |
      | Time                 | <Time>            |
      | LightValues          | 0,true,           |
      | TriggerType          | <TriggerType>     |
      | TriggerWindow        |                   |
    Then the set light schedule response contains soap fault
      | FaultCode    | SOAP-ENV:Server  |
      | FaultString  | VALIDATION_ERROR |
      | InnerMessage | <Message>        |

    Examples: 
      | WeekDay     | ActionTime   | Time         | TriggerType   | Message                                                                                                      |
      | ABSOLUTEDAY | ABSOLUTETIME | 18:00:00.000 |               | Validation Exception, violations: startDay may not be null when weekDay is set to ABSOLUTEDAY;               |
      | MONDAY      | SUNRISE      |              | LIGHT_TRIGGER | Validation Exception, violations: triggerWindow may not be null when actionTime is set to SUNRISE or SUNSET and triggerType is LIGHT_TRIGGER; |
      | MONDAY      | SUNSET       |              | LIGHT_TRIGGER | Validation Exception, violations: triggerWindow may not be null when actionTime is set to SUNRISE or SUNSET and triggerType is LIGHT_TRIGGER; |

  # Note: Result is 'NOT_FOUND' because there isn't a record in the database with a CorrelationUID
  # Note: HasScheduled is set to 'false' because the response type is 'NOT_OK', but should be 'OK'
  @OslpMockServer
  Scenario Outline: Set light schedule with 50 schedules # Success
    Given an ssld oslp device
      | DeviceIdentification | TEST1024000000001 |
      | Protocol             | <Protocol>        |
    And the device returns a set light schedule response "OK" over "<Protocol>"
    When receiving a set light schedule request for 50 schedules
      | DeviceIdentification | TEST1024000000001 |
      | WeekDay              | <WeekDay>         |
      | StartDay             | <StartDay>        |
      | EndDay               | <EndDay>          |
      | ActionTime           | <ActionTime>      |
      | Time                 | <Time>            |
      | LightValues          | <LightValues>     |
      | TriggerType          | <TriggerType>     |
      | TriggerWindow        | <TriggerWindow>   |
    Then the set light schedule async response contains
      | DeviceIdentification | TEST1024000000001 |
    And a set light schedule "<Protocol>" message is sent to device "TEST1024000000001"
      | WeekDay       | <WeekDay>       |
      | StartDay      | <StartDay>      |
      | EndDay        | <EndDay>        |
      | ActionTime    | <ActionTime>    |
      | Time          | <Time>          |
      | LightValues   | <LightValues>   |
      | TriggerType   | <TriggerType>   |
      | TriggerWindow | <TriggerWindow> |
      | ScheduledTime | <ScheduledTime> |
    And the platform buffers a set light schedule response message for device "TEST1024000000001"
      | Result | NOT_FOUND |

    Examples: 
      | Protocol    | WeekDay     | StartDay   | EndDay     | ScheduledTime | ActionTime   | Time         | TriggerWindow | LightValues | TriggerType   |
      | OSLP ELSTER | ABSOLUTEDAY | 2016-01-01 | 2016-12-31 | 2016-12-15    | ABSOLUTETIME | 18:00:00.000 |         30,30 | 0,true,     | LIGHT_TRIGGER |

  Scenario Outline: Set light schedule with 51 schedules # Fail
    Given an ssld oslp device
      | DeviceIdentification | TEST1024000000001 |
    When receiving a set light schedule request for 51 schedules
      | DeviceIdentification | TEST1024000000001 |
      | WeekDay              | <WeekDay>         |
      | StartDay             | <StartDay>        |
      | EndDay               | <EndDay>          |
      | ActionTime           | <ActionTime>      |
      | Time                 | <Time>            |
      | LightValues          | <LightValues>     |
      | TriggerType          | <TriggerType>     |
      | TriggerWindow        | <TriggerWindow>   |
      | ScheduledTime        | 2016-12-15        |
    Then the set light schedule response contains soap fault
      | FaultCode        | SOAP-ENV:Client                                                                                                                                                                                                   |
      | FaultString      | Validation error                                                                                                                                                                                                  |
      | ValidationErrors | cvc-complex-type.2.4.a: Invalid content was found starting with element 'ns2:Schedules'. One of '{"http://www.opensmartgridplatform.org/schemas/publiclighting/schedulemanagement/2014/10":scheduled_time}' is expected. |

    Examples: 
      | WeekDay     | StartDay   | EndDay     | ActionTime   | Time         | TriggerWindow | LightValues | TriggerType   |
      | ABSOLUTEDAY | 2016-01-01 | 2016-12-31 | ABSOLUTETIME | 18:00:00.000 |         30,30 | 0,true,     | LIGHT_TRIGGER |