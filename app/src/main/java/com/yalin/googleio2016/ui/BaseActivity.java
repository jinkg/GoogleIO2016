package com.yalin.googleio2016.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yalin.googleio2016.injection.LoginAndAuthProvider;
import com.yalin.googleio2016.login.LoginAndAuth;
import com.yalin.googleio2016.login.LoginAndAuthListener;
import com.yalin.googleio2016.util.AccountUtils;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.RecentTasksStyler;
import com.yalin.googleio2016.welcome.WelcomeActivity;

/**
 * YaLin
 * 2016/11/23.
 */

public abstract class BaseActivity extends AppCompatActivity implements LoginAndAuthListener {
    private static final String TAG = "BaseActivity";

    private LoginAndAuth mLoginAndAuthProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecentTasksStyler.styleRecentTasksEntry(this);

        if (WelcomeActivity.shouldDisplay(this)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        startLoginProcess();
    }

    private void startLoginProcess() {
        LogUtil.d(TAG, "Starting login process.");

        if (!AccountUtils.hasActiveAccount(this)) {
            LogUtil.d(TAG, "Can't proceed with login -- no account chosen.");
            return;
        }

        String accountName = AccountUtils.getActiveAccountName(this);
        LogUtil.d(TAG, "Chosen account: " + accountName);

        if (mLoginAndAuthProvider != null && mLoginAndAuthProvider.getAccountName()
                .equals(accountName)) {
            LogUtil.d(TAG, "Helper already set up; simply starting it.");
            mLoginAndAuthProvider.start();
            return;
        }

        LogUtil.d(TAG, "Starting login process with account " + accountName);

        if (mLoginAndAuthProvider != null) {
            LogUtil.d(TAG, "Tearing down old Helper, was " + mLoginAndAuthProvider.getAccountName());
            if (mLoginAndAuthProvider.isStarted()) {
                LogUtil.d(TAG, "Stopping old Helper");
                mLoginAndAuthProvider.stop();
            }
            mLoginAndAuthProvider = null;
        }

        LogUtil.d(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthProvider =
                LoginAndAuthProvider.provideLoginAndAuth(this, this, accountName);
        mLoginAndAuthProvider.start();
    }

    @Override
    public void onAuthFailure(String accountName) {

    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {

    }

    @Override
    public void onPlusInfoLoaded(String accountName) {

    }
}
