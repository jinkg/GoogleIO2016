package com.yalin.googleio2016.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * YaLin
 * 2016/12/7.
 */

public class SessionAlarmService extends IntentService {
    private static final String TAG = "SessionAlarmService";

    public static final String ACTION_SCHEDULE_ALL_STARRED_BLOCKS =
            "com.yalin.googleio2016.action.SCHEDULE_ALL_STARRED_BLOCKS";

    public SessionAlarmService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
