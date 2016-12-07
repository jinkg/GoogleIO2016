package com.yalin.googleio2016.util;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * YaLin
 * 2016/11/23.
 */

@TargetApi(21)
public class LUtils {

    private static Typeface sMediumTypeface;

    private LUtils(AppCompatActivity appCompatActivity) {
    }

    public static LUtils getInstance(AppCompatActivity activity) {
        return new LUtils(activity);
    }

    private static boolean hasL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public void setMediumTypeface(TextView textView) {
        if (hasL()) {
            if (sMediumTypeface == null) {
                sMediumTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL);
            }

            textView.setTypeface(sMediumTypeface);
        } else {
            textView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        }
    }
}
