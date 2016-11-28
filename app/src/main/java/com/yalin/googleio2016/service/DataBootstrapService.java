package com.yalin.googleio2016.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.yalin.googleio2016.settings.SettingsUtils;

/**
 * YaLin
 * 2016/11/28.
 */

public class DataBootstrapService extends IntentService {
    private static final String TAG = "DataBootstrapService";

    public static void startDataBootstrapIfNecessary(Context context) {
        if (!SettingsUtils.isDataBootstrapDone(context)) {
            context.startService(new Intent(context, DataBootstrapService.class));
        }
    }

    public DataBootstrapService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context appContext = getApplicationContext();


    }
}
