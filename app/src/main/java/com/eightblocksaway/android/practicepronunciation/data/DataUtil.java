package com.eightblocksaway.android.practicepronunciation.data;

import android.text.format.Time;

public class DataUtil {

    public static final String SYLLABLE_SEPARATOR = "-";
    public static final String DEFINITION_SEPARATOR = " ### ";

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.setToNow();
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }
}
