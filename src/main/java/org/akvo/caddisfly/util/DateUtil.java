package org.akvo.caddisfly.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtil {

    public static Date convertStringToDate(String dateString, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.US);
        try {
            return simpleDateFormat.parse(dateString.trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
