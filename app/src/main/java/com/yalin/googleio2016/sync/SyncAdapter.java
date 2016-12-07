package com.yalin.googleio2016.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * YaLin
 * 2016/12/5.
 */

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String EXTRA_SYNC_USER_DATA_ONLY =
            "com.yalin.googleio2016.EXTRA_SYNC_USER_DATA_ONLY";

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle extras,
                              String authority,
                              ContentProviderClient provider,
                              SyncResult syncResult) {

    }
}
