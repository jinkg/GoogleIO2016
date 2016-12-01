package com.yalin.googleio2016.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.yalin.googleio2016.provider.ScheduleContract.Cards;
import com.yalin.googleio2016.provider.ScheduleContract.MySchedule;
import com.yalin.googleio2016.provider.ScheduleContract.Rooms;
import com.yalin.googleio2016.provider.ScheduleContract.RoomsColumns;
import com.yalin.googleio2016.provider.ScheduleContract.Sessions;
import com.yalin.googleio2016.provider.ScheduleContract.SessionsColumns;
import com.yalin.googleio2016.provider.ScheduleContract.Speakers;
import com.yalin.googleio2016.provider.ScheduleContract.SyncColumns;
import com.yalin.googleio2016.provider.ScheduleContract.Tags;
import com.yalin.googleio2016.provider.ScheduleContract.TagsColumns;
import com.yalin.googleio2016.util.LogUtil;

/**
 * YaLin
 * 2016/11/29.
 */

public class ScheduleDatabase extends SQLiteOpenHelper {

    private static final String TAG = "ScheduleDatabase";

    private static final String DATABASE_NAME = "schedule.db";

    // NOTE: carefully update onUpgrade() when bumping database versions to make
    // sure user data is saved.

    private static final int VER_2016_11_30 = 1; // app version 1.0
    private static final int VER_2016_11_30B = 2;
    private static final int VER_2016_11_30C = 3;
    private static final int CUR_DATABASE_VERSION = VER_2016_11_30C;

    interface Tables {
        String CARDS = "cards";
        String TAGS = "tags";
        String ROOMS = "rooms";
        String SESSIONS = "sessions";
        String MY_SCHEDULE = "myschedule";
        String SPEAKERS = "speakers";
        String SESSIONS_TAGS = "sessions_tags";
        String SESSIONS_SPEAKERS = "sessions_speakers";

        String FEEDBACK = "feedback";

        String SESSIONS_JOIN_ROOMS_TAGS = "sessions "
                + "LEFT OUTER JOIN myschedule ON sessions.session_id=myschedule.session_id "
                + "AND myschedule.account_name=? "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id "
                + "LEFT OUTER JOIN sessions_tags ON sessions.session_id=sessions_tags.session_id";

        String SESSIONS_JOIN_ROOMS = "sessions "
                + "LEFT OUTER JOIN myschedule ON sessions.session_id=myschedule.session_id "
                + "AND myschedule.account_name=? "
                + "LEFT OUTER JOIN rooms ON sessions.room_id=rooms.room_id";

        String SESSIONS_SPEAKERS_JOIN_SPEAKERS = "sessions_speakers "
                + "LEFT OUTER JOIN speakers ON sessions_speakers.speaker_id=speakers.speaker_id";

        String SESSIONS_TAGS_JOIN_TAGS = "sessions_tags "
                + "LEFT OUTER JOIN tags ON sessions_tags.tag_id=tags.tag_id";
    }

    public interface SessionsSpeakers {
        String SESSION_ID = "session_id";
        String SPEAKER_ID = "speaker_id";
    }

    public interface SessionsTags {
        String SESSION_ID = "session_id";
        String TAG_ID = "tag_id";
    }

    /**
     * {@code REFERENCES} clauses.
     */
    private interface References {
        String TAG_ID = "REFERENCES " + Tables.TAGS + "(" + Tags
                .TAG_ID + ")";
        String SESSION_ID = "REFERENCES " + Tables.SESSIONS + "(" + Sessions.SESSION_ID + ")";
        String ROOM_ID = "REFERENCES " + Tables.ROOMS + "(" + Rooms.ROOM_ID + ")";
        String SPEAKER_ID = "REFERENCES " + Tables.SPEAKERS + "(" + Speakers.SPEAKER_ID + ")";
    }

    public ScheduleDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TagsColumns.TAG_ID + " TEXT NOT NULL,"
                + TagsColumns.TAG_CATEGORY + " TEXT NOT NULL,"
                + TagsColumns.TAG_NAME + " TEXT NOT NULL,"
                + TagsColumns.TAG_ORDER_IN_CATEGORY + " INTEGER,"
                + TagsColumns.TAG_COLOR + " TEXT NOT NULL,"
                + TagsColumns.TAG_ABSTRACT + " TEXT NOT NULL,"
                + "UNIQUE (" + TagsColumns.TAG_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.ROOMS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + RoomsColumns.ROOM_ID + " TEXT NOT NULL,"
                + RoomsColumns.ROOM_NAME + " TEXT,"
                + RoomsColumns.ROOM_FLOOR + " TEXT,"
                + "UNIQUE (" + RoomsColumns.ROOM_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.SESSIONS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_ID + " TEXT NOT NULL,"
                + Sessions.ROOM_ID + " TEXT " + References.ROOM_ID + ","
                + SessionsColumns.SESSION_START + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_END + " INTEGER NOT NULL,"
                + SessionsColumns.SESSION_LEVEL + " TEXT,"
                + SessionsColumns.SESSION_TITLE + " TEXT,"
                + SessionsColumns.SESSION_ABSTRACT + " TEXT,"
                + SessionsColumns.SESSION_REQUIREMENTS + " TEXT,"
                + SessionsColumns.SESSION_KEYWORDS + " TEXT,"
                + SessionsColumns.SESSION_HASHTAG + " TEXT,"
                + SessionsColumns.SESSION_URL + " TEXT,"
                + SessionsColumns.SESSION_YOUTUBE_URL + " TEXT,"
                + SessionsColumns.SESSION_MODERATOR_URL + " TEXT,"
                + SessionsColumns.SESSION_PDF_URL + " TEXT,"
                + SessionsColumns.SESSION_NOTES_URL + " TEXT,"
                + SessionsColumns.SESSION_CAL_EVENT_ID + " INTEGER,"
                + SessionsColumns.SESSION_LIVESTREAM_ID + " TEXT,"
                + SessionsColumns.SESSION_TAGS + " TEXT,"
                + SessionsColumns.SESSION_GROUPING_ORDER + " INTEGER,"
                + SessionsColumns.SESSION_SPEAKER_NAMES + " TEXT,"
                + SessionsColumns.SESSION_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + SessionsColumns.SESSION_MAIN_TAG + " TEXT,"
                + SessionsColumns.SESSION_COLOR + " INTEGER,"
                + SessionsColumns.SESSION_CAPTIONS_URL + " TEXT,"
                + SessionsColumns.SESSION_PHOTO_URL + " TEXT,"
                + SessionsColumns.SESSION_RELATED_CONTENT + " TEXT,"
                + "UNIQUE (" + SessionsColumns.SESSION_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.MY_SCHEDULE + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + MySchedule.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + MySchedule.MY_SCHEDULE_ACCOUNT_NAME + " TEXT NOT NULL,"
                + MySchedule.MY_SCHEDULE_DIRTY_FLAG + " INTEGER NOT NULL DEFAULT 1,"
                + MySchedule.MY_SCHEDULE_IN_SCHEDULE + " INTEGER NOT NULL DEFAULT 1,"
                + "UNIQUE (" + MySchedule.SESSION_ID + ","
                + MySchedule.MY_SCHEDULE_ACCOUNT_NAME + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.SESSIONS_TAGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsTags.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + SessionsTags.TAG_ID + " TEXT NOT NULL " + References.TAG_ID + ","
                + "UNIQUE (" + SessionsTags.SESSION_ID + ","
                + SessionsTags.TAG_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.SESSIONS_SPEAKERS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SessionsSpeakers.SESSION_ID + " TEXT NOT NULL " + References.SESSION_ID + ","
                + SessionsSpeakers.SPEAKER_ID + " TEXT NOT NULL " + References.SPEAKER_ID + ","
                + "UNIQUE (" + SessionsSpeakers.SESSION_ID + ","
                + SessionsSpeakers.SPEAKER_ID + ") ON CONFLICT REPLACE)");

        upgradeFrom30to30B(db);
        upgradeFrom30Bto30C(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LogUtil.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        // Current DB version. We update this variable as we perform upgrades to reflect
        // the current version we are in.
        int version = oldVersion;

        if (version == VER_2016_11_30) {
            LogUtil.d(TAG, "Upgrading database from 30 to 30B.");
            upgradeFrom30to30B(db);
            version = VER_2016_11_30B;
        }

        if (version == VER_2016_11_30B) {
            LogUtil.d(TAG, "Upgrading database from 30B to 30C.");
            upgradeFrom30Bto30C(db);
            version = VER_2016_11_30C;
        }

        if (version != CUR_DATABASE_VERSION) {
            LogUtil.w(TAG, "Upgrade unsuccessful -- destroying old data during upgrade");

            db.execSQL("DROP TABLE IF EXISTS " + Tables.CARDS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.ROOMS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SESSIONS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.MY_SCHEDULE);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SESSIONS_TAGS);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.SESSIONS_SPEAKERS);

            onCreate(db);

            version = CUR_DATABASE_VERSION;
        }
    }

    private void upgradeFrom30to30B(SQLiteDatabase db) {
        // Note: Adding photoUrl to tags
        db.execSQL("ALTER TABLE " + Tables.TAGS
                + " ADD COLUMN " + TagsColumns.TAG_PHOTO_URL + " TEXT");
    }

    private void upgradeFrom30Bto30C(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CARDS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Cards.ACTION_COLOR + " TEXT, "
                + Cards.ACTION_TEXT + " TEXT, "
                + Cards.ACTION_URL + " TEXT, "
                + Cards.BACKGROUND_COLOR + " TEXT, "
                + Cards.CARD_ID + " TEXT, "
                + Cards.DISPLAY_END_DATE + " INTEGER, "
                + Cards.DISPLAY_START_DATE + " INTEGER, "
                + Cards.MESSAGE + " TEXT, "
                + Cards.TEXT_COLOR + " TEXT, "
                + Cards.TITLE + " TEXT,  "
                + Cards.ACTION_TYPE + " TEXT,  "
                + Cards.ACTION_EXTRA + " TEXT, "
                + "UNIQUE (" + Cards.CARD_ID + ") ON CONFLICT REPLACE)");
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
