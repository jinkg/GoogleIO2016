package com.yalin.googleio2016.injection;

import android.app.Activity;

import com.yalin.googleio2016.messaging.MessagingRegistration;
import com.yalin.googleio2016.messaging.MessagingRegistrationWithGCM;

/**
 * YaLin
 * 2016/11/24.
 */

public class MessagingRegistrationProvider {
    private static MessagingRegistration stubMessagingRegistration;

    public static void setStubMessagingRegistration(MessagingRegistration messaging) {
        stubMessagingRegistration = messaging;
    }

    public static MessagingRegistration provideMessagingRegistration(Activity activity) {
        if (stubMessagingRegistration != null) {
            return stubMessagingRegistration;
        } else {
            return new MessagingRegistrationWithGCM(activity);
        }
    }
}
