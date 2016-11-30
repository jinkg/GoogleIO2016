package com.yalin.googleio2016.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.settings.SettingsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

/**
 * YaLin
 * 2016/11/29.
 */

public class TimeUtils {
    private static final SimpleDateFormat[] ACCEPTED_TIMESTAMP_FORMATS = {
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z", Locale.US)
    };

    public static Date parseTimestamp(String timestamp) {
        for (SimpleDateFormat format : ACCEPTED_TIMESTAMP_FORMATS) {
            // TODO: We shouldn't be forcing the time zone when parsing dates.
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                return format.parse(timestamp);
            } catch (ParseException ex) {
                // ignore
            }
        }

        // All attempts to parse have failed
        return null;
    }

    public static long timestampToMillis(String timestamp, long defaultValue) {
        if (TextUtils.isEmpty(timestamp)) {
            return defaultValue;
        }
        Date d = parseTimestamp(timestamp);
        return d == null ? defaultValue : d.getTime();
    }

    /**
     * Retrieve the current time. If the current build is a debug build, the mock time is returned
     * when set, taking into account the passage of time by adding the difference between the
     * current system time and the system time when the application was created.
     */
    public static long getCurrentTime(final Context context) {
        if (BuildConfig.DEBUG) {
            return context.getSharedPreferences(UIUtils.MOCK_DATA_PREFERENCES, Context.MODE_PRIVATE)
                    .getLong(UIUtils.PREFS_MOCK_CURRENT_TIME, System.currentTimeMillis())
                    + System.currentTimeMillis() - getAppStartTime(context);
        } else {
            return System.currentTimeMillis();
        }
    }

    /**
     * Retrieve the app start time,set when the application was created. This is used to calculate
     * the current time, in debug mode only.
     */
    private static long getAppStartTime(final Context context) {
        return context.getSharedPreferences(UIUtils.MOCK_DATA_PREFERENCES, Context.MODE_PRIVATE)
                .getLong(UIUtils.PREFS_MOCK_APP_START_TIME, System.currentTimeMillis());
    }

    public static String formatShortDateTime(Context context, Date date) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        return DateUtils.formatDateRange(context, formatter, date.getTime(), date.getTime(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_SHOW_TIME,
                SettingsUtils.getDisplayTimeZone(context).getID()).toString().toUpperCase();
    }

}
