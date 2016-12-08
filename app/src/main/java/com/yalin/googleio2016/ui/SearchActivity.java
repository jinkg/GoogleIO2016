package com.yalin.googleio2016.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.explore.ExploreSessionsActivity;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.session.SessionDetailActivity;

/**
 * YaLin
 * 2016/12/8.
 */

public class SearchActivity extends BaseActivity implements
        AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SCREEN_LABEL = "Search";
    private static final String ARG_QUERY = "query";

    private SearchView mSearchView;
    private String mQuery = "";
    private ListView mSearchResults;
    private SimpleCursorAdapter mResultsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchView = (SearchView) findViewById(R.id.search_view);
        setupSearchView();
        mSearchResults = (ListView) findViewById(R.id.search_results);
        mResultsAdapter = new SimpleCursorAdapter(this,
                R.layout.list_item_search_result, null,
                new String[]{ScheduleContract.SearchTopicSessionsColumns.SEARCH_SNIPPET},
                new int[]{R.id.search_result}, 0);
        mSearchResults.setAdapter(mResultsAdapter);
        mSearchResults.setOnItemClickListener(this);
        Toolbar toolbar = getToolbar();

        VectorDrawableCompat vectorDrawable =
                VectorDrawableCompat.create(getResources(), R.drawable.ic_up, getTheme());
        if (vectorDrawable != null) {
            Drawable up = DrawableCompat.wrap(vectorDrawable);
            DrawableCompat.setTint(up, ContextCompat.getColor(this, R.color.app_body_text_2));
            toolbar.setNavigationIcon(up);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateUpOrBack(SearchActivity.this, null);
            }
        });

        String query = getIntent().getStringExtra(SearchManager.QUERY);
        query = query == null ? "" : query;
        mQuery = query;

        if (mSearchView != null) {
            mSearchView.setQuery(query, false);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            doEnterAnim();
        }
        overridePendingTransition(0, 0);
    }

    /**
     * As we only ever want one instance of this screen, we set a launchMode of singleTop. This
     * means that instead of re-creating this Activity, a new intent is delivered via this callback.
     * This prevents multiple instances of the search dialog 'stacking up' e.g. if you perform a
     * voice search.
     * <p>
     * See: http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                searchFor(query);
                mSearchView.setQuery(query, false);
            }
        }
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);

        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFor(newText);
                return true;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                dismiss(null);
                return false;
            }
        });
        if (!TextUtils.isEmpty(mQuery)) {
            mSearchView.setQuery(mQuery, false);
        }
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    private boolean dismissing = false;

    public void dismiss(View view) {
        if (dismissing) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 21) {
            dismissing = true;
            doExitAnim();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    /**
     * On Lollipop+ perform a circular reveal animation (an expanding circular mask) when showing
     * the search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doEnterAnim() {
        // Fade in a background scrim as this is a floating window. We could have used a
        // translucent window background but this approach allows us to turn off window animation &
        // overlap the fade with the reveal animation – making it feel snappier.

        View scrim = findViewById(R.id.scrim);
        scrim.animate()
                .alpha(1f)
                .setDuration(500L)
                .setInterpolator(
                        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in))
                .start();

        // Next perform the circular reveal on the search panel
        final View searchPanel = findViewById(R.id.search_panel);
        if (searchPanel != null) {
            // We use a view tree observer to set this up once the view is measured & laid out
            searchPanel.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    searchPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                    // As the height will change once the initial suggestions are delivered by the
                    // loader, we can't use the search panels height to calculate the final radius
                    // so we fall back to it's parent to be safe
                    final ViewGroup searchPanelParent = (ViewGroup) searchPanel.getParent();
                    final int revealRadius = (int) Math.hypot(
                            searchPanelParent.getWidth(), searchPanelParent.getHeight());
                    // Center the animation on the top right of the panel i.e. near to the
                    // search button which launched this screen.
                    Animator show = ViewAnimationUtils.createCircularReveal(searchPanel,
                            searchPanel.getRight(), searchPanel.getTop(), 0f, revealRadius);
                    show.setDuration(250L);
                    show.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                            android.R.interpolator.fast_out_slow_in));
                    return false;
                }
            });
        }
    }

    /**
     * On Lollipop+ perform a circular animation (a contracting circular mask) when hiding the
     * search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doExitAnim() {
        final View searchPanel = findViewById(R.id.search_panel);

        final int revealRadius = (int) Math.hypot(searchPanel.getWidth(), searchPanel.getHeight());

        Animator shrink = ViewAnimationUtils.createCircularReveal(searchPanel,
                searchPanel.getRight(), searchPanel.getTop(), revealRadius, 0f);
        shrink.setDuration(200L);
        shrink.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                android.R.interpolator.fast_out_slow_in));
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchPanel.setVisibility(View.INVISIBLE);
                ActivityCompat.finishAfterTransition(SearchActivity.this);
            }
        });
        shrink.start();

        findViewById(R.id.scrim).animate()
                .alpha(0f)
                .setDuration(200L)
                .setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                        android.R.interpolator.fast_out_slow_in))
                .start();
    }

    private void searchFor(String query) {
        // TODO: 2016/12/8 add analytics
        // ANALYTICS EVENT: Start a search on the Search activity
        // Contains: Nothing (Event params are constant:  Search query not included)
//        AnalyticsHelper.sendEvent(SCREEN_LABEL, "Search", "");

        Bundle args = new Bundle(1);
        if (query == null) {
            query = "";
        }
        args.putString(ARG_QUERY, query);
        if (TextUtils.equals(query, mQuery)) {
            getLoaderManager().initLoader(SearchTopicsSessionsQuery.TOKEN, args, this);
        } else {
            getLoaderManager().restartLoader(SearchTopicsSessionsQuery.TOKEN, args, this);
        }
        mQuery = query;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor c = mResultsAdapter.getCursor();
        c.moveToPosition(position);
        boolean isTopicTag = c.getInt(SearchTopicsSessionsQuery.IS_TOPIC_TAG) == 1;
        String tagOrSessionId = c.getString(SearchTopicsSessionsQuery.TAG_OR_SESSION_ID);
        if (isTopicTag) {
            Intent intent = new Intent(this, ExploreSessionsActivity.class);
            intent.putExtra(ExploreSessionsActivity.EXTRA_FILTER_TAG, tagOrSessionId);
            startActivity(intent);
        } else if (tagOrSessionId != null) {
            Intent intent = new Intent(this, SessionDetailActivity.class);
            intent.setData(ScheduleContract.Sessions.buildSessionUri(tagOrSessionId));
            startActivity(intent);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ScheduleContract.SearchTopicsSessions.CONTENT_URI,
                SearchTopicsSessionsQuery.PROJECTION,
                null,
                new String[]{args.getString(ARG_QUERY)}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mResultsAdapter.swapCursor(data);
        mSearchResults.setVisibility(data.getCount() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    private interface SearchTopicsSessionsQuery {
        int TOKEN = 0x4;
        String[] PROJECTION = ScheduleContract.SearchTopicsSessions.DEFAULT_PROJECTION;

        int _ID = 0;
        int TAG_OR_SESSION_ID = 1;
        int SEARCH_SNIPPET = 2;
        int IS_TOPIC_TAG = 3;
    }
}
