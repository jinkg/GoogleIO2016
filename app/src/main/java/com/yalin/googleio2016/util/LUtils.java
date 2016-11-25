package com.yalin.googleio2016.util;

import android.annotation.TargetApi;
import android.support.v7.app.AppCompatActivity;

/**
 * YaLin
 * 2016/11/23.
 */

@TargetApi(21)
public class LUtils {
    private LUtils(AppCompatActivity appCompatActivity) {
    }

    public static LUtils getInstance(AppCompatActivity activity) {
        return new LUtils(activity);
    }
}
