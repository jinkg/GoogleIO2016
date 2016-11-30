package com.yalin.googleio2016.explore.data;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.google.common.base.Strings;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.util.LogUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * YaLin
 * 2016/11/30.
 */

public class EventCard {
    private static final String TAG = "EventCard";

    public static final String ACTION_TYPE_LINK = "LINK";
    public static final String ACTION_TYPE_MAP = "MAP";
    public static final String ACTION_TYPE_SESSION = "SESSION";
    public static final Set<String> VALID_ACTION_TYPES = new HashSet<>();

    static {
        VALID_ACTION_TYPES.add(ACTION_TYPE_SESSION);
        VALID_ACTION_TYPES.add(ACTION_TYPE_MAP);
        VALID_ACTION_TYPES.add(ACTION_TYPE_LINK);
    }

    private String mActionExtra;
    private String mActionString;
    private String mActionType;
    private String mActionUrl;
    private String mDescription;
    private String mTitle;

    private EventCard(final String title, final String actionString, final String actionUrl,
                      final String description, final String actionType, final String actionExtra) {
        mTitle = title;
        mActionString = actionString;
        mActionUrl = actionUrl;
        mDescription = description;
        mActionType = actionType;
        mActionExtra = actionExtra;
    }

    public String getActionExtra() {
        return mActionExtra;
    }

    public String getActionString() {
        return mActionString;
    }

    public String getActionType() {
        return mActionType;
    }

    public String getActionUrl() {
        return mActionUrl;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isValid() {
        return isValid(this);
    }

    @Override
    public String toString() {
        return "EventCard{" +
                "mTitle='" + mTitle + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mActionUrl='" + mActionUrl + '\'' +
                ", mActionString='" + mActionString + '\'' +
                ", mActionType='" + mActionType + '\'' +
                ", mActionExtra='" + mActionExtra + '\'' +
                '}';
    }

    @Nullable
    public static EventCard fromCursorRow(Cursor cursor) {
        // TODO: Validate parameters and return null.
        EventCard card = new EventCard(
                cursor.getString(cursor.getColumnIndex(ScheduleContract.Cards.TITLE)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.Cards.ACTION_TEXT)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.Cards.ACTION_URL)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.Cards.MESSAGE)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.Cards.ACTION_TYPE)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.Cards.ACTION_EXTRA)));
        if (!isValid(card)) {
            LogUtil.d(TAG, "Invalid card loaded from database:" + card);
            return null;
        }
        return card;
    }

    public static boolean isValid(final EventCard card) {
        if (ACTION_TYPE_SESSION.equalsIgnoreCase(card.getActionType()) &&
                Strings.isNullOrEmpty(card.getActionExtra())) {
            return false;
        }
        if (!VALID_ACTION_TYPES.contains(card.getActionType())) {
            return false;
        }
        return true;
    }
}
