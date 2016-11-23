package com.yalin.googleio2016.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.os.Build;

import com.yalin.googleio2016.R;

/**
 * YaLin
 * 2016/11/23.
 */

public class RecentTasksStyler {
    private static Bitmap sIcon = null;

    private RecentTasksStyler() {
    }

    public static void styleRecentTasksEntry(Activity activity) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }

        final String label = activity.getString(activity.getApplicationInfo().labelRes);
        final int colorPrimary =
                UIUtils.getThemeColor(activity, R.attr.colorPrimary, R.color.theme_primary);
        if (sIcon == null) {
            sIcon = UIUtils.vectorToBitmap(activity, R.drawable.ic_recents_logo);
        }
        activity.setTaskDescription(
                new ActivityManager.TaskDescription(label, sIcon, colorPrimary));
    }
}
