package com.yalin.googleio2016.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.ui.BaseActivity;
import com.yalin.googleio2016.util.TimeUtils;
import com.yalin.googleio2016.welcome.WelcomeActivity;

import java.util.TimeZone;

/**
 * YaLin
 * 2016/11/23.
 */

public class SettingsUtils {
    public static final String CONFERENCE_YEAR_PREF_POSTFIX = "_2016";

    /**
     * Boolean preference indicating the user would like to see times in their local timezone
     * throughout the app.
     */
    public static final String PREF_LOCAL_TIMES = "pref_local_times";

    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating whether the app has performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Long storing the sync interval that's currently configured.
     */
    public static final String PREF_CUR_SYNC_INTERVAL = "pref_cur_sync_interval";

    /**
     * Long indicating when a sync was last ATTEMPTED (not necessarily succeeded).
     */
    public static final String PREF_LAST_SYNC_ATTEMPTED = "pref_last_sync_attempted";

    /**
     * Long indicating when a sync last SUCCEEDED.
     */
    public static final String PREF_LAST_SYNC_SUCCEEDED = "pref_last_sync_succeeded";

    /**
     * Long storing the sync interval that's currently configured.
     */

    /**
     * Boolean indicating whether ToS has been accepted.
     */
    public static final String PREF_DECLINED_WIFI_SETUP = "pref_declined_wifi_setup" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Return the {@link TimeZone} the app is set to use (either user or conference).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static TimeZone getDisplayTimeZone(Context context) {
        TimeZone defaultTz = TimeZone.getDefault();
        return (isUsingLocalTime(context) && defaultTz != null)
                ? defaultTz : Config.CONFERENCE_TIMEZONE;
    }

    /**
     * Return true if the user has indicated they want the schedule in local times, false if they
     * want to use the conference time zone. This preference is enabled/disabled by the user in the
     * {@link SettingsActivity}.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isUsingLocalTime(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_LOCAL_TIMES, false);
    }

    /**
     * Return true when the {@code R.raw.bootstrap_data_json bootstrap data} has been marked loaded.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(BuildConfig.PREF_DATA_BOOTSTRAP_DONE, false);
    }

    /**
     * Mark that the app has finished loading the {@code R.raw.bootstrap_data bootstrap data}.
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markDataBootstrapDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(BuildConfig.PREF_DATA_BOOTSTRAP_DONE, true).apply();
    }

    /**
     * Return true if user has accepted the
     * {@link WelcomeActivity Tos}, false if they haven't (yet).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isTosAccepted(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_TOS_ACCEPTED, false);
    }

    /**
     * Return true if the user has indicated they're attending I/O in person. This preference can be
     * enabled/disabled by the user in the
     * {@link SettingsActivity}.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isAttendeeAtVenue(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(BuildConfig.PREF_ATTENDEE_AT_VENUE, true);
    }

    /**
     * Mark {@code newValue whether} the user has accepted the TOS so the app doesn't ask again.
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markTosAccepted(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_TOS_ACCEPTED, newValue).apply();
    }

    /**
     * Return true if the first-app-run-activities have already been executed.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean isFirstRunProcessComplete(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    /**
     * Mark {@code newValue whether} this is the first time the first-app-run-processes have run.
     * Managed by {@link BaseActivity the}
     * {@link BaseActivity two} base activities.
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markFirstRunProcessesDone(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, newValue).apply();
    }

    /**
     * Return a long representing the last time a sync was attempted (regardless of success).
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getLastSyncAttemptedTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_ATTEMPTED, 0L);
    }

    /**
     * Return a long representing the last time a sync succeeded.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getLastSyncSucceededTime(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_LAST_SYNC_SUCCEEDED, 0L);
    }

    /**
     * Return a long representing the current data sync interval time.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static long getCurSyncInterval(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getLong(PREF_CUR_SYNC_INTERVAL, 0L);
    }

    /**
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param show    Whether app should show session reminders
     */
    public static void setShowSessionReminders(final Context context, boolean show) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(BuildConfig.PREF_SESSION_REMINDERS_ENABLED, show).apply();
    }

    /**
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param show    Whether app should show session feedback reminders
     */
    public static void setShowSessionFeedbackReminders(final Context context, boolean show) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(BuildConfig.PREF_SESSION_FEEDBACK_REMINDERS_ENABLED, show).apply();
    }

    /**
     * Return true if user has already declined WiFi setup, but false if they haven't yet.
     *
     * @param context Context to be used to lookup the {@link android.content.SharedPreferences}.
     */
    public static boolean hasDeclinedWifiSetup(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_DECLINED_WIFI_SETUP, false);
    }

    /**
     * Mark that the user has explicitly declined WiFi setup assistance.
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void markDeclinedWifiSetup(final Context context, boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_DECLINED_WIFI_SETUP, newValue).apply();
    }

    /**
     * Mark a sync was attempted (stores current time as 'last sync attempted' preference).
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markSyncAttemptedNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_ATTEMPTED, TimeUtils.getCurrentTime(context)).apply();
    }

    /**
     * Mark that a sync succeeded (stores current time as 'last sync succeeded' preference).
     *
     * @param context Context to be used to edit the {@link android.content.SharedPreferences}.
     */
    public static void markSyncSucceededNow(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_LAST_SYNC_SUCCEEDED, TimeUtils.getCurrentTime(context)).apply();
    }

    /**
     * Set a new interval for the data sync time.
     *
     * @param context  Context to be used to edit the {@link android.content.SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void setCurSyncInterval(final Context context, long newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putLong(PREF_CUR_SYNC_INTERVAL, newValue).apply();
    }
}
