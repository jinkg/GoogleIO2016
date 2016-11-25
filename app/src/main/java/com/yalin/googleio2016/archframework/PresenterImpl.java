package com.yalin.googleio2016.archframework;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yalin.googleio2016.util.LogUtil;

/**
 * YaLin
 * 2016/11/23.
 * <p>
 * This implements the {@link Presenter} interface. This Presenter can interact with more than one
 * {@link UpdatableView}, based on the constructor used to create it. The most common use case for
 * have multiple {@link UpdatableView} controlled by the same presenter is an Activity with tabs,
 * where each view in the tab (typically a {@link android.app.Fragment}) is an {@link
 * UpdatableView}.
 * <p/>
 * It requests the model to load its initial data, it listens to events from the {@link
 * UpdatableView}(s) and passes the user actions on to the Model, then it updates the {@link
 * UpdatableView}(s) once the Model has completed its data update.
 */
public class PresenterImpl implements Presenter, UpdatableView.UserActionListener {
    private static final String TAG = "PresenterImpl";

    @Nullable
    private UpdatableView[] mUpdatableViews;

    private Model mModel;

    private QueryEnum[] mInitialQueriesToLoad;

    private UserActionEnum[] mValidUserActions;

    public PresenterImpl(Model model, UpdatableView view, UserActionEnum[] validUserActions,
                         QueryEnum[] initialQueries) {
        this(model, new UpdatableView[]{view}, validUserActions, initialQueries);
    }

    public PresenterImpl(Model model, @Nullable UpdatableView[] views, UserActionEnum[] validUserActions,
                         QueryEnum[] initialQueries) {
        mModel = model;
        if (views != null) {
            mUpdatableViews = views;
            for (UpdatableView updatableView : mUpdatableViews) {
                updatableView.addListener(this);
            }
        } else {
            LogUtil.e(TAG, "Creating a PresenterImpl with null View");
        }
        mValidUserActions = validUserActions;
        mInitialQueriesToLoad = initialQueries;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadInitialQueries() {
        if (mInitialQueriesToLoad != null && mInitialQueriesToLoad.length > 0) {
            for (QueryEnum query : mInitialQueriesToLoad) {
                mModel.requestData(query, new Model.DataQueryCallback() {
                    @Override
                    public void onModelUpdated(Model model, QueryEnum query) {
                        if (mUpdatableViews != null) {
                            for (UpdatableView view : mUpdatableViews) {
                                view.displayData(model, query);
                            }
                        } else {
                            LogUtil.e(TAG, "loadInitialQueries(), cannot notify a null view!");
                        }
                    }

                    @Override
                    public void onError(QueryEnum query) {
                        if (mUpdatableViews != null) {
                            for (UpdatableView view : mUpdatableViews) {
                                view.displayErrorMessage(query);
                            }
                        } else {
                            LogUtil.e(TAG, "loadInitialQueries(), cannot notify a null view!");
                        }
                    }
                });
            }
        } else {
            if (mUpdatableViews != null) {
                for (UpdatableView view : mUpdatableViews) {
                    view.displayData(mModel, null);
                }
            } else {
                LogUtil.e(TAG, "loadInitialQueries(), cannot notify a null view!");
            }
        }
    }

    /**
     * Called when the user has performed an {@code action}, with data to be passed as a {@link
     * android.os.Bundle} in {@code args}.
     * <p/>
     * Add the constants used to store values in the bundle to the Model implementation class as
     * final static protected strings.
     * <p/>
     * If the {@code action} should trigger a new data query, specify the query ID by storing the
     * associated Integer in the {@code args} using {@link ModelWithLoaderManager#KEY_RUN_QUERY_ID}.
     * The {@code args} will be passed on to the cursor loader so you can pass in extra arguments
     * for your query.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onUserAction(UserActionEnum action, @Nullable Bundle args) {
        boolean isValid = false;
        if (mValidUserActions != null && action != null) {
            for (UserActionEnum validAction : mValidUserActions) {
                if (validAction.getId() == action.getId()) {
                    isValid = true;
                }
            }
        }
        if (isValid) {
            mModel.deliverUserAction(action, args, new Model.UserActionCallback() {
                @Override
                public void onModelUpdated(Model model, UserActionEnum userAction) {
                    if (mUpdatableViews != null) {
                        for (UpdatableView view : mUpdatableViews) {
                            view.displayUserActionResult(model, userAction, true);
                        }
                    } else {
                        LogUtil.e(TAG, "onUserAction(), cannot notify a null view!");
                    }
                }

                @Override
                public void onError(UserActionEnum userAction) {
                    if (mUpdatableViews != null) {
                        for (UpdatableView view : mUpdatableViews) {
                            view.displayUserActionResult(null, userAction, false);
                        }
                        // User action not understood by model, even though the presenter understands

                        // it.
                        LogUtil.e(TAG, "Model doesn't implement user action " + userAction.getId() +
                                ". Have you forgotten to implement this UserActionEnum in your " +
                                "model," +
                                " or have you called setValidUserActions on your presenter with a " +
                                "UserActionEnum that it shouldn't support?");
                    } else {
                        LogUtil.e(TAG, "onUserAction(), cannot notify a null view!");
                    }
                }
            });
        } else {
            if (mUpdatableViews != null) {
                for (UpdatableView view : mUpdatableViews) {
                    view.displayUserActionResult(null, action, false);
                }
                // User action not understood.
                throw new RuntimeException(
                        "Invalid user action " + (action != null ? action.getId() : null) +
                                ". Have you called setValidUserActions on your presenter, with all " +

                                "the UserActionEnum you want to support?");
            } else {
                LogUtil.e(TAG, "onUserAction(), cannot notify a null view!");
            }
        }
    }
}
