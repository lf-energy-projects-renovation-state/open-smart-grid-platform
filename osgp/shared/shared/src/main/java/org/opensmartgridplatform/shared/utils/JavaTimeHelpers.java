package org.opensmartgridplatform.shared.utils;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Date;

public class JavaTimeHelpers {

  public static long getMillisFrom(final Temporal datetime) {
    return datetime.get(ChronoField.MILLI_OF_SECOND);
  }

  public static String formatDate(final Date date, final String format) {
    return DateTimeFormatter.ofPattern(format)
        .format(date.toInstant().atZone(ZoneId.systemDefault()));
  }
}
