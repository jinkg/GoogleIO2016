package com.yalin.googleio2016.explore;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.annotation.Nullable;
import android.view.View;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.explore.data.MessageData;
import com.yalin.googleio2016.messaging.MessagingRegistrationWithGCM;
import com.yalin.googleio2016.settings.ConfMessageCardUtils;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.WiFiUtils;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Helper class to create message data view objects representing MessageCards for the Explore I/O
 * stream.
 */
public class MessageCardHelper {
    private static final String TAG = "MessageCardHelper";

    public static MessageData getSimpleMessageCardData(
            final ConfMessageCardUtils.ConfMessageCard card) {
        MessageData messageData = new MessageData();
        messageData.setEndButtonStringResourceId(R.string.ok);
        messageData.setMessage(card.getSimpleMessage());
        messageData.setEndButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ConfMessageCardUtils.markDismissedConfMessageCard(v.getContext(), card);
            }
        });
        return messageData;
    }

    /**
     * Return the notifications messages opt-in data.
     */
    public static MessageData getNotificationsOptInMessageData() {
        MessageData messageData = new MessageData();
        messageData.setStartButtonStringResourceId(R.string.explore_io_msgcards_answer_no);
        messageData.setMessageStringResourceId(R.string.explore_io_notifications_ask_opt_in);
        messageData.setEndButtonStringResourceId(R.string.explore_io_msgcards_answer_yes);

        messageData.setStartButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Marking notifications question answered with decline.");
                ConfMessageCardUtils.setDismissedConfMessageCard(view.getContext(),
                        ConfMessageCardUtils.ConfMessageCard.SESSION_NOTIFICATIONS, false);
                SettingsUtils.setShowSessionReminders(view.getContext(), false);
                SettingsUtils.setShowSessionFeedbackReminders(view.getContext(), false);
            }
        });
        messageData.setEndButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Marking notifications messages question answered with affirmation.");
                ConfMessageCardUtils.setDismissedConfMessageCard(view.getContext(),
                        ConfMessageCardUtils.ConfMessageCard.SESSION_NOTIFICATIONS, true);
                SettingsUtils.setShowSessionReminders(view.getContext(), true);
                SettingsUtils.setShowSessionFeedbackReminders(view.getContext(), true);
            }
        });

        return messageData;
    }

    /**
     * Return the conference messages opt-in data.
     */
    public static MessageData getConferenceOptInMessageData() {
        MessageData messageData = new MessageData();
        messageData.setStartButtonStringResourceId(R.string.explore_io_msgcards_answer_no);
        messageData.setMessageStringResourceId(R.string.explore_io_msgcards_ask_opt_in);
        messageData.setEndButtonStringResourceId(R.string.explore_io_msgcards_answer_yes);

        messageData.setStartButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Marking conference messages question answered with decline.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(view.getContext(), true);
                ConfMessageCardUtils.setConfMessageCardsEnabled(view.getContext(), false);
                Activity activity;
                if ((activity = getActivity(view)) != null) {
                    // This will activate re-registering with the correct GCM topic(s).
                    new MessagingRegistrationWithGCM(activity).registerDevice();
                }
            }
        });
        messageData.setEndButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Marking conference messages question answered with affirmation.");
                ConfMessageCardUtils.markAnsweredConfMessageCardsPrompt(view.getContext(), true);
                ConfMessageCardUtils.setConfMessageCardsEnabled(view.getContext(), true);
                Activity activity;
                if ((activity = getActivity(view)) != null) {
                    // This will activate re-registering with the correct GCM topic(s).
                    new MessagingRegistrationWithGCM(activity).registerDevice();
                }
            }
        });

        return messageData;
    }

    /**
     * Return the wifi setup card data.
     */
    public static MessageData getWifiSetupMessageData() {
        MessageData messageData = new MessageData();
        messageData.setStartButtonStringResourceId(R.string.explore_io_msgcards_answer_no);
        messageData.setMessageStringResourceId(R.string.question_setup_wifi_card_text);
        messageData.setEndButtonStringResourceId(R.string.explore_io_msgcards_answer_yes);
        messageData.setIconDrawableId(R.drawable.message_card_wifi);

        messageData.setStartButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Marking wifi setup declined.");

                // Switching like this ensure the value change listener is fired.
                SettingsUtils.markDeclinedWifiSetup(view.getContext(), false);
                SettingsUtils.markDeclinedWifiSetup(view.getContext(), true);
            }
        });
        messageData.setEndButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Installing conference wifi.");
                WiFiUtils.installConferenceWiFi(view.getContext());

                // Switching like this ensure the value change listener is fired.
                SettingsUtils.markDeclinedWifiSetup(view.getContext(), true);
                SettingsUtils.markDeclinedWifiSetup(view.getContext(), false);
            }
        });

        return messageData;
    }

    @Nullable()
    private static Activity getActivity(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
