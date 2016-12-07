package com.yalin.googleio2016.sync.userdata;

import android.content.Context;

import com.yalin.googleio2016.sync.userdata.firebase.FirebaseUserDataSyncHelper;

/**
 * YaLin
 * 2016/12/7.
 */

public class UserDataSyncHelperFactory {
    public static AbstractUserDataSyncHelper buildSyncHelper(Context context, String accountName) {
        return new FirebaseUserDataSyncHelper();
    }
}
