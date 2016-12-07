package com.yalin.googleio2016.feedback;

import com.turbomanage.httpclient.BasicHttpClient;

/**
 * YaLin
 * 2016/12/7.
 * <p>
 * Sends feedback data to the server Feedback API.
 */
public class FeedbackApiHelper {
    private static final String TAG = "FeedbackApiHelper";

    private final String mUrl;

    private BasicHttpClient mHttpClient;

    public FeedbackApiHelper(BasicHttpClient httpClient, String url) {
        mHttpClient = httpClient;
        mUrl = url;
    }
}
