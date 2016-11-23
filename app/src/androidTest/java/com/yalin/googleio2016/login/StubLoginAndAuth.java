package com.yalin.googleio2016.login;

import android.content.Intent;
import android.os.Handler;

/**
 * YaLin
 * 2016/11/23.
 */

public class StubLoginAndAuth implements LoginAndAuth {

    private String mAccountName;

    private LoginAndAuthListener mListener;

    private boolean mSuccess;

    private boolean mNewAuthentication;

    private boolean mIsStarted;

    public StubLoginAndAuth(String accountName, boolean success, boolean newAuthentication) {
        mAccountName = accountName;
        mSuccess = success;
        mNewAuthentication = newAuthentication;
    }

    public void setListener(LoginAndAuthListener listener) {
        mListener = listener;
    }

    @Override
    public String getAccountName() {
        return mAccountName;
    }

    @Override
    public void start() {
        mIsStarted = true;
        final Handler h = new Handler();

        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (mListener == null) {
                    h.postDelayed(this, 50);
                } else {
                    if (mSuccess) {
                        mListener.onAuthSuccess(mAccountName, mNewAuthentication);
                    } else {
                        mListener.onAuthFailure(mAccountName);
                    }
                }
            }
        };
        h.postDelayed(r, 0);
    }

    @Override
    public boolean isStarted() {
        return mIsStarted;
    }

    @Override
    public void stop() {
        mIsStarted = false;
    }

    @Override
    public void retryAuthByUserRequest() {
        start();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }
}
