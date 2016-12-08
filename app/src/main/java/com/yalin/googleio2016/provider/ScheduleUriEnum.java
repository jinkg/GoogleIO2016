package com.yalin.googleio2016.provider;

/**
 * YaLin
 * 2016/11/29.
 * <p>
 * The list of {@code Uri}s recognised by the {@code ContentProvider} of the app.
 * <p/>
 * It is important to order them in the order that follows {@link android.content.UriMatcher}
 * matching rules: wildcard {@code *} applies to one segment only and it processes matching per
 * segment in a tree manner over the list of {@code Uri} in the order they are added. The first
 * rule means that {@code sessions / *} would not match {@code sessions / id / room}. The second
 * rule is more subtle and means that if Uris are in the  order {@code * / room / counter} and
 * {@code sessions / room / time}, then {@code speaker / room / time} will not match anything,
 * because the {@code UriMatcher} will follow the path of the first  and will fail at the third
 * segment.
 */
public enum ScheduleUriEnum {
    BLOCKS(100, "blocks", ScheduleContract.Blocks.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.BLOCKS),
    TAGS(200, "tags", ScheduleContract.Tags.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.TAGS),
    SESSIONS(400, "sessions", ScheduleContract.Sessions.CONTENT_TYPE_ID, false,
            ScheduleDatabase.Tables.SESSIONS),
    SESSIONS_MY_SCHEDULE(401, "sessions/my_schedule", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS_SEARCH(403, "sessions/search/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, false, null),
    SESSIONS_ID(405, "sessions/*", ScheduleContract.Sessions.CONTENT_TYPE_ID, true, null),
    SESSIONS_ID_SPEAKERS(406, "sessions/*/speakers", ScheduleContract.Speakers.CONTENT_TYPE_ID, false,
            ScheduleDatabase.Tables.SESSIONS_SPEAKERS),
    SESSIONS_ID_TAGS(407, "sessions/*/tags", ScheduleContract.Tags.CONTENT_TYPE_ID, false,
            ScheduleDatabase.Tables.SESSIONS_TAGS),

    SEARCH_INDEX(801, "search_index", null, false, null),

    SEARCH_TOPICS_SESSION(1400, "search_topics_sessions",
            ScheduleContract.SearchTopicsSessions.CONTENT_TYPE_ID, false, null /*virtual table*/),

    CARDS(1500, "cards", ScheduleContract.Cards.CONTENT_TYPE_ID, false, ScheduleDatabase.Tables.CARDS);

    public int code;

    /**
     * The path to the {@link android.content.UriMatcher} will use to match. * may be used as a
     * wild card for any text, and # may be used as a wild card for numbers.
     */
    public String path;

    public String contentType;

    public String table;

    ScheduleUriEnum(int code, String path, String contentTypeId, boolean item, String table) {
        this.code = code;
        this.path = path;
        this.contentType = item ? ScheduleContract.makeContentItemType(contentTypeId)
                : ScheduleContract.makeContentType(contentTypeId);
        this.table = table;
    }
}
