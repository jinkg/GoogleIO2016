package com.yalin.googleio2016.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.provider.ScheduleContract.Blocks;
import com.yalin.googleio2016.provider.ScheduleContract.Feedback;
import com.yalin.googleio2016.provider.ScheduleContract.SearchTopicsSessions;
import com.yalin.googleio2016.provider.ScheduleContract.Sessions;
import com.yalin.googleio2016.provider.ScheduleContract.Speakers;
import com.yalin.googleio2016.provider.ScheduleContract.Tags;
import com.yalin.googleio2016.provider.ScheduleDatabase.SessionsSpeakers;
import com.yalin.googleio2016.provider.ScheduleDatabase.Tables;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.util.AccountUtils;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.SelectionBuilder;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * YaLin
 * 2016/11/29.
 * <p>
 * {@link android.content.ContentProvider} that stores {@link ScheduleContract} data. Data is
 * usually inserted by {@link com.yalin.googleio2016.sync.SyncHelper}, and queried using
 * {@link android.app.LoaderManager} pattern.
 */

public class ScheduleProvider extends ContentProvider {
    private static final String TAG = "ScheduleProvider";

    private ScheduleDatabase mOpenHelper;

    private ScheduleProviderUriMatcher mUriMatcher;

    /**
     * Providing important state information to be included in bug reports.
     * <p>
     * !!! Remember !!! Any important data logged to {@code writer} shouldn't contain personally
     * identifiable information as it can be seen in bugreports.
     */
    @Override
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        Context context = getContext();

        // Using try/catch block in case there are issues retrieving information to log.
        try {
            // Calling append in multiple calls is typically better than creating net new strings to
            // pass to method invocations.
            writer.print("Last sync attempted: ");
            writer.println(new java.util.Date(SettingsUtils.getLastSyncAttemptedTime(context)));
            writer.print("Last sync successful: ");
            writer.println(new java.util.Date(SettingsUtils.getLastSyncSucceededTime(context)));
            writer.print("Current sync interval: ");
            writer.println(SettingsUtils.getCurSyncInterval(context));
            writer.print("Is an account active: ");
            writer.println(AccountUtils.hasActiveAccount(context));
            boolean canGetAuthToken = !TextUtils.isEmpty(AccountUtils.getAuthToken(context));
            writer.print("Can an auth token be retrieved: ");
            writer.println(canGetAuthToken);

        } catch (Exception exception) {
            writer.append("Exception while dumping state: ");
            exception.printStackTrace(writer);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ScheduleDatabase(getContext());
        mUriMatcher = new ScheduleProviderUriMatcher();
        return true;
    }

    private void deleteDatabase() {
        mOpenHelper.close();
        Context context = getContext();
        ScheduleDatabase.deleteDatabase(context);
        mOpenHelper = new ScheduleDatabase(getContext());
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        return matchingUriEnum.contentType;
    }

    /**
     * Returns a tuple of question marks. For example, if {@code count} is 3, returns "(?,?,?)".
     */
    private String makeQuestionMarkTuple(int count) {
        if (count < 1) {
            return "()";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(?");
        for (int i = 1; i < count; i++) {
            stringBuilder.append(",?");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    /**
     * Adds the {@code tagsFilter} query parameter to the given {@code builder}. This query
     * parameter is used by the {@link com.yalin.googleio2016.explore.ExploreSessionsActivity}
     * when the user makes a selection containing multiple filters.
     */
    private void addTagsFilter(SelectionBuilder builder, String tagsFilter, String numCategories) {
        // Note: for context, remember that session queries are done on a join of sessions
        // and the sessions_tags relationship table, and are GROUP'ed BY the session ID.
        String[] requiredTags = tagsFilter.split(",");
        if (requiredTags.length == 0) {
            // filtering by 0 tags -- no-op
        } else if (requiredTags.length == 1) {
            // filtering by only one tag, so a simple WHERE clause suffices
            builder.where(Tags.TAG_ID + "=?", requiredTags[0]);
        } else {
            // Filtering by multiple tags, so we must add a WHERE clause with an IN operator,
            // and add a HAVING statement to exclude groups that fall short of the number
            // of required tags. For example, if requiredTags is { "X", "Y", "Z" }, and a certain
            // session only has tags "X" and "Y", it will be excluded by the HAVING statement.
            int categories = 1;
            if (numCategories != null && TextUtils.isDigitsOnly(numCategories)) {
                try {
                    categories = Integer.parseInt(numCategories);
                    LogUtil.d(TAG, "Categories being used " + categories);
                } catch (Exception ex) {
                    LogUtil.e(TAG, "exception parsing categories ", ex);
                }
            }
            String questionMarkTuple = makeQuestionMarkTuple(requiredTags.length);
            builder.where(Tags.TAG_ID + " IN " + questionMarkTuple, requiredTags);
            builder.having(
                    "COUNT(" + Qualified.SESSIONS_SESSION_ID + ") >= " + categories);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String tagsFilter = uri.getQueryParameter(Sessions.QUERY_PARAMETER_TAG_FILTER);
        String categories = uri.getQueryParameter(Sessions.QUERY_PARAMETER_CATEGORIES);

        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);

        // Avoid the expensive string concatenation below if not loggable.
        if (LogUtil.isLoggable()) {
            LogUtil.d(TAG, "uri=" + uri + " code=" + matchingUriEnum.code + " proj=" +
                    Arrays.toString(projection) + " selection=" + selection + " args="
                    + Arrays.toString(selectionArgs) + ")");
        }

        switch (matchingUriEnum) {
            default: {
                final SelectionBuilder builder = buildExpandedSelection(uri, matchingUriEnum.code);

                if (!TextUtils.isEmpty(tagsFilter) && !TextUtils.isEmpty(categories)) {
                    addTagsFilter(builder, tagsFilter, categories);
                }
                boolean distinct = ScheduleContractHelper.isQueryDistinct(uri);

                Cursor cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, distinct, projection, sortOrder, null);

                Context context = getContext();

                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
            }
            case SEARCH_TOPICS_SESSION: {
                if (selectionArgs == null || selectionArgs.length == 0) {
                    return createMergedSearchCursor(null, null);
                }
                String selectionArg = selectionArgs[0] == null ? "" : selectionArgs[0];
                // First we query the Tags table to find any tags that match the given query
                Cursor tags = query(Tags.CONTENT_URI, SearchTopicsSessions.TOPIC_TAG_PROJECTION,
                        SearchTopicsSessions.TOPIC_TAG_SELECTION,
                        new String[]{Config.Tags.CATEGORY_TRACK, selectionArg + "%"},
                        SearchTopicsSessions.TOPIC_TAG_SORT);
                // Then we query the sessions_search table and get a list of sessions that match
                // the given keywords.
                Cursor search = null;
                if (selectionArgs[0] != null) { // dont query if there was no selectionArg.
                    search = query(ScheduleContract.Sessions.buildSearchUri(selectionArg),
                            SearchTopicsSessions.SEARCH_SESSIONS_PROJECTION,
                            null, null,
                            Sessions.SORT_BY_TYPE_THEN_TIME);
                }
                // Now that we have two cursors, we merge the cursors and return a unified view
                // of the two result sets.
                return createMergedSearchCursor(tags, search);
            }
        }
    }

    /**
     * Create a {@link MatrixCursor} given the tags and search cursors.
     *
     * @param tags   Cursor with the projection {@link SearchTopicsSessions#TOPIC_TAG_PROJECTION}.
     * @param search Cursor with the projection
     *               {@link SearchTopicsSessions#SEARCH_SESSIONS_PROJECTION}.
     * @return Returns a MatrixCursor always with {@link SearchTopicsSessions#DEFAULT_PROJECTION}
     */
    private Cursor createMergedSearchCursor(Cursor tags, Cursor search) {
        // How big should our MatrixCursor be?
        int maxCount = (tags == null ? 0 : tags.getCount()) +
                (search == null ? 0 : search.getCount());

        MatrixCursor matrixCursor = new MatrixCursor(
                SearchTopicsSessions.DEFAULT_PROJECTION, maxCount);

        // Iterate over the tags cursor and add rows.
        if (tags != null && tags.moveToFirst()) {
            do {
                matrixCursor.addRow(
                        new Object[]{
                                tags.getLong(0),
                                tags.getLong(1), /*tag_id*/
                                "{" + tags.getString(2) + "}", /*search_snippet*/
                                1}); /*is_topic_tag*/
            } while (tags.moveToNext());
        }
        // Iterate over the search cursor and add rows.
        if (search != null && search.moveToFirst()) {
            do {
                matrixCursor.addRow(
                        new Object[]{
                                search.getLong(0),
                                search.getString(1),
                                search.getString(2),  /*search_snippet*/
                                0}); /*is_topic_tag*/
            } while (search.moveToNext());
        }

        return matrixCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LogUtil.d(TAG, "insert(uri=" + uri + ", values=" + values.toString()
                + ", account=" + getCurrentAccountName(uri, false) + ")");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        if (matchingUriEnum.table != null) {
            try {
                db.insertOrThrow(matchingUriEnum.table, null, values);
                notifyChange(uri);
            } catch (SQLiteConstraintException exception) {
                // Leaving this here as it's handy to to breakpoint on this throw when debugging a
                // bootstrap file issue.
                throw exception;
            }
        }

        switch (matchingUriEnum) {
            case BLOCKS: {
                return Blocks.buildBlockUri(values.getAsString(Blocks.BLOCK_ID));
            }
            case CARDS: {
                return ScheduleContract.Cards.buildCardUri(values.getAsString(
                        ScheduleContract.Cards.CARD_ID));
            }
            case TAGS: {
                return Tags.buildTagUri(values.getAsString(Tags.TAG_ID));
            }
            case SESSIONS: {
                return Sessions.buildSessionUri(values.getAsString(Sessions.SESSION_ID));
            }
            case SESSIONS_ID_SPEAKERS: {
                return Speakers.buildSpeakerUri(values.getAsString(SessionsSpeakers.SPEAKER_ID));
            }
            case SESSIONS_ID_TAGS: {
                return Tags.buildTagUri(values.getAsString(Tags.TAG_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String accountName = getCurrentAccountName(uri, false);
        LogUtil.d(TAG, "delete(uri=" + uri + ", account=" + accountName + ")");
        if (uri == ScheduleContract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);

        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String accountName = getCurrentAccountName(uri, false);
        LogUtil.d(TAG, "update(uri=" + uri + ", values=" + values.toString()
                + ", account=" + accountName + ")");

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        if (matchingUriEnum == ScheduleUriEnum.SEARCH_INDEX) {
            // update the search index
            ScheduleDatabase.updateSessionSearchIndex(db);
            return 1;
        }

        // TODO: 2016/12/8  add other update
        return 0;
    }

    /**
     * Build a simple {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchUri(uri);
        // The main Uris, corresponding to the root of each type of Uri, do not have any selection
        // criteria so the full table is used. The others apply a selection criteria.
        switch (matchingUriEnum) {
            case BLOCKS:
            case CARDS:
            case TAGS:
            case SESSIONS:
                return builder.table(matchingUriEnum.table);
            case SESSIONS_ID: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(Tables.SESSIONS)
                        .where(Sessions.SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_SPEAKERS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(Tables.SESSIONS_SPEAKERS)
                        .where(Sessions.SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_TAGS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(Tables.SESSIONS_TAGS)
                        .where(Sessions.SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_MY_SCHEDULE: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(Tables.MY_SCHEDULE)
                        .where(ScheduleContract.MyScheduleColumns.SESSION_ID + "=?", sessionId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + uri);
            }
        }
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        ScheduleUriEnum matchingUriEnum = mUriMatcher.matchCode(match);
        if (matchingUriEnum == null) {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        switch (matchingUriEnum) {
            case BLOCKS: {
                return builder.table(Tables.BLOCKS);
            }
            case CARDS: {
                return builder.table(Tables.CARDS);
            }
            case TAGS: {
                return builder.table(Tables.TAGS);
            }
            case SESSIONS: {
                // We query sessions on the joined table of sessions with rooms and tags.
                // Since there may be more than one tag per session, we GROUP BY session ID.
                // The starred sessions ("my schedule") are associated with a user, so we
                // use the current user to select them properly
                return builder.table(Tables.SESSIONS_JOIN_ROOMS_TAGS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, Tables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                        .map(Sessions.SESSION_IN_MY_SCHEDULE, "IFNULL(in_schedule,0)")
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SESSIONS_MY_SCHEDULE: {
                return builder.table(Tables.SESSIONS_JOIN_ROOMS_TAGS_FEEDBACK_MYSCHEDULE,
                        getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, Tables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                        .map(Sessions.HAS_GIVEN_FEEDBACK, Subquery.SESSION_HAS_GIVEN_FEEDBACK)
                        .map(Sessions.SESSION_IN_MY_SCHEDULE, "IFNULL(in_schedule, 0)")
                        .where("( " + Sessions.SESSION_IN_MY_SCHEDULE + "=1 OR " +
                                Sessions.SESSION_TAGS +
                                " LIKE '%" + Config.Tags.SPECIAL_KEYNOTE + "%' )")
                        .groupBy(Qualified.SESSIONS_SESSION_ID);
            }
            case SESSIONS_SEARCH: {
                final String query = Sessions.getSearchQuery(uri);
                return builder.table(Tables.SESSIONS_SEARCH_JOIN_SESSIONS_ROOMS,
                        getCurrentAccountName(uri, true))
                        .map(Sessions.SEARCH_SNIPPET, Subquery.SESSIONS_SNIPPET)
                        .mapToTable(Sessions._ID, Tables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                        .map(Sessions.SESSION_IN_MY_SCHEDULE, "IFNULL(in_schedule, 0)")
                        .where(ScheduleDatabase.SessionsSearchColumns.BODY + " MATCH ?", query);
            }
            case SESSIONS_ID: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(Tables.SESSIONS_JOIN_ROOMS, getCurrentAccountName(uri, true))
                        .mapToTable(Sessions._ID, Tables.SESSIONS)
                        .mapToTable(Sessions.ROOM_ID, Tables.SESSIONS)
                        .mapToTable(Sessions.SESSION_ID, Tables.SESSIONS)
                        .map(Sessions.SESSION_IN_MY_SCHEDULE, "IFNULL(in_schedule,0)")
                        .where(Qualified.SESSIONS_SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_SPEAKERS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(Tables.SESSIONS_SPEAKERS_JOIN_SPEAKERS)
                        .mapToTable(Speakers._ID, Tables.SPEAKERS)
                        .mapToTable(Speakers.SPEAKER_ID, Tables.SPEAKERS)
                        .where(Qualified.SESSIONS_SPEAKERS_SESSION_ID + "=?", sessionId);
            }
            case SESSIONS_ID_TAGS: {
                final String sessionId = Sessions.getSessionId(uri);
                return builder.table(Tables.SESSIONS_TAGS_JOIN_TAGS)
                        .mapToTable(Tags._ID, Tables.TAGS)
                        .mapToTable(Tags.TAG_ID, Tables.TAGS)
                        .where(Qualified.SESSIONS_TAGS_SESSION_ID + "=?", sessionId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /**
     * Notifies the system that the given {@code uri} data has changed.
     * <p/>
     * We only notify changes if the uri wasn't called by the sync adapter, to avoid issuing a large
     * amount of notifications while doing a sync. The
     * {@link com.yalin.googleio2016.sync.ConferenceDataHandler} notifies all top level
     * conference paths once the conference data sync is done, and the
     * {@link com.yalin.googleio2016.sync.userdata.AbstractUserDataSyncHelper} notifies all
     * user data related paths once the user data sync is done.
     */
    private void notifyChange(Uri uri) {
        if (!ScheduleContractHelper.isUriCalledFromSyncAdapter(uri)) {
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(uri, null);
                // Widgets can't register content observers so we refresh widgets separately.
                // TODO: 2016/11/30
//                context.sendBroadcast(ScheduleWidgetProvider.getRefreshBroadcastIntent(context, false));
            }
        }
    }

    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    private String getCurrentAccountName(Uri uri, boolean sanitize) {
        String accountName = ScheduleContractHelper.getOverrideAccountName(uri);
        if (accountName == null) {
            accountName = AccountUtils.getActiveAccountName(getContext());
        }
        if (sanitize) {
            // sanitize accountName when concatenating (http://xkcd.com/327/)
            accountName = (accountName != null) ? accountName.replace("'", "''") : null;
        }
        return accountName;
    }

    private interface Subquery {
        String SESSION_HAS_GIVEN_FEEDBACK = "(SELECT COUNT(1) FROM "
                + Tables.FEEDBACK + " WHERE " + Qualified.FEEDBACK_SESSION_ID + "="
                + Qualified.SESSIONS_SESSION_ID + ")";

        String SESSIONS_SNIPPET = "snippet(" + Tables.SESSIONS_SEARCH + ",'{','}','\u2026')";
    }

    /**
     * {@link ScheduleContract} fields that are fully qualified with a specific
     * parent {@link Tables}. Used when needed to work around SQL ambiguity.
     */
    private interface Qualified {
        String SESSIONS_SESSION_ID = Tables.SESSIONS + "." + Sessions.SESSION_ID;
        String SESSIONS_ROOM_ID = Tables.SESSIONS + "." + Sessions.ROOM_ID;
        String SESSIONS_TAGS_SESSION_ID = Tables.SESSIONS_TAGS + "."
                + ScheduleDatabase.SessionsTags.SESSION_ID;

        String SESSIONS_SPEAKERS_SESSION_ID = Tables.SESSIONS_SPEAKERS + "."
                + SessionsSpeakers.SESSION_ID;

        String SESSIONS_SPEAKERS_SPEAKER_ID = Tables.SESSIONS_SPEAKERS + "."
                + SessionsSpeakers.SPEAKER_ID;

        String FEEDBACK_SESSION_ID = Tables.FEEDBACK + "." + Feedback.SESSION_ID;
    }
}
