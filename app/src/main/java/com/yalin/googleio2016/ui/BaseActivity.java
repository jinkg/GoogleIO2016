package com.yalin.googleio2016.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.injection.LoginAndAuthProvider;
import com.yalin.googleio2016.injection.MessagingRegistrationProvider;
import com.yalin.googleio2016.login.LoginAndAuth;
import com.yalin.googleio2016.login.LoginAndAuthListener;
import com.yalin.googleio2016.login.LoginStateListener;
import com.yalin.googleio2016.messaging.MessagingRegistration;
import com.yalin.googleio2016.navigation.AppNavigationViewAsDrawerImpl;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;
import com.yalin.googleio2016.service.DataBootstrapService;
import com.yalin.googleio2016.sync.account.Account;
import com.yalin.googleio2016.util.AccountUtils;
import com.yalin.googleio2016.util.ImageLoader;
import com.yalin.googleio2016.util.LUtils;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.RecentTasksStyler;
import com.yalin.googleio2016.welcome.WelcomeActivity;

/**
 * YaLin
 * 2016/11/23.
 */

public abstract class BaseActivity extends AppCompatActivity implements
        LoginAndAuthListener,
        LoginStateListener,
        AppNavigationViewAsDrawerImpl.NavigationDrawerStateListener {
    private static final String TAG = "BaseActivity";

    public static final int SWITCH_USER_RESULT = 9998;
    private static final int SELECT_GOOGLE_ACCOUNT_RESULT = 9999;

    private LoginAndAuth mLoginAndAuthProvider;

    private AppNavigationViewAsDrawerImpl mAppNavigationViewAsDrawer;

    private Toolbar mToolbar;

    private LUtils mLUtils;

    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Registration with GCM for notifications
    private MessagingRegistration mMessagingRegistration;

    // handle to our sync observer (that notifies us about changes in our sync state)
    private Object mSyncObserverHandle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecentTasksStyler.styleRecentTasksEntry(this);

        if (WelcomeActivity.shouldDisplay(this)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        mMessagingRegistration = MessagingRegistrationProvider.provideMessagingRegistration(this);

        Account.createSyncAccount(this);

        if (savedInstanceState == null) {
            mMessagingRegistration.registerDevice();
        }

        mLUtils = LUtils.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        DataBootstrapService.startDataBootstrapIfNecessary(this);

        startLoginProcess();
    }

    @Override
    public void onStartLoginProcessRequested() {
        startLoginProcess();
    }

    private void startLoginProcess() {
        LogUtil.d(TAG, "Starting login process.");

        if (!AccountUtils.hasActiveAccount(this)) {
            LogUtil.d(TAG, "Can't proceed with login -- no account chosen.");
            return;
        }

        String accountName = AccountUtils.getActiveAccountName(this);
        LogUtil.d(TAG, "Chosen account: " + accountName);

        if (mLoginAndAuthProvider != null && mLoginAndAuthProvider.getAccountName()
                .equals(accountName)) {
            LogUtil.d(TAG, "Helper already set up; simply starting it.");
            mLoginAndAuthProvider.start();
            return;
        }

        LogUtil.d(TAG, "Starting login process with account " + accountName);

        if (mLoginAndAuthProvider != null) {
            LogUtil.d(TAG, "Tearing down old Helper, was " + mLoginAndAuthProvider.getAccountName());
            if (mLoginAndAuthProvider.isStarted()) {
                LogUtil.d(TAG, "Stopping old Helper");
                mLoginAndAuthProvider.stop();
            }
            mLoginAndAuthProvider = null;
        }

        LogUtil.d(TAG, "Creating and starting new Helper with account: " + accountName);
        mLoginAndAuthProvider =
                LoginAndAuthProvider.provideLoginAndAuth(this, this, accountName);
        mLoginAndAuthProvider.start();
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses of
     * BaseActivity override this to indicate what nav drawer item corresponds to them Return
     * NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationItemEnum.INVALID;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        getToolbar();
    }

    @Override
    public void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating) {
        LogUtil.d(TAG, "onNavDrawerStateChanged");
    }

    @Override
    public void onNavDrawerSlide(float slideOffset) {

    }

    @Override
    public void onBackPressed() {
        if (mAppNavigationViewAsDrawer.isNavDrawerOpen()) {
            mAppNavigationViewAsDrawer.closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mAppNavigationViewAsDrawer = new AppNavigationViewAsDrawerImpl(new ImageLoader(this), this);
        mAppNavigationViewAsDrawer.activityReady(this, this, getSelfNavDrawerItem());

        if (getSelfNavDrawerItem() != NavigationItemEnum.INVALID) {
            setToolbarForNavigation();
        }

        // todo add alpha animation
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMessagingRegistration != null) {
            mMessagingRegistration.destory();
        }
    }

    @Override
    public void onAccountChangeRequested() {

    }

    @Override
    public void onSignInOrCreateAccount() {

    }

    @Override
    public void onAuthFailure(String accountName) {
        // todo refresh account

        mAppNavigationViewAsDrawer.updateNavigationItems();
    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {
        //todo sync data

        mAppNavigationViewAsDrawer.updateNavigationItems();
        mMessagingRegistration.registerDevice();
    }

    @Override
    public void onPlusInfoLoaded(String accountName) {
        mAppNavigationViewAsDrawer.updateNavigationItems();
    }

    public Toolbar getToolbar() {
        if (mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mToolbar != null) {
                mToolbar.setNavigationContentDescription(R.string.navdrawer_description_a11y);
                setSupportActionBar(mToolbar);
            }
        }
        return mToolbar;
    }

    private void setToolbarForNavigation() {
        if (mToolbar != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_hamburger);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAppNavigationViewAsDrawer.showNavigation();
                }
            });
        }
    }
}
