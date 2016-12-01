package com.yalin.googleio2016.io.model;

import com.google.gson.annotations.SerializedName;
import com.yalin.googleio2016.util.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * YaLin
 * 2016/12/1.
 * <p>
 * Object used with Gson library to convert json data for use.
 * <p/>
 * Note: The library contains date parsing validations similar, but more restrictive than the
 * application's general logic.
 */
public class Card {
    private static final String TAG = "Card";

    public static final SimpleDateFormat TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
    // TODO: Remove this format once other clients aren't reliant on it.
    public static final SimpleDateFormat ALT_TIME_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    @SerializedName("card_id")
    public String mId;
    @SerializedName("title")
    public String mTitle;
    @SerializedName("message")
    public String mMessage;
    @SerializedName("short_message")
    public String mShortMessage;
    @SerializedName("action_url")
    public String mActionUrl;
    @SerializedName("bg_color_android")
    public String mBackgroundColor;
    @SerializedName("text_color_android")
    public String mTextColor;
    @SerializedName("action_color_android")
    public String mActionColor;
    @SerializedName("action_text")
    public String mActionText;
    @SerializedName("action_extra")
    public String mActionExtra;
    @SerializedName("action_type")
    public String mActionType;
    @SerializedName("valid_from")
    public String mValidFrom;
    @SerializedName("valid_until")
    public String mValidUntil;

    @Override
    public String toString() {
        return "Card{" +
                "mId='" + mId + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mMessage='" + mMessage + '\'' +
                ", mShortMessage='" + mShortMessage + '\'' +
                ", mActionUrl='" + mActionUrl + '\'' +
                ", mBackgroundColor='" + mBackgroundColor + '\'' +
                ", mTextColor='" + mTextColor + '\'' +
                ", mActionColor='" + mActionColor + '\'' +
                ", mActionText='" + mActionText + '\'' +
                ", mActionExtra='" + mActionExtra + '\'' +
                ", mActionType='" + mActionType + '\'' +
                ", mValidFrom='" + mValidFrom + '\'' +
                ", mValidUntil='" + mValidUntil + '\'' +
                '}';
    }

    /**
     * Returns millis since epoch of the time represented by the {@code formattedTime}. The input
     * must be in the acceptable time format or an Exception will be thrown.
     */
    public static long getEpochMillisFromTimeString(String formattedTime)
            throws IllegalArgumentException {
        try {
            return TIME_FORMAT.parse(formattedTime).getTime();
        } catch (ParseException exception) {
            try {
                LogUtil.d(TAG, "Trying alternate time format");
                return ALT_TIME_FORMAT.parse(formattedTime).getTime();
            } catch (ParseException exception2) {
                throw new IllegalArgumentException("Invalid time format: " + formattedTime,
                        exception);
            }
        }
    }
}
