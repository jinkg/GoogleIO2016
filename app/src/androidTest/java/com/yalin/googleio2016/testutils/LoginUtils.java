package com.yalin.googleio2016.testutils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.yalin.googleio2016.util.AccountUtils;

/**
 * YaLin
 * 2016/11/23.
 */

public class LoginUtils {
    public final static String DUMMY_ACCOUNT_NAME = "testieso";

    /**
     * @return account name, or a test account name
     */
    public static String setFirstAvailableAccountAsActive(Context context) {
        String account;
        AccountManager am =
                AccountManager.get(InstrumentationRegistry.getTargetContext());
        Account[] accountArray =
                am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        if (accountArray.length > 0) {
            account = accountArray[0].name;
        } else {
            account = DUMMY_ACCOUNT_NAME;
        }
        AccountUtils
                .setActiveAccount(InstrumentationRegistry.getTargetContext(),
                        account);
        return account;
    }
}
