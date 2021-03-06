package com.yalin.googleio2016.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.model.ScheduleItem;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.settings.SettingsUtils;

/**
 * YaLin
 * 2016/11/23.
 */

public class UIUtils {
    private static final String TAG = "UIUtils";

    public static final String MOCK_DATA_PREFERENCES = "mock_data";
    public static final String PREFS_MOCK_CURRENT_TIME = "mock_current_time";
    public static final String PREFS_MOCK_APP_START_TIME = "mock_app_start_time";

    /**
     * Format and return the given session speakers and {@link ScheduleContract.Rooms} values.
     */
    public static String formatSessionSubtitle(String roomName, String speakerNames,
                                               Context context) {

        // Determine if the session is in the past
        if (roomName == null) {
            roomName = context.getString(R.string.unknown_room);
        }

        if (!TextUtils.isEmpty(speakerNames)) {
            return speakerNames + "\n" + roomName;
        } else {
            return roomName;
        }
    }

    public static boolean shouldShowLiveSessionsOnly(final Context context) {
        return !SettingsUtils.isAttendeeAtVenue(context)
                && TimeUtils.getCurrentTime(context) < Config.CONFERENCE_END_MILLIS;
    }

    /**
     * Queries the theme of the given {@code context} for a theme color.
     *
     * @param context            the context holding the current theme.
     * @param attrResId          the theme color attribute to resolve.
     * @param fallbackColorResId a color resource id tto fallback to if the theme color cannot be
     *                           resolved.
     * @return the theme color or the fallback color.
     */
    public static
    @ColorInt
    int getThemeColor(@NonNull Context context, @AttrRes int attrResId,
                      @ColorRes int fallbackColorResId) {
        final TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(attrResId, tv, true)) {
            return tv.data;
        }
        return ContextCompat.getColor(context, fallbackColorResId);
    }

    /**
     * @return If on SDK 17+, returns false if setting for animator duration scale is set to 0.
     * Returns true otherwise.
     */
    public static boolean animationEnabled(ContentResolver contentResolver) {
        boolean animationEnabled = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                if (Settings.Global.getFloat(contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE) == 0.0f) {
                    animationEnabled = false;

                }
            } catch (Settings.SettingNotFoundException e) {
                LogUtil.d(TAG, "Setting ANIMATOR_DURATION_SCALE not found");
            }
        }
        return animationEnabled;
    }

    public static Bitmap vectorToBitmap(@NonNull Context context, @DrawableRes int drawableResId) {
        VectorDrawableCompat vector = VectorDrawableCompat
                .create(context.getResources(), drawableResId, context.getTheme());
        if (vector == null) {
            return null;
        }
        final Bitmap bitmap = Bitmap.createBitmap(vector.getIntrinsicWidth(),
                vector.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        vector.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vector.draw(canvas);
        return bitmap;
    }

    private static final int[] RES_IDS_ACTION_BAR_SIZE = {R.attr.actionBarSize};

    /**
     * Calculates the Action Bar height in pixels.
     */
    public static int calculateActionBarSize(Context context) {
        if (context == null) {
            return 0;
        }

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();
        return (int) size;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRtl(final Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        } else {
            return context.getResources().getConfiguration().getLayoutDirection()
                    == View.LAYOUT_DIRECTION_RTL;
        }
    }

    public static
    @DrawableRes
    int getSessionIcon(int sessionType) {
        switch (sessionType) {
            case ScheduleItem.SESSION_TYPE_SESSION:
                return R.drawable.ic_session;
            case ScheduleItem.SESSION_TYPE_CODELAB:
                return R.drawable.ic_codelab;
            case ScheduleItem.SESSION_TYPE_BOXTALK:
                return R.drawable.ic_sandbox;
            case ScheduleItem.SESSION_TYPE_MISC:
            default:
                return R.drawable.ic_misc;
        }
    }

    // TODO: Improve the mapping of icons to breaks.
    // Initially this was a convenience method and there were few icons to be assigned to
    // breaks. The current implementation could be improved if the icon - break mapping
    // was defined via a configuration file and loaded at runtime. This would make the breaks
    // more flexible.
    public static
    @DrawableRes
    int getBreakIcon(String breakTitle) {
        if (!TextUtils.isEmpty(breakTitle)) {
            if (breakTitle.contains("After") || breakTitle.contains("Concert")) {
                return R.drawable.ic_after_hours;
            } else if (breakTitle.contains("Badge")) {
                return R.drawable.ic_badge_pickup;
            } else if (breakTitle.contains("Pre-Keynote")) {
                return R.drawable.ic_session;
            } else if (breakTitle.contains("Codelabs")) {
                return R.drawable.ic_codelab;
            } else if (breakTitle.contains("Sandbox") || breakTitle.contains("Office hours")) {
                return R.drawable.ic_sandbox;
            }
        }
        return R.drawable.ic_food;
    }
}
