package com.yalin.googleio2016.injection;

import android.app.Activity;

import com.yalin.googleio2016.login.LoginAndAuth;
import com.yalin.googleio2016.login.LoginAndAuthListener;
import com.yalin.googleio2016.login.LoginAndAuthWithGoogleApi;

/**
 * YaLin
 * 2016/11/23.
 */

public class LoginAndAuthProvider {
    private static LoginAndAuth stubLoginAndAuth;

    public static void setStubLoginAndAuth(LoginAndAuth login) {
        stubLoginAndAuth = login;
    }

    public static LoginAndAuth provideLoginAndAuth(Activity activity, LoginAndAuthListener callback,
                                                   String accountName) {
        if (stubLoginAndAuth != null) {
            return stubLoginAndAuth;
        } else {
            return new LoginAndAuthWithGoogleApi(activity, callback, accountName);
        }
    }
}
