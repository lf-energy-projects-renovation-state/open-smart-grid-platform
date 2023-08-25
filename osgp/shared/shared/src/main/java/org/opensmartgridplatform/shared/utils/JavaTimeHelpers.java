package org.opensmartgridplatform.shared.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.GregorianCalendar;

public class JavaTimeHelpers {

  public static long getMillisFrom(final Temporal datetime) {
    return datetime.get(ChronoField.MILLI_OF_SECOND);
  }

  public static String formatDate(final Date date, final DateTimeFormatter formatter) {
    return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()));
  }

  public static ZonedDateTime gregorianCalendarToZonedDateTime(
      final GregorianCalendar gregorianCalendar, final ZoneId zoneId) {
    return ZonedDateTime.ofInstant(gregorianCalendar.toInstant(), zoneId);
  }

  public static int getOffsetForZonedDateTimeInMillis(final ZonedDateTime dateTime) {
    return dateTime.getOffset().getTotalSeconds() * 1000;
  }

  public static boolean isDayLightSavingsActive(final ZonedDateTime dateTime) {
    return dateTime.getZone().getRules().isDaylightSavings(dateTime.toInstant());
  }
}
