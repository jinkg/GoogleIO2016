package com.yalin.googleio2016.explore;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.ModelWithLoaderManager;
import com.yalin.googleio2016.archframework.PresenterImpl;
import com.yalin.googleio2016.archframework.UpdatableView;
import com.yalin.googleio2016.explore.ExploreIOModel.ExploreIOQueryEnum;
import com.yalin.googleio2016.explore.ExploreIOModel.ExploreIOUserActionEnum;
import com.yalin.googleio2016.explore.data.EventData;
import com.yalin.googleio2016.explore.data.ItemGroup;
import com.yalin.googleio2016.explore.data.LiveStreamData;
import com.yalin.googleio2016.explore.data.MessageData;
import com.yalin.googleio2016.explore.data.SessionData;
import com.yalin.googleio2016.injection.ModelProvider;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.session.SessionDetailActivity;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.ui.widget.recyclerview.ItemMarginDecoration;
import com.yalin.googleio2016.ui.widget.recyclerview.UpdatableAdapter;
import com.yalin.googleio2016.util.ImageLoader;
import com.yalin.googleio2016.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * YaLin
 * 2016/11/25.
 * <p>
 * Display the Explore I/O cards. There are three styles of cards, which are referred to as Groups
 * by the CollectionView implementation.
 * <p/>
 * <ul> <li>The live-streaming session cards.</li> <li>Time sensitive message cards.</li> <li>Session
 * topic cards.</li> </ul>
 * <p/>
 * Only the final group of cards is dynamically loaded from a {@link
 * android.content.ContentProvider}.
 */
public class ExploreIOFragment extends Fragment
        implements UpdatableView<ExploreIOModel, ExploreIOQueryEnum, ExploreIOUserActionEnum> {

    /**
     * Used to load images asynchronously on a background thread.
     */
    private ImageLoader mImageLoader;

    /**
     * RecyclerView containing a stream of cards to display to the user.
     */
    private RecyclerView mCardList = null;

    /**
     * Adapter for providing data for the stream of cards.
     */
    private ExploreAdapter mAdapter;

    /**
     * Empty view displayed when {@code mCardList} is empty.
     */
    private View mEmptyView;

    private List<UserActionListener> mListeners = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_explore_io, container, false);
        mCardList = (RecyclerView) root.findViewById(R.id.explore_card_list);
        mCardList.setHasFixedSize(true);
        final int cardVerticalMargin = getResources().getDimensionPixelSize(R.dimen.spacing_normal);
        mCardList.addItemDecoration(new ItemMarginDecoration(0, cardVerticalMargin,
                0, cardVerticalMargin));
        mEmptyView = root.findViewById(android.R.id.empty);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImageLoader = new ImageLoader(getActivity(), R.drawable.io_logo);
        initPresenter();
    }

    @Override
    public void displayData(ExploreIOModel model, ExploreIOQueryEnum query) {
        // Only display data when the tag metadata is available.
        if (model.getTagMetadata() != null) {
            if (mAdapter == null) {
                mAdapter = new ExploreAdapter(getActivity(), model, mImageLoader);
                mCardList.setAdapter(mAdapter);
            } else {
                mAdapter.update(model);
            }
            mEmptyView.setVisibility(mAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void displayErrorMessage(ExploreIOQueryEnum query) {
        // No UI changes when error with query
    }

    @Override
    public void displayUserActionResult(ExploreIOModel mode, ExploreIOUserActionEnum userAction, boolean success) {
        switch (userAction) {
            case RELOAD:
                displayData(mode, ExploreIOQueryEnum.SESSIONS);
                break;
        }
    }

    @Override
    public Uri getDataUri(ExploreIOQueryEnum query) {
        switch (query) {
            case SESSIONS:
                return ScheduleContract.Sessions.CONTENT_URI;
            default:
                return Uri.EMPTY;
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void addListener(UserActionListener toAdd) {
        mListeners.add(toAdd);
    }

    private void initPresenter() {
        ExploreIOModel model = ModelProvider.provideExploreIOModel(
                getDataUri(ExploreIOQueryEnum.SESSIONS), getContext(),
                getLoaderManager());
        PresenterImpl presenter = new PresenterImpl(model, this,
                ExploreIOUserActionEnum.values(), ExploreIOQueryEnum.values());
        presenter.loadInitialQueries();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();

        // TODO: 2016/11/30 add draw shadow frame layout
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // TODO: 2016/11/30
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // TODO: 2016/11/30
    }

    private void fireReloadEvent() {
        if (!isAdded()) {
            return;
        }
        for (UserActionListener h1 : mListeners) {
            Bundle args = new Bundle();
            args.putInt(ModelWithLoaderManager.KEY_RUN_QUERY_ID,
                    ExploreIOQueryEnum.SESSIONS.getId());
            //noinspection unchecked
            h1.onUserAction(ExploreIOUserActionEnum.RELOAD, args);
        }
    }

    private void fireReloadTagsEvent() {
        if (!isAdded()) {
            return;
        }
        for (UserActionListener h1 : mListeners) {
            Bundle args = new Bundle();
            args.putInt(ModelWithLoaderManager.KEY_RUN_QUERY_ID,
                    ExploreIOQueryEnum.TAGS.getId());
            //noinspection unchecked
            h1.onUserAction(ExploreIOUserActionEnum.RELOAD, args);
        }
    }

    /**
     * Adapter for providing cards (Messages, Keynote, Live Stream and conference Tracks)
     * for the Explore fragment.
     */
    private static class ExploreAdapter
            extends UpdatableAdapter<ExploreIOModel, RecyclerView.ViewHolder> {

        private static final int TYPE_TRACK = 0;

        private static final int TYPE_MESSAGE = 1;

        private static final int TYPE_KEYNOTE = 2;

        private static final int TYPE_LIVE_STREAM = 3;

        private static final int TYPE_EVENT_DATA = 4;

        private static final int LIVE_STREAM_TRACK_ID = R.string.live_now;

        private static final int EVENT_DATA_TRACK_ID = 999;

        // Immutable state
        private final Activity mHost;

        private final LayoutInflater mInflater;

        private final ImageLoader mImageLoader;

        private final RecyclerView.RecycledViewPool mRecycledViewPool;

        // State
        private List mItems;

        // Maps of state keyed on track id
        private SparseArrayCompat<UpdatableAdapter> mTrackSessionsAdapters;

        private SparseArrayCompat<Parcelable> mTrackSessionsState;

        ExploreAdapter(@NonNull Activity activity,
                       @NonNull ExploreIOModel model,
                       @NonNull ImageLoader imageLoader) {
            mHost = activity;
            mImageLoader = imageLoader;
            mInflater = LayoutInflater.from(activity);
            mRecycledViewPool = new RecyclerView.RecycledViewPool();
            mItems = processModel(model);
            setupSessionAdapters(model);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void update(@NonNull ExploreIOModel model) {
            // Attempt to update our data in-place so as not to lose scroll position etc.
            final List newItems = processModel(model);
            boolean changed = false;
            if (newItems.size() != mItems.size()) {
                changed = true;
            } else {
                for (int i = 0; i < newItems.size(); i++) {
                    final Object newCard = newItems.get(i);
                    final Object oldCard = mItems.get(i);
                    if (newCard.equals(oldCard)) {
                        if (newCard instanceof ItemGroup) {
                            final ItemGroup newTrack = (ItemGroup) newCard;
                            mTrackSessionsAdapters.get(getTrackId(newTrack))
                                    .update(newTrack.getSessions());
                        }
                    } else {
                        changed = true;
                        break;
                    }
                }
            }
            if (changed) {
                // Couldn't update existing model, do a full refresh
                mItems = newItems;
                setupSessionAdapters(model);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemViewType(int position) {
            final Object item = mItems.get(position);
            if (item instanceof LiveStreamData) {
                return TYPE_LIVE_STREAM;
            } else if (item instanceof ItemGroup) {
                return TYPE_TRACK;
            } else if (item instanceof MessageData) {
                return TYPE_MESSAGE;
            } else if (item instanceof SessionData) {
                return TYPE_KEYNOTE;
            } else if (item instanceof EventData) {
                return TYPE_EVENT_DATA;
            }
            throw new IllegalArgumentException("Unknown view type.");
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_EVENT_DATA:
                    return createEventViewHolder(parent);
                case TYPE_TRACK:
                    return createTrackViewHolder(parent);
                case TYPE_MESSAGE:
                    return createMessageViewHolder(parent);
                case TYPE_KEYNOTE:
                    return createKeynoteViewHolder(parent);
                case TYPE_LIVE_STREAM:
                    return createLiveStreamViewHolder(parent);
                default:
                    throw new IllegalArgumentException("Unknown view type.");
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case TYPE_TRACK:
                    bindTrack((TrackViewHolder) holder, (ItemGroup) mItems.get(position));
                    break;
                case TYPE_MESSAGE:
                    bindMessage((MessageViewHolder) holder, (MessageData) mItems.get(position));
                    break;
                case TYPE_KEYNOTE:
                    bindKeynote((KeynoteViewHolder) holder, (SessionData) mItems.get(position));
                    break;
                case TYPE_LIVE_STREAM:
                    bindLiveStream((TrackViewHolder) holder, (LiveStreamData) mItems.get(position));
                    break;
                case TYPE_EVENT_DATA:
                    bindEventData((EventDataViewHolder) holder, (EventData) mItems.get(position));
                    break;
            }
        }

        private void bindEventData(final EventDataViewHolder holder, final EventData eventData) {
            int trackId = getTrackId(eventData);
            holder.cards.setAdapter(mTrackSessionsAdapters.get(trackId));
            holder.cards.getLayoutManager().onRestoreInstanceState(
                    mTrackSessionsState.get(trackId));
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            if (holder instanceof TrackViewHolder) {
                final TrackViewHolder trackViewHolder = (TrackViewHolder) holder;
                final int position = trackViewHolder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    final int trackId = getTrackId(mItems.get(position));
                    mTrackSessionsState.put(trackId,
                            trackViewHolder.sessions.getLayoutManager().onSaveInstanceState());
                }
            }
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }


        @NonNull
        private EventDataViewHolder createEventViewHolder(final ViewGroup parent) {
            final EventDataViewHolder
                    holder = new EventDataViewHolder(
                    mInflater.inflate(R.layout.explore_io_event_card, parent, false));
            holder.cards.setHasFixedSize(true);
            holder.cards.setRecycledViewPool(mRecycledViewPool);
            ViewCompat.setImportantForAccessibility(
                    holder.cards, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            holder.headerImage.setImageResource(R.drawable.explore_io_ontheground_header);
            holder.title.setText(R.string.explore_io_on_the_ground_title);
            return holder;
        }

        @NonNull
        private TrackViewHolder createTrackViewHolder(final ViewGroup parent) {
            final TrackViewHolder holder = new TrackViewHolder(
                    mInflater.inflate(R.layout.explore_io_track_card, parent, false));
            holder.sessions.setHasFixedSize(true);
            holder.sessions.setRecycledViewPool(mRecycledViewPool);
            ViewCompat.setImportantForAccessibility(
                    holder.sessions, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            holder.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    final ItemGroup track = (ItemGroup) mItems.get(position);
                    final Intent intent = new Intent(mHost, ExploreSessionsActivity.class);
                    intent.putExtra(ExploreSessionsActivity.EXTRA_FILTER_TAG, track.getId());

                    final ActivityOptionsCompat options =
                            ActivityOptionsCompat.makeSceneTransitionAnimation(mHost,
                                    Pair.create((View) holder.headerImage, mHost.getString(
                                            R.string.transition_track_header)),
                                    Pair.create((View) holder.title, mHost.getString(
                                            R.string.transition_track_title)),
                                    Pair.create(holder.itemView, mHost.getString(
                                            R.string.transition_track_background)));
                    ActivityCompat.startActivity(mHost, intent, options.toBundle());
                }
            });
            return holder;
        }

        @NonNull
        private MessageViewHolder createMessageViewHolder(final ViewGroup parent) {
            final MessageViewHolder holder = new MessageViewHolder(
                    mInflater.inflate(R.layout.explore_io_message_card, parent, false));
            // Work with pre-existing infrastructure which supplied a click listener and relied on
            // a shared pref listener & a reload to dismiss message cards.
            // By setting our own click listener and manually calling onClick we can remove the
            // item in the adapter directly.
            holder.buttonStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    final MessageData message = (MessageData) mItems.get(position);
                    message.getStartButtonClickListener().onClick(holder.buttonStart);
                    mItems.remove(position);
                    notifyItemRemoved(position);
                }
            });
            holder.buttonEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    final MessageData message = (MessageData) mItems.get(position);
                    message.getEndButtonClickListener().onClick(holder.buttonEnd);
                    mItems.remove(position);
                    notifyItemRemoved(position);
                }
            });
            return holder;
        }

        @NonNull
        private KeynoteViewHolder createKeynoteViewHolder(final ViewGroup parent) {
            final KeynoteViewHolder holder = new KeynoteViewHolder(
                    mInflater.inflate(R.layout.explore_io_keynote_card, parent, false));
            holder.clickableItem.setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void onClick(final View v) {
                    final int position = holder.getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) return;
                    final SessionData keynote = (SessionData) mItems.get(position);
                    final Intent intent = new Intent(mHost, SessionDetailActivity.class);
                    intent.setData(
                            ScheduleContract.Sessions.buildSessionUri(keynote.getSessionId()));
                    final ActivityOptionsCompat options = ActivityOptionsCompat
                            .makeSceneTransitionAnimation(mHost,
                                    Pair.create(holder.itemView,
                                            mHost.getString(
                                                    R.string.transition_session_background)),
                                    Pair.create((View) holder.thumbnail,
                                            mHost.getString(R.string.transition_session_image)));
                    ActivityCompat.startActivity(mHost, intent, options.toBundle());
                }
            });
            return holder;
        }

        @NonNull
        private TrackViewHolder createLiveStreamViewHolder(final ViewGroup parent) {
            final TrackViewHolder holder = new TrackViewHolder(
                    mInflater.inflate(R.layout.explore_io_track_card, parent, false));
            ViewCompat.setImportantForAccessibility(
                    holder.sessions, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            holder.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Intent intent = new Intent(mHost, ExploreSessionsActivity.class);
                    intent.setData(ScheduleContract.Sessions
                            .buildSessionsAfterUri(TimeUtils.getCurrentTime(mHost)));
                    intent.putExtra(ExploreSessionsActivity.EXTRA_SHOW_LIVE_STREAM_SESSIONS, true);
                    ActivityCompat.startActivity(mHost, intent, null);
                }
            });
            return holder;
        }

        private void bindTrack(final TrackViewHolder holder, final ItemGroup track) {
            bindTrackOrLiveStream(holder, track, track.getTitle());
        }

        private void bindKeynote(final KeynoteViewHolder holder, final SessionData keynote) {
            holder.title.setText(keynote.getSessionName());
            if (!TextUtils.isEmpty(keynote.getImageUrl())) {
                mImageLoader.loadImage(keynote.getImageUrl(), holder.thumbnail);
            }
            if (!TextUtils.isEmpty(keynote.getDetails())) {
                holder.description.setText(keynote.getDetails());
            }
        }

        private void bindLiveStream(final TrackViewHolder holder, final LiveStreamData data) {
            bindTrackOrLiveStream(holder, data, mHost.getString(R.string.live_now));
        }

        private void bindTrackOrLiveStream(final TrackViewHolder holder, final ItemGroup track,
                                           final String title) {
            holder.title.setText(title);
            holder.header.setContentDescription(title);
            if (track.getPhotoUrl() != null) {
                holder.headerImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mImageLoader.loadImage(track.getPhotoUrl(), holder.headerImage);
            } else {
                holder.headerImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                holder.headerImage.setImageResource(R.drawable.ic_hash_io_16_monochrome);

            }
            final int trackId = getTrackId(track);
            holder.sessions.setAdapter(mTrackSessionsAdapters.get(trackId));
            holder.sessions.getLayoutManager().onRestoreInstanceState(
                    mTrackSessionsState.get(trackId));
        }

        private void bindMessage(final MessageViewHolder holder, final MessageData message) {
            holder.description.setText(message.getMessageString(mHost));
            if (message.getIconDrawableId() > 0) {
                holder.icon.setVisibility(View.VISIBLE);
                holder.icon.setImageResource(message.getIconDrawableId());
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            if (message.getStartButtonStringResourceId() != -1) {
                holder.buttonEnd.setVisibility(View.VISIBLE);
                holder.buttonStart.setText(message.getStartButtonStringResourceId());
            } else {
                holder.buttonStart.setVisibility(View.GONE);
            }
            if (message.getEndButtonStringResourceId() != -1) {
                holder.buttonEnd.setVisibility(View.VISIBLE);
                holder.buttonEnd.setText(message.getEndButtonStringResourceId());
            } else {
                holder.buttonEnd.setVisibility(View.GONE);
            }
        }

        private List processModel(final ExploreIOModel model) {
            final ArrayList<Object> exploreCards = new ArrayList<>();

            // Add any Message cards
            final List<MessageData> messages = model.getMessages();
            if (messages != null && !messages.isEmpty()) {
                exploreCards.addAll(messages);
            }

            // Add Keynote card.
            final SessionData keynote = model.getKeynoteData();
            if (keynote != null) {
                exploreCards.add(keynote);
            }

            // Add Event Cards if onsite.
            if (SettingsUtils.isAttendeeAtVenue(mHost)) {
                final EventData eventData = model.getEventData();
                if (!eventData.getCards().isEmpty()) {
                    exploreCards.add(eventData);
                }
            }

            // Add Live Stream card.
            final LiveStreamData liveStream = model.getLiveStreamData();
            if (liveStream != null && liveStream.getSessions().size() > 0) {
                exploreCards.add(liveStream);
            }

            // Add track cards, ordered alphabetically
            exploreCards.addAll(model.getOrderedTracks());

            return exploreCards;
        }

        /**
         * Setup adapters for tracks which have child session lists
         */
        private void setupSessionAdapters(final ExploreIOModel model) {
            final int trackCount = model.getOrderedTracks().size()
                    + (model.getLiveStreamData() != null ? 1 : 0)
                    + 1; // event data

            mTrackSessionsAdapters = new SparseArrayCompat<>(trackCount);
            mTrackSessionsState = new SparseArrayCompat<>(trackCount);

            final LiveStreamData liveStream = model.getLiveStreamData();
            if (liveStream != null && liveStream.getSessions().size() > 0) {
                mTrackSessionsAdapters.put(getTrackId(liveStream),
                        new LiveStreamSessionsAdapter(mHost, liveStream.getSessions(),
                                mImageLoader));
            }

            final EventData eventData = model.getEventData();
            if (eventData.getCards() != null &&
                    eventData.getCards().size() > 0) {
                mTrackSessionsAdapters.put(getTrackId(eventData),
                        new EventDataAdapter(mHost, eventData.getCards()));
            }

            for (final ItemGroup group : model.getOrderedTracks()) {
                mTrackSessionsAdapters.put(getTrackId(group),
                        SessionsAdapter.createHorizontal(mHost, group.getSessions()));
            }
        }

        /**
         * A derived ID for each track; used as a key for some state objects
         */
        private int getTrackId(Object track) {
            if (track instanceof LiveStreamData) {
                return LIVE_STREAM_TRACK_ID;
            } else if (track instanceof EventData) {
                return EVENT_DATA_TRACK_ID;
            } else if (track instanceof ItemGroup) {
                return ((ItemGroup) track).getId().hashCode();
            }
            return 0;
        }
    }

    private static class EventDataViewHolder extends RecyclerView.ViewHolder {

        final CardView card;
        final ViewGroup header;
        final ImageView headerImage;
        final TextView title;
        final RecyclerView cards;

        public EventDataViewHolder(View itemView) {
            super(itemView);
            card = (CardView) itemView;
            header = (ViewGroup) card.findViewById(R.id.header);
            headerImage = (ImageView) card.findViewById(R.id.header_image);
            title = (TextView) header.findViewById(R.id.title);
            cards = (RecyclerView) card.findViewById(R.id.cards);
        }
    }

    private static class TrackViewHolder extends RecyclerView.ViewHolder {

        final CardView card;
        final ViewGroup header;
        final ImageView headerImage;
        final TextView title;
        final RecyclerView sessions;

        public TrackViewHolder(View itemView) {
            super(itemView);
            card = (CardView) itemView;
            header = (ViewGroup) card.findViewById(R.id.header);
            headerImage = (ImageView) card.findViewById(R.id.header_image);
            title = (TextView) header.findViewById(R.id.title);
            sessions = (RecyclerView) card.findViewById(R.id.sessions);
        }
    }

    private static class MessageViewHolder extends RecyclerView.ViewHolder {

        final ImageView icon;
        final TextView description;
        final Button buttonStart;
        final Button buttonEnd;

        public MessageViewHolder(final View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            description = (TextView) itemView.findViewById(R.id.description);
            buttonStart = (Button) itemView.findViewById(R.id.buttonStart);
            buttonEnd = (Button) itemView.findViewById(R.id.buttonEnd);
        }
    }

    private static class KeynoteViewHolder extends RecyclerView.ViewHolder {

        final ImageView thumbnail;
        final TextView title;
        final TextView description;
        final ViewGroup clickableItem;

        public KeynoteViewHolder(final View itemView) {
            super(itemView);
            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
            title = (TextView) itemView.findViewById(R.id.title);
            description = (TextView) itemView.findViewById(R.id.description);
            clickableItem = (ViewGroup) itemView.findViewById(R.id.explore_io_clickable_item);
        }
    }
}
