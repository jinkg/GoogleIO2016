package com.yalin.googleio2016.util;

import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.settings.SettingsUtils;

import java.text.DateFormat;
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
    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

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
     * @return the name of the day at the given {@code position} in the {@link
     * Config#CONFERENCE_DAYS}. It is assumed that all days in {@link Config#CONFERENCE_DAYS} are
     * consecutive.
     */
    public static String getDayName(Context context, int position) {
        long day1Start = Config.CONFERENCE_DAYS[0][0];
        long day = 1000 * 60 * 60 * 24;
        return TimeUtils.formatShortDate(context, new Date(day1Start + day * position));
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

    /**
     * Format a {@code date} honoring the app preference for using Conference or device timezone.
     * {@code Context} is used to lookup the shared preference settings.
     */
    public static String formatShortDate(Context context, Date date) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        return DateUtils.formatDateRange(context, formatter, date.getTime(), date.getTime(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_NO_YEAR,
                SettingsUtils.getDisplayTimeZone(context).getID()).toString();
    }

    public static String formatShortTime(Context context, Date time) {
        // Android DateFormatter will honor the user's current settings.
        DateFormat format = android.text.format.DateFormat.getTimeFormat(context);
        // Override with Timezone based on settings since users can override their phone's timezone
        // with Pacific time zones.
        TimeZone tz = SettingsUtils.getDisplayTimeZone(context);
        if (tz != null) {
            format.setTimeZone(tz);
        }
        return format.format(time);
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
