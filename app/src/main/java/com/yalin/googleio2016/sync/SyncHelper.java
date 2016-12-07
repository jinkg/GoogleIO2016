package com.yalin.googleio2016.sync;


import android.content.ContentResolver;
import android.os.Bundle;

import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.sync.account.Account;
import com.yalin.googleio2016.util.LogUtil;

/**
 * YaLin
 * 2016/12/5.
 */

public class SyncHelper {
    private static final String TAG = "SyncHelper";

    public static void requestManualSync() {
        requestManualSync(false);
    }

    public static void requestManualSync(boolean userDataSyncOnly) {
        LogUtil.d(TAG, "Requesting manual sync for account. userDataSyncOnly=" + userDataSyncOnly);
        android.accounts.Account account = Account.getAccount();
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        if (userDataSyncOnly) {
            b.putBoolean(SyncAdapter.EXTRA_SYNC_USER_DATA_ONLY, true);
        }
        ContentResolver
                .setSyncAutomatically(account, ScheduleContract.CONTENT_AUTHORITY, true);
        ContentResolver.setIsSyncable(account, ScheduleContract.CONTENT_AUTHORITY, 1);

        boolean pending =
                ContentResolver.isSyncPending(account, ScheduleContract.CONTENT_AUTHORITY);
        if (pending) {
            LogUtil.d(TAG, "Waring: sync is PENDING. Will cancel.");
        }
        boolean active = ContentResolver.isSyncActive(account, ScheduleContract.CONTENT_AUTHORITY);
        if (active) {
            LogUtil.d(TAG, "Waring: sync is ACTIVE. Will cancel.");
        }

        if (pending || active) {
            LogUtil.d(TAG, "Cancelling previously pending/active sync.");
            ContentResolver.cancelSync(account, ScheduleContract.CONTENT_AUTHORITY);
        }

        LogUtil.d(TAG, "Requesting sync now");
        ContentResolver.requestSync(account, ScheduleContract.CONTENT_AUTHORITY, b);
    }
}
