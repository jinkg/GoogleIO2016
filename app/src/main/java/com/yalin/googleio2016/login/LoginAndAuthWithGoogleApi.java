package com.yalin.googleio2016.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.google.android.gms.common.Scopes;
import com.yalin.googleio2016.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * YaLin
 * 2016/11/23.
 */

public class LoginAndAuthWithGoogleApi implements LoginAndAuth {
    private static final String TAG = "LoginAndAuthGoogle";

    private static final List<String> AUTH_SCOPES = new ArrayList<>(Arrays.asList(
            Scopes.PLUS_LOGIN,
            Scopes.DRIVE_APPFOLDER,
            "https://www.googleapis.com/auth/plus.profile.emails.read"));

    private WeakReference<Activity> mActivityRef;

    private WeakReference<LoginAndAuthListener> mCallbackRef;

    private String mAccountName;

    public LoginAndAuthWithGoogleApi(Activity activity, LoginAndAuthListener callback,
                                     String accountName) {
        LogUtil.d(TAG, "Helper created. Account: " + mAccountName);
        mActivityRef = new WeakReference<>(activity);
        mCallbackRef = new WeakReference<>(callback);
        mAccountName = accountName;
    }

    public static List<String> getAuthScopes() {
        return AUTH_SCOPES;
    }

    @Override
    public String getAccountName() {
        return null;
    }

    @Override
    public void start() {
        final LoginAndAuthListener callbacks;
        if (null != (callbacks = mCallbackRef.get())) {
            final Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    callbacks.onAuthSuccess(mAccountName, true);
                }
            }, 300);

        }
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public void stop() {

    }

    @Override
    public void retryAuthByUserRequest() {

    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }
}
