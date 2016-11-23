package com.yalin.googleio2016.login;

/**
 * YaLin
 * 2016/11/23.
 */

public interface LoginStateListener {
    void onSignInOrCreateAccount();

    void onAccountChangeRequested();

    void onStartLoginProcessRequested();
}
