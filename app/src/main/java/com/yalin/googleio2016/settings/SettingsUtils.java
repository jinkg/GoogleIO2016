package com.yalin.googleio2016.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.ui.BaseActivity;
import com.yalin.googleio2016.welcome.WelcomeActivity;

/**
 * YaLin
 * 2016/11/23.
 */

public class SettingsUtils {
    public static final String CONFERENCE_YEAR_PREF_POSTFIX = "_2016";

    public static final String PREF_TOS_ACCEPTED = "pref_tos_accepted" +
            CONFERENCE_YEAR_PREF_POSTFIX;

    /**
     * Boolean indicating whether the app has performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done" +
            CONFERENCE_YEAR_PREF_POSTFIX;

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
}
