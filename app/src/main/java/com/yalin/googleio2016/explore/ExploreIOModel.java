package com.yalin.googleio2016.explore;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.Model;
import com.yalin.googleio2016.archframework.ModelWithLoaderManager;
import com.yalin.googleio2016.archframework.QueryEnum;
import com.yalin.googleio2016.archframework.UserActionEnum;
import com.yalin.googleio2016.explore.data.EventCard;
import com.yalin.googleio2016.explore.data.EventData;
import com.yalin.googleio2016.explore.data.ItemGroup;
import com.yalin.googleio2016.explore.data.LiveStreamData;
import com.yalin.googleio2016.explore.data.MessageData;
import com.yalin.googleio2016.explore.data.SessionData;
import com.yalin.googleio2016.model.TagMetadata;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.settings.ConfMessageCardUtils;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.TagUtils;
import com.yalin.googleio2016.util.TimeUtils;
import com.yalin.googleio2016.util.WiFiUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * This is an implementation of a {@link Model} that queries the sessions at Google I/O and extracts
 * the data needed to present the Explore I/O user interface.
 */
public class ExploreIOModel extends ModelWithLoaderManager<ExploreIOModel.ExploreIOQueryEnum,
        ExploreIOModel.ExploreIOUserActionEnum> {
    private static final String TAG = "ExploreIOModel";

    private final Context mContext;

    @NonNull
    private EventData mEventData = new EventData();
    private SessionData mKeynoteData;
    private LiveStreamData mLiveStreamData;
    private List<ItemGroup> mOrderedTracks;
    private Uri mSessionsUri;
    private TagMetadata mTagMetadata;

    /**
     * Theme groups loaded from the database pre-randomly filtered and stored by topic name. Not
     * shown in current design.
     */
    private Map<String, ItemGroup> mThemes = new HashMap<>();
    /**
     * Topic groups loaded from the database pre-randomly filtered and stored by topic name.
     */
    private Map<String, ItemGroup> mTracks = new HashMap<>();

    public ExploreIOModel(Context context, Uri sessionUri, LoaderManager loaderManager) {
        super(ExploreIOQueryEnum.values(), ExploreIOUserActionEnum.values(), loaderManager);
        mContext = context;
        mSessionsUri = sessionUri;
    }

    @Override
    public void cleanUp() {
        mThemes.clear();
        mThemes = null;
        mTracks.clear();
        mTracks = null;
        mOrderedTracks.clear();
        mOrderedTracks = null;
        mKeynoteData = null;
        mLiveStreamData = null;
    }

    @Override
    public Loader<Cursor> createCursorLoader(ExploreIOQueryEnum query, Bundle args) {
        CursorLoader loader = null;
        switch (query) {
            case SESSIONS:
                // Create and return the Loader.
                loader = getCursorLoaderInstance(mContext, mSessionsUri,
                        ExploreIOQueryEnum.SESSIONS.getProjection(), null, null,
                        ScheduleContract.Sessions.SORT_BY_TYPE_THEN_TIME);
                break;
            case TAGS:
                LogUtil.d(TAG, "Starting sessions tag query");
                loader = TagMetadata.createCursorLoader(mContext);
                break;
            case CARDS:
                String currentTime = TimeUtils.getCurrentTime(mContext) + "";
                LogUtil.d(TAG, "Starting cards query: " + currentTime);
                loader = getCursorLoaderInstance(mContext, ScheduleContract.Cards.CONTENT_URI,
                        ExploreIOQueryEnum.CARDS.getProjection(),
                        " ? > " + ScheduleContract.Cards.DISPLAY_START_DATE + " AND ? < " +
                                ScheduleContract.Cards.DISPLAY_END_DATE + " AND " +
                                ScheduleContract.Cards.ACTION_TYPE + " IN ('" +
                                EventCard.ACTION_TYPE_LINK + "', '" +
                                EventCard.ACTION_TYPE_MAP + "', '" +
                                EventCard.ACTION_TYPE_SESSION + "')",
                        new String[]{currentTime, currentTime},
                        ScheduleContract.Cards.CARD_ID);
                break;
        }
        return loader;
    }

    public CursorLoader getCursorLoaderInstance(Context context, Uri uri, String[] projection,
                                                String selection, String[] selectionArgs,
                                                String sortOrder) {
        return new CursorLoader(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    @NonNull
    public EventData getEventData() {
        return mEventData;
    }

    public SessionData getKeynoteData() {
        return mKeynoteData;
    }

    public LiveStreamData getLiveStreamData() {
        return mLiveStreamData;
    }


    /**
     * @return the tracks ordered alphabetically. The ordering can only happen if the query {@link
     * com.yalin.googleio2016.explore.ExploreIOModel.ExploreIOQueryEnum#TAGS} has returned,
     * which can be checked by calling {@link #getTagMetadata()}.
     */
    public Collection<ItemGroup> getOrderedTracks() {
        if (mOrderedTracks != null) {
            return mOrderedTracks;
        }
        mOrderedTracks = new ArrayList<>(getTracks());
        for (ItemGroup item : mOrderedTracks) {
            if (item.getTitle() == null) {
                item.formatTitle(mTagMetadata);
            }
        }

        // Order the tracks by title.
        Collections.sort(mOrderedTracks, new Comparator<ItemGroup>() {
            @Override
            public int compare(final ItemGroup lhs, final ItemGroup rhs) {
                if (lhs.getTitle() == null) {
                    return 1;
                } else if (rhs.getTitle() == null) {
                    return -1;
                }
                return lhs.getTitle().compareTo(rhs.getTitle());
            }
        });

        return mOrderedTracks;
    }

    public TagMetadata getTagMetadata() {
        return mTagMetadata;
    }

    private Collection<ItemGroup> getTracks() {
        return mTracks.values();
    }

    /**
     * Get the list of {@link MessageData} to be displayed to the user, based upon time, location
     * etc.
     *
     * @return messages to be displayed.
     */
    public List<MessageData> getMessages() {
        final List<MessageData> messagesToDisplay = new ArrayList<>();

        if (shouldShowCard(ConfMessageCardUtils.ConfMessageCard.SESSION_NOTIFICATIONS)) {
            messagesToDisplay.add(MessageCardHelper.getNotificationsOptInMessageData());
        }
        if (SettingsUtils.isAttendeeAtVenue(mContext)) {
            // Users are required to opt in or out of whether they want conference message cards
            if (!ConfMessageCardUtils.hasAnsweredConfMessageCardsPrompt(mContext)) {
                // User has not answered whether they want to opt in.
                // Build a opt-in/out card.
                messagesToDisplay.add(MessageCardHelper.getConferenceOptInMessageData());
                return messagesToDisplay;
            }

            if (ConfMessageCardUtils.isConfMessageCardsEnabled(mContext)) {
                LogUtil.d(TAG, "Conf cards Enabled");
                // User has answered they want to opt in AND the message cards are enabled.
                ConfMessageCardUtils.enableActiveCards(mContext);

                // Note that for these special cards, we'll never show more than one at a time
                // to prevent overloading the user with messagesToDisplay.
                // We want each new message to be notable.
                if (shouldShowCard(ConfMessageCardUtils.ConfMessageCard.WIFI_PRELOAD)) {
                    // Check whether a wifi setup card should be offered.
                    if (WiFiUtils.shouldOfferToSetupWifi(mContext, true)) {
                        // Build card asking users whether they want to enable wifi.
                        messagesToDisplay.add(MessageCardHelper.getWifiSetupMessageData());
                        return messagesToDisplay;
                    }
                }

                if (messagesToDisplay.size() < 1) {
                    LogUtil.d(TAG, "Simple cards");
                    List<ConfMessageCardUtils.ConfMessageCard> simpleCards =
                            ConfMessageCardUtils.ConfMessageCard.getActiveSimpleCards(mContext);
                    // Only show a single card at a time.
                    if (simpleCards.size() > 0) {
                        messagesToDisplay.add(MessageCardHelper.getSimpleMessageCardData(
                                simpleCards.get(0)));
                    }
                }
            }
        }
        return messagesToDisplay;
    }

    @Override
    public void processUserAction(ExploreIOUserActionEnum action,
                                  @Nullable Bundle args, UserActionCallback callback) {
        /**
         * The only user action in this model fires off a query (using {@link #KEY_RUN_QUERY_ID},
         * so this method isn't used.
         */
    }

    @Override
    public boolean readDataFromCursor(Cursor cursor, ExploreIOQueryEnum query) {
        switch (query) {
            case SESSIONS:
                readDataFromSessionsCursor(cursor);
                return true;
            case TAGS:
                readDataFromTagsCursor(cursor);
                return true;
            case CARDS:
                readDataFromCardsCursor(cursor);
                return true;
        }
        return false;
    }

    /**
     * As we go through the session query results we will be collecting X numbers of session data
     * per Topic and Y numbers of sessions per Theme. When new topics or themes are seen a group
     * will be created.
     * <p/>
     * As we iterate through the list of sessions we are also watching out for the keynote and any
     * live sessions streaming right now.
     */
    private void readDataFromSessionsCursor(Cursor cursor) {
        LogUtil.d(TAG, "Reading session data from cursor.");

        boolean atVenue = SettingsUtils.isAttendeeAtVenue(mContext);

        LiveStreamData liveStreamData = new LiveStreamData();
        Map<String, ItemGroup> trackGroups = new HashMap<>();
        Map<String, ItemGroup> themeGroups = new HashMap<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                SessionData session = new SessionData();
                populateSessionFromCursorRow(session, cursor);

                if (isSessionDataInvalid(session)) {
                    continue;
                }

                if (!atVenue &&
                        (!session.isLiveStreamAvailable()) && !session.isVideoAvailable()) {
                    // Skip the opportunity to present the session for those not on site
                    // since it won't be viewable as there is neither a live stream nor video
                    // available.
                    continue;
                }

                String tags = session.getTags();

                if (Config.Tags.SPECIAL_KEYNOTE.equals(session.getMainTag())) {
                    SessionData keynoteData = new SessionData();
                    populateSessionFromCursorRow(keynoteData, cursor);
                    rewriteKeynoteDetails(keynoteData);
                    mKeynoteData = keynoteData;
                } else if (session.isLiveStreamNow(mContext)) {
                    liveStreamData.addSessionData(session);
                }

                if (!TextUtils.isEmpty(tags)) {
                    StringTokenizer tagsTokenizer = new StringTokenizer(tags, ",");
                    while (tagsTokenizer.hasMoreTokens()) {
                        String rawTag = tagsTokenizer.nextToken();
                        if (TagUtils.isTrackTag(rawTag)) {
                            ItemGroup trackGroup = trackGroups.get(rawTag);
                            if (trackGroup == null) {
                                trackGroup = new ItemGroup();
                                trackGroup.setTitleId(rawTag);
                                trackGroup.setId(rawTag);
                                if (mTagMetadata != null && mTagMetadata.getTag(rawTag) != null) {
                                    trackGroup
                                            .setPhotoUrl(mTagMetadata.getTag(rawTag).getPhotoUrl());
                                }
                                trackGroups.put(rawTag, trackGroup);
                            }
                            trackGroup.addSessionData(session);
                        } else if (TagUtils.isThemeTag(rawTag)) {
                            ItemGroup themeGroup = themeGroups.get(rawTag);
                            if (themeGroup == null) {
                                themeGroup = new ItemGroup();
                                themeGroup.setTitleId(rawTag);
                                themeGroup.setId(rawTag);
                                if (mTagMetadata != null && mTagMetadata.getTag(rawTag) != null) {
                                    themeGroup
                                            .setPhotoUrl(mTagMetadata.getTag(rawTag).getPhotoUrl());
                                }
                                themeGroups.put(rawTag, themeGroup);
                            }
                            themeGroup.addSessionData(session);
                        }
                    }
                }
            } while (cursor.moveToNext());
        }

        if (liveStreamData.getSessions().size() > 0) {
            mLiveStreamData = liveStreamData;
        }
        mThemes = themeGroups;
        mTracks = trackGroups;
        mOrderedTracks = null;
    }

    private void readDataFromTagsCursor(Cursor cursor) {
        LogUtil.d(TAG, "TAGS query loaded");

        if (cursor != null && cursor.moveToFirst()) {
            mTagMetadata = new TagMetadata(cursor);
        }

        addPhotoUrlToTopicsAndThemes();
    }

    private void readDataFromCardsCursor(Cursor cursor) {
        LogUtil.d(TAG, "Cards query loaded");
        mEventData = new EventData();
        if (cursor != null && cursor.moveToFirst()) {
            LogUtil.d(TAG, "Read card data");
            do {
                EventCard card = EventCard.fromCursorRow(cursor);
                if (card != null) {
                    mEventData.addEventCard(card);
                }
            } while (cursor.moveToNext());
            LogUtil.d(TAG, "Cards loaded: " + mEventData.getCards().size());
        } else {
            LogUtil.e(TAG, "No Cards data");
        }
    }

    /**
     * A session missing title, description, id, or image isn't eligible for the Explore screen.
     */
    private boolean isSessionDataInvalid(SessionData session) {
        return TextUtils.isEmpty(session.getSessionName()) ||
                TextUtils.isEmpty(session.getDetails()) ||
                TextUtils.isEmpty(session.getSessionId()) ||
                TextUtils.isEmpty(session.getImageUrl());
    }

    private void populateSessionFromCursorRow(SessionData session, Cursor cursor) {
        session.updateData(mContext,
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_TITLE)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_ABSTRACT)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_ID)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_PHOTO_URL)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_MAIN_TAG)),
                cursor.getLong(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_START)),
                cursor.getLong(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_END)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_LIVESTREAM_ID)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_YOUTUBE_URL)),
                cursor.getString(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_TAGS)),
                cursor.getLong(cursor.getColumnIndex(
                        ScheduleContract.Sessions.SESSION_IN_MY_SCHEDULE)) == 1L);
    }

    private void rewriteKeynoteDetails(SessionData keynoteData) {
        long startTime, endTime, currentTime;
        currentTime = TimeUtils.getCurrentTime(mContext);
        if (keynoteData.getStartDate() != null) {
            startTime = keynoteData.getStartDate().getTimeInMillis();
        } else {
            LogUtil.d(TAG, "Keynote start time wasn't set");
            startTime = 0;
        }
        if (keynoteData.getEndDate() != null) {
            endTime = keynoteData.getEndDate().getTimeInMillis();
        } else {
            LogUtil.d(TAG, "Keynote end time wasn't set");
            endTime = Long.MAX_VALUE;
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (currentTime >= startTime && currentTime < endTime) {
            stringBuilder.append(mContext.getString(R.string
                    .live_now));
        } else {
            stringBuilder.append(
                    TimeUtils.formatShortDateTime(mContext, keynoteData.getStartDate().getTime()));
        }
        keynoteData.setDetails(stringBuilder.toString());
    }

    private void addPhotoUrlToTopicsAndThemes() {
        if (mTracks != null) {
            for (ItemGroup topic : mTracks.values()) {
                if (mTagMetadata != null && mTagMetadata.getTag(topic.getTitleId()) != null) {
                    topic.setPhotoUrl(mTagMetadata.getTag(topic.getTitleId()).getPhotoUrl());
                }
            }
        }
        if (mThemes != null) {
            for (ItemGroup theme : mThemes.values()) {
                if (mTagMetadata != null && mTagMetadata.getTag(theme.getTitleId()) != null) {
                    theme.setPhotoUrl(mTagMetadata.getTag(theme.getTitleId()).getPhotoUrl());
                }
            }
        }
    }

    /**
     * Check if this card should be shown and hasn't previously been dismissed.
     *
     * @return {@code true} if the given message card should be displayed.
     */
    private boolean shouldShowCard(ConfMessageCardUtils.ConfMessageCard card) {
        return ConfMessageCardUtils.shouldShowConfMessageCard(mContext, card) &&
                !ConfMessageCardUtils.hasDismissedConfMessageCard(mContext, card);
    }

    public enum ExploreIOQueryEnum implements QueryEnum {
        SESSIONS(0x1, new String[]{
                ScheduleContract.Sessions.SESSION_ID,
                ScheduleContract.Sessions.SESSION_TITLE,
                ScheduleContract.Sessions.SESSION_ABSTRACT,
                ScheduleContract.Sessions.SESSION_TAGS,
                ScheduleContract.Sessions.SESSION_MAIN_TAG,
                ScheduleContract.Sessions.SESSION_PHOTO_URL,
                ScheduleContract.Sessions.SESSION_START,
                ScheduleContract.Sessions.SESSION_END,
                ScheduleContract.Sessions.SESSION_LIVESTREAM_ID,
                ScheduleContract.Sessions.SESSION_YOUTUBE_URL,
                ScheduleContract.Sessions.SESSION_IN_MY_SCHEDULE,
                ScheduleContract.Sessions.SESSION_START,
                ScheduleContract.Sessions.SESSION_END,
        }),

        TAGS(0x2, new String[]{
                ScheduleContract.Tags.TAG_ID,
                ScheduleContract.Tags.TAG_NAME,
        }),

        CARDS(0x3, new String[]{
                ScheduleContract.Cards.CARD_ID,
                ScheduleContract.Cards.TITLE,
                ScheduleContract.Cards.TEXT_COLOR,
                ScheduleContract.Cards.MESSAGE,
                ScheduleContract.Cards.DISPLAY_END_DATE,
                ScheduleContract.Cards.ACTION_COLOR,
                ScheduleContract.Cards.ACTION_URL,
                ScheduleContract.Cards.ACTION_TEXT,
                ScheduleContract.Cards.BACKGROUND_COLOR,
                ScheduleContract.Cards.DISPLAY_START_DATE,
                ScheduleContract.Cards.ACTION_TYPE,
                ScheduleContract.Cards.ACTION_EXTRA
        });

        private int id;
        private String[] projection;

        ExploreIOQueryEnum(int id, String[] projection) {
            this.id = id;
            this.projection = projection;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return projection;
        }
    }

    public enum ExploreIOUserActionEnum implements UserActionEnum {
        RELOAD(1);

        private int id;

        ExploreIOUserActionEnum(int id) {
            this.id = id;
        }


        @Override
        public int getId() {
            return id;
        }
    }
}
