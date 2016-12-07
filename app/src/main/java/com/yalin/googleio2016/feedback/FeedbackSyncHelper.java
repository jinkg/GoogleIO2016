package com.yalin.googleio2016.feedback;

import android.content.Context;

/**
 * YaLin
 * 2016/12/7.
 * <p>
 * Provides unidirectional sync from the feedback data provided by the user to the server feedback
 * API.
 */
public class FeedbackSyncHelper {
    private static final String TAG = "FeedbackSyncHelper";

    Context mContext;
    FeedbackApiHelper mFeedbackApiHelper;

    public FeedbackSyncHelper(Context context, FeedbackApiHelper feedbackApi) {
        mContext = context;
        mFeedbackApiHelper = feedbackApi;

    }

    public void sync() {
        // TODO: 2016/12/7  
    }
}
