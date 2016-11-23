package com.yalin.googleio2016.login;

import android.content.Intent;

/**
 * YaLin
 * 2016/11/23.
 */

public interface LoginAndAuth {
    String getAccountName();

    void start();

    boolean isStarted();

    void stop();

    void retryAuthByUserRequest();

    boolean onActivityResult(int requestCode, int resultCode, Intent data);
}
