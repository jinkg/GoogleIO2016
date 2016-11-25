package com.yalin.googleio2016.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.yalin.googleio2016.archframework.PresenterImpl;
import com.yalin.googleio2016.archframework.UpdatableView;
import com.yalin.googleio2016.login.LoginStateListener;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationQueryEnum;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationUserActionEnum;
import com.yalin.googleio2016.util.ActivityUtils;

/**
 * YaLin
 * 2016/11/23.
 * <p>
 * This abstract class implements both {@link UpdatableView} and {@link AppNavigationView}, without
 * any specific UI implementation details. This uses the {@link com.yalin.googleio2016
 * .archframework} for getting its data and processing user actions. Some methods which are UI
 * specific are left abstract. Extend this class for full navigation functionality.
 */
public abstract class AppNavigationViewAbstractImpl implements
        UpdatableView<NavigationModel, NavigationQueryEnum, NavigationUserActionEnum>,
        AppNavigationView {
    private UserActionListener mUserActionListener;

    protected LoginStateListener mLoginStateListener;

    protected Activity mActivity;

    protected NavigationItemEnum mSelfItem;

    @Override
    public void displayData(NavigationModel model, NavigationQueryEnum query) {
        switch (query) {
            case LOAD_ITEMS:
                displayNavigationItems(model.getItems());
                break;
        }
    }

    @Override
    public void displayErrorMessage(NavigationQueryEnum query) {
        switch (query) {
            case LOAD_ITEMS:
                break;
        }
    }

    @Override
    public void activityReady(Activity activity, LoginStateListener loginStateListener,
                              NavigationItemEnum self) {
        mActivity = activity;
        mLoginStateListener = loginStateListener;
        mSelfItem = self;

        setUpView();

        NavigationModel model = new NavigationModel(getContext());
        PresenterImpl presenter = new PresenterImpl(model, this,
                NavigationUserActionEnum.values(), NavigationQueryEnum.values());
        presenter.loadInitialQueries();
        addListener(presenter);
    }

    @Override
    public void updateNavigationItems() {
        mUserActionListener.onUserAction(NavigationUserActionEnum.RELOAD_ITEMS, null);
    }

    @Override
    public abstract void displayNavigationItems(NavigationItemEnum[] items);

    @Override
    public abstract void setUpView();

    @Override
    public abstract void showNavigation();

    @Override
    public void itemSelected(NavigationItemEnum item) {
        switch (item) {
            case SIGN_IN:
                mLoginStateListener.onSignInOrCreateAccount();
                break;
            default:
                if (item.getClassToLaunch() != null) {
                    ActivityUtils.createBackStack(mActivity,
                            new Intent(mActivity, item.getClassToLaunch()));
                    if (item.finishCurrentActivity()) {
                        mActivity.finish();
                    }
                }
                break;
        }
    }

    @Override
    public void displayUserActionResult(NavigationModel model,
                                        NavigationUserActionEnum userAction, boolean success) {
        switch (userAction) {
            case RELOAD_ITEMS:
                displayNavigationItems(model.getItems());
                break;
        }
    }

    @Override
    public Uri getDataUri(NavigationQueryEnum query) {
        return null;
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void addListener(UserActionListener listener) {
        mUserActionListener = listener;
    }
}
