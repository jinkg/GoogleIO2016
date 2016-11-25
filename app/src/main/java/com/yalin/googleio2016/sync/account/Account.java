package com.yalin.googleio2016.sync.account;

import android.accounts.AccountManager;
import android.app.Activity;

import com.yalin.googleio2016.util.LogUtil;

/**
 * YaLin
 * 2016/11/24.
 * <p>
 * Responsible for registering our custom authenticator with the system, and allowing other
 * classes to obtain a handle to our sync account.
 */

public class Account {
    public static final String ACCOUNT_TYPE = "com.yalin.googleio2016";

    public static final String ACCOUNT_NAME = "Sync Account";

    private static final String TAG = "Account";

    private static android.accounts.Account mAccount;

    public static android.accounts.Account createSyncAccount(Activity activity) {
        AccountManager accountManager = AccountManager.get(activity);

        android.accounts.Account account = getAccount();
        if (accountManager.addAccountExplicitly(account, null, null)) {
            return account;
        } else {
            LogUtil.d(TAG, "Unable to create account");
            return null;
        }
    }

    /**
     * Get the account object for this application.
     * <p>
     * <p>Note that, since this is just used for sync adapter purposes, this object will always
     * be the same.
     *
     * @return account
     */
    public static android.accounts.Account getAccount() {
        if (mAccount == null) {
            mAccount = new android.accounts.Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        }
        return mAccount;
    }
}
