package com.yalin.googleio2016.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.io.JSONHandler;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.sync.ConferenceDataHandler;
import com.yalin.googleio2016.util.LogUtil;

import java.io.IOException;

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

        if (SettingsUtils.isDataBootstrapDone(appContext)) {
            LogUtil.d(TAG, "Data bootstrap already done.");
            return;
        }
        try {
            LogUtil.d(TAG, "Starting data bootstrap process.");
            String bootstrapJson = JSONHandler.parseResource(appContext, R.raw.bootstrap_data);

            ConferenceDataHandler dataHandler = new ConferenceDataHandler(appContext);
            dataHandler.applyConferenceData(new String[]{bootstrapJson},
                    BuildConfig.BOOTSTRAP_DATA_TIMESTAMP, false);

            LogUtil.d(TAG, "End of bootstrap -- successful. Marking bootstrap as done.");
            SettingsUtils.markDataBootstrapDone(appContext);

            getContentResolver().notifyChange(Uri.parse(ScheduleContract.CONTENT_AUTHORITY),
                    null, false);
        } catch (IOException e) {
            // This is serious -- if this happens, the app won't work :-(
            // This is unlikely to happen in production, but IF it does, we apply
            // this workaround as a fallback: we pretend we managed to do the bootstrap
            // and hope that a remote sync will work.
            LogUtil.e(TAG, "*** ERROR DURING BOOTSTRAP! Problem in bootstrap data?", e);
            LogUtil.d(TAG, "Applying fallback -- marking boostrap as done; sync might fix problem.");
            SettingsUtils.markDataBootstrapDone(appContext);
        } finally {
            // Request a manual sync immediately after the bootstrapping process, in case we
            // have an active connection. Otherwise, the scheduled sync could take a while.
//            SyncHelper.requestManualSync();
        }
    }
}
