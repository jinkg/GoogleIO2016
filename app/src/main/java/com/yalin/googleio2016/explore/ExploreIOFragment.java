package com.yalin.googleio2016.explore;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.ModelWithLoaderManager;
import com.yalin.googleio2016.archframework.PresenterImpl;
import com.yalin.googleio2016.archframework.UpdatableView;
import com.yalin.googleio2016.explore.ExploreIOModel.ExploreIOQueryEnum;
import com.yalin.googleio2016.explore.ExploreIOModel.ExploreIOUserActionEnum;
import com.yalin.googleio2016.injection.ModelProvider;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.ui.widget.recyclerview.ItemMarginDecoration;
import com.yalin.googleio2016.ui.widget.recyclerview.UpdatableAdapter;
import com.yalin.googleio2016.util.ImageLoader;

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

        // Immutable state
        private final Activity mHost;

        private final LayoutInflater mInflater;

        private final ImageLoader mImageLoader;

        private final RecyclerView.RecycledViewPool mRecycledViewPool;

        // State
        private List mItems;

        ExploreAdapter(@NonNull Activity activity,
                       @NonNull ExploreIOModel model,
                       @NonNull ImageLoader imageLoader) {
            mHost = activity;
            mImageLoader = imageLoader;
            mInflater = LayoutInflater.from(activity);
            mRecycledViewPool = new RecyclerView.RecycledViewPool();
//            mItems = processModel(model);
//            setupSessionAdapters(model);
        }

        @Override
        public void update(@NonNull ExploreIOModel updatedData) {

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }
}
