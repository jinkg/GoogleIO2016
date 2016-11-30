package com.yalin.googleio2016.archframework;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.common.annotations.VisibleForTesting;
import com.yalin.googleio2016.util.LogUtil;

import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Implementation class for {@link Model}, using the {@link LoaderManager} callbacks to query the
 * data from the {@link com.yalin.googleio2016.provider.ScheduleProvider}.
 */
public abstract class ModelWithLoaderManager<Q extends QueryEnum, UA extends UserActionEnum>
        implements Model<Q, UA>, LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * Key to be used in Bundle passed in {@link #onUserAction(UserActionEnum, Bundle)} for a user
     * action that requires running {@link QueryEnum}, specifying its id. The value stored must be
     * an Integer.
     */
    public static final String KEY_RUN_QUERY_ID = "KEY_RUN_QUERY_ID";

    private static final String TAG = "ModelWithLoaderManager";

    private LoaderManager mLoaderManager;

    private Q[] mQueries;

    private UA[] mUserActions;

    /**
     * Map of callbacks, using the query as key. This is required because we can't pass on the
     * {@link com.yalin.googleio2016.archframework.Model.DataQueryCallback} to the {@link
     * LoaderManager} callbacks.
     * <p/>
     * This is @VisibleForTesting because for integration testing, a fake model is used to allow
     * bypassing the {@link LoaderManager} and pass a mock {@link Cursor} directly to {@link
     * #onLoadFinished(QueryEnum, Cursor)} and adding the callback so events can be fired normally
     * on the callback after the data is read from the cursor.
     */
    @VisibleForTesting
    protected HashMap<Q, DataQueryCallback> mDataQueryCallbacks =
            new HashMap<>();

    /**
     * Map of callbacks, using the id of the user action as key. This is required because some user
     * actions launch a data query and we can't pass on the {@link com.yalin.googleio2016
     * .archframework.Model.UserActionCallback} to the {@link LoaderManager} callbacks.
     * <p/>
     * When the user action leads to a new query being run, the {@link LoaderManager} callbacks
     * provide us with an Integer id. Therefore, we link an Integer id to a callback, and use a
     * separate map to link the Integer id to a user action {}see {@link
     * #mUserActionsLaunchingQueries}.
     * <p/>
     * This is @VisibleForTesting because for integration testing, a fake model is used to allow
     * bypassing the {@link LoaderManager} and pass a mock {@link Cursor} directly to {@link
     * #onLoadFinished(QueryEnum, Cursor)} and adding the callback so events can be fired normally
     * on the callback after the data is read from the cursor.
     */
    @VisibleForTesting
    protected SparseArray<UserActionCallback> mDataUpdateCallbacks =
            new SparseArray<>();

    /**
     * Map of user actions that have launched queries, using their id as key. This is used in
     * conjunction with {@link #mDataUpdateCallbacks}, so once the {@link
     * android.app.LoaderManager.LoaderCallbacks#onLoadFinished(Loader, Object)} has fired, the
     * {@link UserActionCallback} that launched that query can be fired.
     * <p/>
     * This is @VisibleForTesting because for integration testing, a fake model is used to allow
     * bypassing the {@link LoaderManager} and pass a mock {@link Cursor} directly to {@link
     * #onLoadFinished(QueryEnum, Cursor)} and adding the callback so events can be fired normally
     * on the callback after the data is read from the cursor.
     */
    @VisibleForTesting
    protected SparseArray<UA> mUserActionsLaunchingQueries = new SparseArray<>();

    public ModelWithLoaderManager(Q[] queries, UA[] userActions, LoaderManager loaderManager) {
        mQueries = queries;
        mUserActions = userActions;
        mLoaderManager = loaderManager;
    }

    @Override
    public Q[] getQueries() {
        return mQueries;
    }

    @Override
    public UA[] getUserActions() {
        return mUserActions;
    }

    /**
     * Called when the user has performed an {@code action}, with data in {@code args}.
     * <p/>
     * Add the constants used to store values in the bundle to the Model implementation class as
     * final static protected strings.
     * <p/>
     * If the {@code action} should trigger a new data query, specify the query ID by storing the
     * associated Integer in the {@code args} using {@link #KEY_RUN_QUERY_ID}. The {@code args} will
     * be passed on to the cursor loader so you can pass in extra arguments for your query.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void deliverUserAction(UA action, @Nullable Bundle args,
                                  UserActionCallback callback) {
        checkNotNull(callback);
        checkNotNull(action);
        if (args != null && args.containsKey(KEY_RUN_QUERY_ID)) {
            Object queryId = args.get(KEY_RUN_QUERY_ID);
            if (queryId instanceof Integer) {
                if (isQueryValid((Integer) queryId) && mLoaderManager != null) {
                    mLoaderManager.restartLoader((Integer) queryId, args, this);
                    mDataUpdateCallbacks.put((Integer) queryId, callback);
                    mUserActionsLaunchingQueries.put((Integer) queryId, action);
                } else if (isQueryValid((Integer) queryId) && mLoaderManager == null) {
                    // The loader manager hasn't been initialised because initial queries haven't
                    // been run yet. This happens when a user action is triggered by a change in
                    // shared preferences before the initial queries are loaded. Unlikely to happen
                    // often, but it is a possible race condition and it was triggered in UI
                    // tests. Nothing to do in that case because presenter will run all queries
                    // when it will go through loadInitialQueries.
                    LogUtil.d(TAG, "Loader manager hasn't been initialised.");
                } else {
                    callback.onError(action);
                    // Query id should be valid!
                    LogUtil.e(TAG, "onUserAction called with a bundle containing KEY_RUN_QUERY_ID but"
                            + " ths value is not a valid query id!");
                }
            } else {
                callback.onError(action);
                // Query id should be an integer!
                LogUtil.e(TAG, "onUserAction called with a bundle containing KEY_RUN_QUERY_ID but"
                        + "the value is not an Integer so it's not a valid query id!");
            }
        } else {
            processUserAction(action, args, callback);
        }
    }

    /**
     * This should be implemented by the feature. Typically, there will be a switch on the {@code
     * action}, a method will be called to update the data, then the callback will be fired.
     *
     * @see SessionDetailModel#processUserAction(UA, @Nullable Bundle, UserActionCallback )
     */
    public abstract void processUserAction(UA action, @Nullable Bundle args,
                                           UserActionCallback callback);

    @SuppressWarnings("unchecked")
    @Override
    public void requestData(Q query, DataQueryCallback callback) {
        checkNotNull(query);
        checkNotNull(callback);
        if (isQueryValid(query)) {
            mLoaderManager.initLoader(query.getId(), null, this);
            mDataQueryCallbacks.put(query, callback);
        } else {
            LogUtil.e(TAG, "Invalid query " + query);
            callback.onError(query);
        }
    }

    private boolean isQueryValid(Q query) {
        checkNotNull(query);
        return isQueryValid(query.getId());
    }

    @SuppressWarnings("unchecked")
    private boolean isQueryValid(int queryId) {
        Q match = (Q) QueryEnumHelper.getQueryForId(queryId, getQueries());
        return match != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return createCursorLoader((Q) QueryEnumHelper.getQueryForId(id, mQueries), args);
    }

    /**
     * This should be implemented by the feature. In addition to the {@link
     * QueryEnum#getProjection()}, other information such as sorting order will be needed.
     */
    public abstract Loader<Cursor> createCursorLoader(Q query, Bundle args);

    /**
     * This should be implemented by the feature. It reads the data from the {@code cursor} for the
     * given {@code query}. Typically, there will be a switch on the {@code query}, then a private
     * method will be called to read the data from the cursor.
     */
    public abstract boolean readDataFromCursor(Cursor cursor, Q query);

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Q query = (Q) QueryEnumHelper.getQueryForId(loader.getId(), mQueries);
        onLoadFinished(query, data);
    }

    /**
     * This method is called directly from integration tests to allow us to pass in a mock cursor,
     * so we can stub out different data and thus test the UI fully.
     */
    @SuppressWarnings("unchecked")
    @VisibleForTesting
    public void onLoadFinished(Q query, Cursor data) {
        boolean success = readDataFromCursor(data, query);
        if (mDataUpdateCallbacks.indexOfKey(query.getId()) > 0
                && mUserActionsLaunchingQueries.indexOfKey(query.getId()) > 0) {
            UserActionCallback callback = mDataUpdateCallbacks.get(query.getId());
            UA userAction = mUserActionsLaunchingQueries.get(query.getId());
            if (success) {
                callback.onModelUpdated(this, userAction);
            } else {
                callback.onError(userAction);
            }
        } else if (mDataQueryCallbacks.containsKey(query) &&
                mDataQueryCallbacks.get(query) != null) {
            DataQueryCallback callback = mDataQueryCallbacks.get(query);
            if (success) {
                callback.onModelUpdated(this, query);
            } else {
                callback.onError(query);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Not used
    }
}
