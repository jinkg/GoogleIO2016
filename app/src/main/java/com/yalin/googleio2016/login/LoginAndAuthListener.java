package com.yalin.googleio2016.login;

/**
 * YaLin
 * 2016/11/23.
 */

public interface LoginAndAuthListener {
    void onPlusInfoLoaded(String accountName);

    void onAuthSuccess(String accountName, boolean newlyAuthenticated);

    void onAuthFailure(String accountName);
}
