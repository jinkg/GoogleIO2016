package com.yalin.googleio2016;

import com.yalin.googleio2016.util.LogUtil;

/**
 * YaLin
 * 2016/11/25.
 */

public class Application extends android.app.Application {
    private static final String TAG = "Application";

    @Override
    public void onCreate() {
        super.onCreate();

        final Thread.UncaughtExceptionHandler exceptionHandler =
                Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LogUtil.e(TAG, "Application Exception", e);
                exceptionHandler.uncaughtException(t, e);
            }
        });
    }
}
