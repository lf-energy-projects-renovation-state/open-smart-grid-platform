// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.datadecoder.dlmsclassdatadecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.utils.DlmsHelper;
import org.opensmartgridplatform.adapter.protocol.dlms.domain.datadecoder.BasicDlmsDataDecoder;
import org.opensmartgridplatform.dlms.enums.ClockBase;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.ClockStatusBitDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service(value = "timeAndEventsClassesDecoder")
public class TimeAndEventsClassesDecoder {

  // This class contains decoding functions for the attributes of the DLMS classes in DLMS Blue
  // book chapter "Interface classes for time- and event bound control".

  private static final String ARRAY_EXPECTED_BUT_FOUND = "ARRAY expected, but found ";
  private static final String STRUCTURE_EXPECTED_BUT_FOUND = "STRUCTURE expected, but found ";

  private final DlmsHelper dlmsHelper;
  private final BasicDlmsDataDecoder basicDlmsDataDecoder;

  @Autowired
  public TimeAndEventsClassesDecoder(
      final DlmsHelper dlmsHelper, final BasicDlmsDataDecoder basicDlmsDataDecoder) {
    this.dlmsHelper = dlmsHelper;
    this.basicDlmsDataDecoder = basicDlmsDataDecoder;
  }

  // Class-id 8: Clock attribute 4
  public String decodeClockStatus(final DataObject attributeData) {
    try {
      final int value = this.dlmsHelper.readInteger(attributeData, "read clock status");
      final Set<ClockStatusBitDto> statusBits = ClockStatusBitDto.forClockStatus(value);
      if (statusBits != null && !statusBits.isEmpty()) {
        return "status: "
            + statusBits.stream()
                .map(ClockStatusBitDto::getDescription)
                .collect(Collectors.joining(", "));
      } else {
        return "no status set";
      }
    } catch (final Exception e) {
      return "decoding clock base failed: " + e.getMessage();
    }
  }

  // Class-id 8: Clock attribute 9
  public String decodeClockBase(final DataObject attributeData) {
    try {
      final int value = this.dlmsHelper.readInteger(attributeData, "read clock base");
      return ClockBase.getByValue(value).name();
    } catch (final Exception e) {
      return "decoding clock base failed: " + e.getMessage();
    }
  }

  // Class-id 11: Special days table attribute 2
  public String decodeSpecialDays(final DataObject attributeData) {
    final List<String> specialDaysDecoded = new ArrayList<>();

    try {
      if (attributeData.getType() == Type.ARRAY) {
        final List<DataObject> specialDays = attributeData.getValue();
        for (final DataObject specialDay : specialDays) {
          specialDaysDecoded.add(this.decodeSpecialDay(specialDay));
        }
        return String.join(System.lineSeparator(), specialDaysDecoded);
      } else {
        return ARRAY_EXPECTED_BUT_FOUND + attributeData.getType().name();
      }
    } catch (final Exception e) {
      return "decoding special days failed: " + e.getMessage();
    }
  }

  private String decodeSpecialDay(final DataObject specialDay) {
    try {
      if (specialDay.getType() == Type.STRUCTURE) {
        final List<DataObject> fields = specialDay.getValue();
        final int index = this.dlmsHelper.readInteger(fields.get(0), "read special day index");
        final String date = this.basicDlmsDataDecoder.decodeDate(fields.get(1));
        final int day_id = this.dlmsHelper.readInteger(fields.get(2), "read special day id");

        return index + ". (id " + day_id + "): " + date;
      } else {
        return STRUCTURE_EXPECTED_BUT_FOUND + specialDay.getType().name();
      }
    } catch (final Exception e) {
      return "decoding special day failed: " + e.getMessage();
    }
  }

  // Class-id 20: Activity calendar attribute 3 and 7
  public String decodeSeasonProfiles(final DataObject attributeData) {
    final List<String> seasonProfilesDecoded = new ArrayList<>();

    try {
      if (attributeData.getType() == Type.ARRAY) {
        final List<DataObject> seasonProfiles = attributeData.getValue();
        for (final DataObject seasonProfile : seasonProfiles) {
          seasonProfilesDecoded.add(this.decodeSeasonProfile(seasonProfile));
        }
        return String.join(System.lineSeparator(), seasonProfilesDecoded);
      } else {
        return ARRAY_EXPECTED_BUT_FOUND + attributeData.getType().name();
      }
    } catch (final Exception e) {
      return "decoding season profiles failed: " + e.getMessage();
    }
  }

  private String decodeSeasonProfile(final DataObject seasonProfile) {
    try {
      if (seasonProfile.getType() == Type.STRUCTURE) {
        final List<DataObject> fields = seasonProfile.getValue();
        final String name = new String((byte[]) fields.get(0).getValue());
        final String start =
            this.dlmsHelper
                .readDateTime(fields.get(1), "read season start")
                .asLocalDateTime()
                .toString();
        final String weekName = new String((byte[]) fields.get(2).getValue());

        return name + ": " + start + ", weekName: " + weekName;
      } else {
        return STRUCTURE_EXPECTED_BUT_FOUND + seasonProfile.getType().name();
      }
    } catch (final Exception e) {
      return "decoding season profile failed: " + e.getMessage();
    }
  }

  // Class-id 20: Activity calendar attribute 4 and 8
  public String decodeWeekProfiles(final DataObject attributeData) {
    final List<String> weekProfilesDecoded = new ArrayList<>();

    try {
      if (attributeData.getType() == Type.ARRAY) {
        final List<DataObject> weekProfiles = attributeData.getValue();
        for (final DataObject weekProfile : weekProfiles) {
          weekProfilesDecoded.add(this.decodeWeekProfile(weekProfile));
        }
        return String.join(System.lineSeparator(), weekProfilesDecoded);
      } else {
        return ARRAY_EXPECTED_BUT_FOUND + attributeData.getType().name();
      }
    } catch (final Exception e) {
      return "decoding week profiles failed: " + e.getMessage();
    }
  }

  private String decodeWeekProfile(final DataObject weekProfile) {
    try {
      if (weekProfile.getType() == Type.STRUCTURE) {
        final List<DataObject> fields = weekProfile.getValue();
        final String name = new String((byte[]) fields.get(0).getValue());
        final int monday = this.dlmsHelper.readInteger(fields.get(1), "read Monday");
        final int tuesday = this.dlmsHelper.readInteger(fields.get(2), "read Tuesday");
        final int wednesday = this.dlmsHelper.readInteger(fields.get(3), "read Wednesday");
        final int thursday = this.dlmsHelper.readInteger(fields.get(4), "read Thursday");
        final int friday = this.dlmsHelper.readInteger(fields.get(5), "read Friday");
        final int saturday = this.dlmsHelper.readInteger(fields.get(6), "read Saturday");
        final int sunday = this.dlmsHelper.readInteger(fields.get(7), "read Sunday");
        return name
            + ": Monday "
            + monday
            + ", Tuesday "
            + tuesday
            + ", Wednesday "
            + wednesday
            + ", Thursday "
            + thursday
            + ", Friday "
            + friday
            + ", Saturday "
            + saturday
            + ", Sunday "
            + sunday;
      } else {
        return STRUCTURE_EXPECTED_BUT_FOUND + weekProfile.getType().name();
      }
    } catch (final Exception e) {
      return "decoding week profile failed: " + e.getMessage();
    }
  }

  // Class-id 20: Activity calendar attribute 5 and 9
  public String decodeDayProfiles(final DataObject attributeData) {
    final List<String> dayProfilesDecoded = new ArrayList<>();

    try {
      if (attributeData.getType() == Type.ARRAY) {
        final List<DataObject> dayProfiles = attributeData.getValue();
        for (final DataObject dayProfile : dayProfiles) {
          dayProfilesDecoded.add(this.decodeDayProfile(dayProfile));
        }
        return String.join(System.lineSeparator(), dayProfilesDecoded);
      } else {
        return ARRAY_EXPECTED_BUT_FOUND + attributeData.getType().name();
      }
    } catch (final Exception e) {
      return "decoding day profiles failed: " + e.getMessage();
    }
  }

  private String decodeDayProfile(final DataObject dayProfile) {
    try {
      if (dayProfile.getType() == Type.STRUCTURE) {
        final List<DataObject> fields = dayProfile.getValue();
        final int id = this.dlmsHelper.readInteger(fields.get(0), "read day profile id");
        final String schedule = this.decodeDaySchedule(fields.get(1));
        return id + ": " + schedule;
      } else {
        return STRUCTURE_EXPECTED_BUT_FOUND + dayProfile.getType().name();
      }
    } catch (final Exception e) {
      return "decoding day profile failed: " + e.getMessage();
    }
  }

  private String decodeDaySchedule(final DataObject daySchedule) {
    final List<String> dayDayProfileActionsDecoded = new ArrayList<>();

    try {
      if (daySchedule.getType() == Type.ARRAY) {
        final List<DataObject> dayProfileActions = daySchedule.getValue();
        for (final DataObject dayProfileAction : dayProfileActions) {
          dayDayProfileActionsDecoded.add(this.decodeDayProfileAction(dayProfileAction));
        }
        return String.join(", ", dayDayProfileActionsDecoded);
      } else {
        return ARRAY_EXPECTED_BUT_FOUND + daySchedule.getType().name();
      }
    } catch (final Exception e) {
      return "decoding day profile actions failed: " + e.getMessage();
    }
  }

  private String decodeDayProfileAction(final DataObject dayProfileAction) {
    try {
      if (dayProfileAction.getType() == Type.STRUCTURE) {
        final List<DataObject> fields = dayProfileAction.getValue();
        final String startTime = this.basicDlmsDataDecoder.decodeTime(fields.get(0));
        final String logicalName =
            this.dlmsHelper.readLogicalName(fields.get(1), "read logical name").toDsmrString();
        final int selector = this.dlmsHelper.readInteger(fields.get(2), "read selector");
        return startTime + " " + logicalName + " selector " + selector;
      } else {
        return STRUCTURE_EXPECTED_BUT_FOUND + dayProfileAction.getType().name();
      }
    } catch (final Exception e) {
      return "decoding day profile action failed: " + e.getMessage();
    }
  }
}
