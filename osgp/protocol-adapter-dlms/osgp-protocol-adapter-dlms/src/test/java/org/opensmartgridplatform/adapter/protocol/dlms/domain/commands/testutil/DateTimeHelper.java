// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0

package org.opensmartgridplatform.adapter.protocol.dlms.domain.commands.testutil;

import java.util.Date;
import org.joda.time.DateTime;
import org.openmuc.jdlms.datatypes.CosemDateTime;
import org.openmuc.jdlms.datatypes.DataObject;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.CosemDateDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.CosemDateTimeDto;
import org.opensmartgridplatform.dto.valueobjects.smartmetering.CosemTimeDto;

public class DateTimeHelper {

  // Compares date with cosemDateTime. Note: cosemDateTime uses hundredths and not milliseconds
  public static boolean areDatesEqual(final Date date, final CosemDateTimeDto cosemDateTime) {
    final DateTime dateTime = new DateTime(date);
    final CosemDateDto cosemDate = cosemDateTime.getDate();
    final CosemTimeDto cosemTime = cosemDateTime.getTime();

    return (dateTime.getYear() == cosemDate.getYear()
        && dateTime.getMonthOfYear() == cosemDate.getMonth()
        && dateTime.getDayOfMonth() == cosemDate.getDayOfMonth()
        && dateTime.getHourOfDay() == cosemTime.getHour()
        && dateTime.getMinuteOfHour() == cosemTime.getMinute()
        && dateTime.getSecondOfMinute() == cosemTime.getSecond()
        && dateTime.getMillisOfSecond() == cosemTime.getHundredths() * 10);
  }

  public static DataObject getDateAsOctetString(final int year, final int month, final int day) {
    final CosemDateTime dateTime = new CosemDateTime(year, month, day, 0, 0, 0, 0);

    return DataObject.newOctetStringData(dateTime.encode());
  }
}
