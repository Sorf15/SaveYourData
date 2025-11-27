package com.sorf.syd.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     *
     * @return formatted time: "14:05:24"
     */
    public static String getTime() {
        return String.format("%tT", new Date());
    }

    public static String getTime(Date date) {
        return String.format("%tT", date);
    }

    public static String getFullTime() {
        return format.format(new Date());
    }

    public static String getFullTime(Date date) {
        return format.format(date);
    }
}
