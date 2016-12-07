package com.yalin.googleio2016;

import com.yalin.googleio2016.util.ParserUtils;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * YaLin
 * 2016/11/29.
 */

public class Config {
    // Turn the hard-coded conference dates in gradle.properties into workable objects.
    public static final long[][] CONFERENCE_DAYS = new long[][]{
            // start and end of day 1
            {ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY1_START),
                    ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY1_END)},
            // start and end of day 2
            {ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY2_START),
                    ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY2_END)},
            // start and end of day 3
            {ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY3_START),
                    ParserUtils.parseTime(BuildConfig.CONFERENCE_DAY3_END)},
    };

    public static final TimeZone CONFERENCE_TIMEZONE =
            TimeZone.getTimeZone(BuildConfig.INPERSON_TIMEZONE);

    public static final long CONFERENCE_START_MILLIS = CONFERENCE_DAYS[0][0];

    public static final long CONFERENCE_END_MILLIS = CONFERENCE_DAYS[CONFERENCE_DAYS.length - 1][1];

    // When do we start to offer to set up the user's wifi?
    public static final long WIFI_SETUP_OFFER_START = (BuildConfig.DEBUG ?
            System.currentTimeMillis() - 1000 :
            CONFERENCE_START_MILLIS - TimeUnit.MILLISECONDS.convert(3L, TimeUnit.DAYS));

    // Auto sync interval. Shouldn't be too small, or it might cause battery drain.
    public static final long AUTO_SYNC_INTERVAL_LONG_BEFORE_CONFERENCE =
            TimeUnit.MILLISECONDS.convert(6L, TimeUnit.HOURS);

    public static final long AUTO_SYNC_INTERVAL_AROUND_CONFERENCE =
            TimeUnit.MILLISECONDS.convert(2L, TimeUnit.HOURS);

    // Disable periodic sync after the conference and rely entirely on GCM push for syncing data.
    public static final long AUTO_SYNC_INTERVAL_AFTER_CONFERENCE = -1L;

    // How many days before the conference we consider to be "around the conference date"
    // for purposes of sync interval (at which point the AUTO_SYNC_INTERVAL_AROUND_CONFERENCE
    // interval kicks in)
    public static final long AUTO_SYNC_AROUND_CONFERENCE_THRESH =
            TimeUnit.MILLISECONDS.convert(3L, TimeUnit.DAYS);

    // Known session tags that induce special behaviors
    public interface Tags {

        // tag that indicates a session is a live session
        String SESSIONS = "TYPE_SESSIONS";

        // the tag category that we use to group sessions together when displaying them
        String SESSION_GROUPING_TAG_CATEGORY = "TYPE";

        // tag categories
        String CATEGORY_THEME = "THEME";
        String CATEGORY_TRACK = "TRACK";
        String CATEGORY_TYPE = "TYPE";
        String CATEGORY_SEP = "_";

        String SPECIAL_KEYNOTE = "FLAG_KEYNOTE";

        String[] EXPLORE_CATEGORIES =
                {CATEGORY_THEME, CATEGORY_TRACK, CATEGORY_TYPE};

        int[] EXPLORE_CATEGORY_ALL_STRING = {
                R.string.all_themes, R.string.all_topics, R.string.all_types
        };

        int[] EXPLORE_CATEGORY_TITLE = {
                R.string.themes, R.string.topics, R.string.types
        };
    }
}
