package com.yalin.googleio2016.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * YaLin
 * 2016/11/29.
 * <p>
 * Contract class for interacting with {@link ScheduleProvider}. Unless otherwise noted, all
 * time-based fields are milliseconds since epoch and can be compared against
 * {@link System#currentTimeMillis()}.
 * <p>
 * The backing {@link android.content.ContentProvider} assumes that {@link android.net.Uri}
 * are generated using stronger {@link java.lang.String} identifiers, instead of
 * {@code int} {@link android.provider.BaseColumns#_ID} values, which are prone to shuffle during
 * sync.
 */
public final class ScheduleContract {
    public static final String CONTENT_TYPE_APP_BASE = "googleIO2016.";

    public static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd."
            + CONTENT_TYPE_APP_BASE;

    public static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd."
            + CONTENT_TYPE_APP_BASE;

    public interface SyncColumns {
        String UPDATED = "updated";
    }

    interface TagsColumns {

        /**
         * Unique string identifying this tag. For example, "TOPIC_ANDROID", "TYPE_CODELAB"
         */
        String TAG_ID = "tag_id";
        /**
         * Tag category. For example, the tags that identify what topic a session pertains
         * to might belong to the "TOPIC" category; the tags that identify what type a session
         * is (codelab, office hours, etc) might belong to the "TYPE" category.
         */
        String TAG_CATEGORY = "tag_category";
        /**
         * Tag name. For example, "Android".
         */
        String TAG_NAME = "tag_name";
        /**
         * Tag's order in its category (for sorting).
         */
        String TAG_ORDER_IN_CATEGORY = "tag_order_in_category";
        /**
         * Tag's color, in integer format.
         */
        String TAG_COLOR = "tag_color";
        /**
         * Tag abstract. Short summary describing tag.
         */
        String TAG_ABSTRACT = "tag_abstract";
        /**
         * The tag's photo Url.
         */
        String TAG_PHOTO_URL = "tag_photo_url";
    }

    interface RoomsColumns {

        /**
         * Unique string identifying this room.
         */
        String ROOM_ID = "room_id";
        /**
         * Name describing this room.
         */
        String ROOM_NAME = "room_name";
        /**
         * Building floor this room exists on.
         */
        String ROOM_FLOOR = "room_floor";
    }

    interface SessionsColumns {

        /**
         * Unique string identifying this session.
         */
        String SESSION_ID = "session_id";

        /**
         * Difficulty level of this session.
         */
        String SESSION_LEVEL = "session_level";

        /**
         * Start time of this track.
         */
        String SESSION_START = "session_start";

        /**
         * End time of this track.
         */
        String SESSION_END = "session_end";

        /**
         * Title describing this track.
         */
        String SESSION_TITLE = "session_title";

        /**
         * Body of text explaining this session in detail.
         */
        String SESSION_ABSTRACT = "session_abstract";

        /**
         * Requirements that attendees should meet.
         */
        String SESSION_REQUIREMENTS = "session_requirements";

        /**
         * Kewords/tags for this session.
         */
        String SESSION_KEYWORDS = "session_keywords";

        /**
         * Hashtag for this session.
         */
        String SESSION_HASHTAG = "session_hashtag";

        /**
         * Full URL to session online.
         */
        String SESSION_URL = "session_url";

        /**
         * Full URL to YouTube.
         */
        String SESSION_YOUTUBE_URL = "session_youtube_url";

        /**
         * Full URL to PDF.
         */
        String SESSION_PDF_URL = "session_pdf_url";

        /**
         * Full URL to official session notes.
         */
        String SESSION_NOTES_URL = "session_notes_url";

        /**
         * User-specific flag indicating starred status.
         */
        String SESSION_IN_MY_SCHEDULE = "session_in_my_schedule";

        /**
         * Key for session Calendar event. (Used in ICS or above)
         */
        String SESSION_CAL_EVENT_ID = "session_cal_event_id";

        /**
         * The YouTube live stream URL.
         */
        String SESSION_LIVESTREAM_ID = "session_livestream_url";

        /**
         * The Moderator URL.
         */
        String SESSION_MODERATOR_URL = "session_moderator_url";

        /**
         * The set of tags the session has. This is a comma-separated list of tags.
         */
        String SESSION_TAGS = "session_tags";

        /**
         * The names of the speakers on this session, formatted for display.
         */
        String SESSION_SPEAKER_NAMES = "session_speaker_names";

        /**
         * The order (for sorting) of this session's type.
         */
        String SESSION_GROUPING_ORDER = "session_grouping_order";

        /**
         * The hashcode of the data used to create this record.
         */
        String SESSION_IMPORT_HASHCODE = "session_import_hashcode";

        /**
         * The session's main tag.
         */
        String SESSION_MAIN_TAG = "session_main_tag";

        /**
         * The session's branding color.
         */
        String SESSION_COLOR = "session_color";

        /**
         * The session's captions URL (for livestreamed sessions).
         */
        String SESSION_CAPTIONS_URL = "session_captions_url";

        /**
         * The session interval when using the interval counter query.
         */
        String SESSION_INTERVAL_COUNT = "session_interval_count";

        /**
         * The session's photo URL.
         */
        String SESSION_PHOTO_URL = "session_photo_url";

        /**
         * The session's related content (videos and call to action links).
         */
        String SESSION_RELATED_CONTENT = "session_related_content";
    }

    interface SpeakersColumns {

        /**
         * Unique string identifying this speaker.
         */
        String SPEAKER_ID = "speaker_id";
        /**
         * Name of this speaker.
         */
        String SPEAKER_NAME = "speaker_name";
        /**
         * Profile photo of this speaker.
         */
        String SPEAKER_IMAGE_URL = "speaker_image_url";
        /**
         * Company this speaker works for.
         */
        String SPEAKER_COMPANY = "speaker_company";
        /**
         * Body of text describing this speaker in detail.
         */
        String SPEAKER_ABSTRACT = "speaker_abstract";
        /**
         * Deprecated. Full URL to the speaker's profile.
         */
        String SPEAKER_URL = "speaker_url";
        /**
         * Full URL to the the speaker's G+ profile.
         */
        String SPEAKER_PLUSONE_URL = "plusone_url";
        /**
         * Full URL to the the speaker's Twitter profile.
         */
        String SPEAKER_TWITTER_URL = "twitter_url";
        /**
         * The hashcode of the data used to create this record.
         */
        String SPEAKER_IMPORT_HASHCODE = "speaker_import_hashcode";
    }

    interface MyScheduleColumns {

        String SESSION_ID = SessionsColumns.SESSION_ID;
        /**
         * Account name for which the session is starred (in my schedule)
         */
        String MY_SCHEDULE_ACCOUNT_NAME = "account_name";
        /**
         * Indicate if last operation was "add" (true) or "remove" (false). Since uniqueness is
         * given by seesion_id+account_name, this field can be used as a way to find removals and
         * sync them with the cloud
         */
        String MY_SCHEDULE_IN_SCHEDULE = "in_schedule";
        /**
         * Flag to indicate if the corresponding in_my_schedule item needs to be synced
         */
        String MY_SCHEDULE_DIRTY_FLAG = "dirty";
        String MY_SCHEDULE_TIMESTAMP = "timestamp";
    }

    interface CardsColumns {
        /**
         * Unique id for each card
         */
        String CARD_ID = "card_id";
        String TITLE = "title";
        /**
         * URL for the action displayed on the card
         */
        String ACTION_URL = "action_url";
        /**
         * Time when the card can start to be displayed
         */
        String DISPLAY_START_DATE = "start_date";
        /**
         * Time when the card should no longer be displayed
         */
        String DISPLAY_END_DATE = "end_date";
        /**
         * Extended message for the card
         */
        String MESSAGE = "message";
        String BACKGROUND_COLOR = "bg_color";
        String TEXT_COLOR = "text_color";
        String ACTION_COLOR = "action_color";
        String ACTION_TEXT = "action_text";
        String ACTION_TYPE = "action_type";
        String ACTION_EXTRA = "action_extra";
    }


    public static final String CONTENT_AUTHORITY = "com.yalin.googleio2016";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_BLOCKS = "blocks";

    private static final String PATH_AFTER = "after";

    private static final String PATH_CARDS = "cards";

    private static final String PATH_TAGS = "tags";

    private static final String PATH_ROOM = "room";

    private static final String PATH_UNSCHEDULED = "unscheduled";

    private static final String PATH_ROOMS = "rooms";

    private static final String PATH_SESSIONS = "sessions";

    private static final String PATH_FEEDBACK = "feedback";

    private static final String PATH_MY_SCHEDULE = "my_schedule";

    private static final String PATH_MY_VIEWED_VIDEOS = "my_viewed_videos";

    private static final String PATH_MY_FEEDBACK_SUBMITTED = "my_feedback_submitted";

    private static final String PATH_SESSIONS_COUNTER = "counter";

    private static final String PATH_SPEAKERS = "speakers";

    private static final String PATH_ANNOUNCEMENTS = "announcements";

    private static final String PATH_MAP_MARKERS = "mapmarkers";

    private static final String PATH_MAP_FLOOR = "floor";

    private static final String PATH_MAP_TILES = "maptiles";

    private static final String PATH_HASHTAGS = "hashtags";

    private static final String PATH_VIDEOS = "videos";

    private static final String PATH_SEARCH = "search";

    private static final String PATH_SEARCH_SUGGEST = "search_suggest_query";

    private static final String PATH_SEARCH_INDEX = "search_index";

    private static final String PATH_PEOPLE_IVE_MET = "people_ive_met";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_BLOCKS,
            PATH_TAGS,
            PATH_ROOMS,
            PATH_CARDS,
            PATH_SESSIONS,
            PATH_FEEDBACK,
            PATH_MY_SCHEDULE,
            PATH_SPEAKERS,
            PATH_ANNOUNCEMENTS,
            PATH_MAP_MARKERS,
            PATH_MAP_FLOOR,
            PATH_MAP_MARKERS,
            PATH_MAP_TILES,
            PATH_HASHTAGS,
            PATH_VIDEOS,
            PATH_PEOPLE_IVE_MET
    };

    public static String makeContentItemType(String id) {
        if (id != null) {
            return CONTENT_ITEM_TYPE_BASE + id;
        } else {
            return null;
        }
    }

    public static String makeContentType(String id) {
        if (id != null) {
            return CONTENT_TYPE_BASE + id;
        } else {
            return null;
        }
    }

    /**
     * Each session has zero or more {@link Tags}, a {@link Rooms},
     * zero or more {@link Speakers}.
     */
    public static class Sessions implements SessionsColumns, RoomsColumns,
            SyncColumns, BaseColumns {

        public static final String QUERY_PARAMETER_TAG_FILTER = "filter";
        public static final String QUERY_PARAMETER_CATEGORIES = "categories";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SESSIONS).build();

        public static final Uri CONTENT_MY_SCHEDULE_URI =
                CONTENT_URI.buildUpon().appendPath(PATH_MY_SCHEDULE).build();

        public static final String CONTENT_TYPE_ID = "session";

        public static final String ROOM_ID = "room_id";

        public static final String SEARCH_SNIPPET = "search_snippet";

        public static final String HAS_GIVE_FEEDBACK = "has_give_feedback";

        public static final String SORT_BY_TYPE_THEN_TIME = SESSION_GROUPING_ORDER + " ASC,"
                + SESSION_START + " ASC," + SESSION_TITLE + " COLLATE NOCASE ASC";

        /**
         * Build {@link Uri} for requested {@link #SESSION_ID}.
         */
        public static Uri buildSessionUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).build();
        }

        /**
         * Build {@link Uri} that references any {@link Speakers} associated
         * with the requested {@link #SESSION_ID}.
         */
        public static Uri buildSpeakersDirUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_SPEAKERS).build();
        }

        /**
         * Build {@link Uri} that references any {@link Tags} associated with
         * the requested {@link #SESSION_ID}.
         */
        public static Uri buildTagsDirUri(String sessionId) {
            return CONTENT_URI.buildUpon().appendPath(sessionId).appendPath(PATH_TAGS).build();
        }

        /**
         * Read {@link #SESSION_ID} from {@link Sessions} {@link Uri}.
         */
        public static String getSessionId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Speakers are individual people that lead {@link Sessions}.
     */
    public static class Speakers implements SpeakersColumns, SyncColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SPEAKERS).build();

        public static final String CONTENT_TYPE_ID = "speaker";

        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = SpeakersColumns.SPEAKER_NAME
                + " COLLATE NOCASE ASC";

        /**
         * Build {@link Uri} for requested {@link #SPEAKER_ID}.
         */
        public static Uri buildSpeakerUri(String speakerId) {
            return CONTENT_URI.buildUpon().appendPath(speakerId).build();
        }

        /**
         * Read {@link #SPEAKER_ID} from {@link Speakers} {@link Uri}.
         */
        public static String getSpeakerId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Tags represent Session classifications. A session can have many tags. Tags can indicate,
     * for example, what product a session pertains to (Android, Chrome, ...), what type
     * of session it is (session, codelab, office hours, ...) and what overall event theme
     * it falls under (Design, Develop, Distribute), amongst others.
     */
    public static class Tags implements TagsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TAGS).build();

        public static final String CONTENT_TYPE_ID = "tag";

        /**
         * Build {@link Uri} that references all tags.
         */
        public static Uri buildTagsUri() {
            return CONTENT_URI;
        }

        /**
         * Build a {@link Uri} that references a given tag.
         */
        public static Uri buildTagUri(String tagId) {
            return CONTENT_URI.buildUpon().appendPath(tagId).build();
        }

        /**
         * Read {@link #TAG_ID} from {@link Tags} {@link Uri}.
         */
        public static String getTagId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * Rooms are physical locations at the conference venue.
     */
    public static class Rooms implements RoomsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROOMS).build();

        public static final String CONTENT_TYPE_ID = "room";

        /**
         * Build {@link Uri} for requested {@link #ROOM_ID}.
         */
        public static Uri buildRoomUri(String roomId) {
            return CONTENT_URI.buildUpon().appendPath(roomId).build();
        }

        /**
         * Build {@link Uri} that references any {@link Sessions} associated
         * with the requested {@link #ROOM_ID}.
         */
        public static Uri buildSessionsDirUri(String roomId) {
            return CONTENT_URI.buildUpon().appendPath(roomId).appendPath(PATH_SESSIONS).build();
        }

        /**
         * Read {@link #ROOM_ID} from {@link Rooms} {@link Uri}.
         */
        public static String getRoomId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /**
     * MySchedule represent the sessions that the user has starred/added to the "my schedule".
     * Each row of MySchedule represents one session in one account's my schedule.
     */
    public static class MySchedule implements MyScheduleColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MY_SCHEDULE).build();

        public static final String CONTENT_TYPE_ID = "myschedule";

        public static Uri buildMyScheduleUri(String accountName) {
            return ScheduleContractHelper.addOverrideAccountName(CONTENT_URI, accountName);
        }

    }

    /**
     * Cards are presented on the Explore I/O screen.
     */
    public static class Cards implements CardsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CARDS).build();

        public static final String CONTENT_TYPE_ID = "cards";

        /**
         * Build {@link Uri} that references any {@link Cards}.
         */
        public static Uri buildCardsUri() {
            return CONTENT_URI.buildUpon().appendPath(PATH_CARDS).build();
        }

        /**
         * Build {@link Uri} for requested {@link #CARD_ID}.
         */
        public static Uri buildCardUri(String cardId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_CARDS).appendPath(cardId).build();
        }
    }
}
