package com.yalin.googleio2016.ui;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.injection.LoginAndAuthProvider;
import com.yalin.googleio2016.injection.MessagingRegistrationProvider;
import com.yalin.googleio2016.login.LoginAndAuth;
import com.yalin.googleio2016.login.LoginAndAuthListener;
import com.yalin.googleio2016.login.LoginStateListener;
import com.yalin.googleio2016.messaging.MessagingRegistration;
import com.yalin.googleio2016.navigation.AppNavigationViewAsDrawerImpl;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.service.DataBootstrapService;
import com.yalin.googleio2016.sync.SyncHelper;
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
        MultiSwipeRefreshLayout.CanChildScrollUpCallback,
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

    private void trySetupSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(R.color.flat_button_text);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestDataRefresh();
                }
            });

            if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
                MultiSwipeRefreshLayout mswrl = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
                mswrl.setCanChildScrollUpCallback(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        DataBootstrapService.startDataBootstrapIfNecessary(this);

        mSyncStatusObserver.onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

        startLoginProcess();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
        if (mLoginAndAuthProvider != null) {
            mLoginAndAuthProvider.stop();
        }
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

        trySetupSwipeRefresh();
        // todo add alpha animation
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_refresh:
                requestDataRefresh();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void requestDataRefresh() {
        android.accounts.Account activeAccount = AccountUtils.getActiveAccount(this);
        if (ContentResolver.isSyncActive(activeAccount, ScheduleContract.CONTENT_AUTHORITY)) {
            LogUtil.d(TAG, "Ignoring manual sync request because a sync is already in progress.");
            return;
        }
        LogUtil.d(TAG, "Requesting manual data refresh.");
        SyncHelper.requestManualSync();
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
        // override if you want to be notified when another account has been selected account has
        // changed
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_GOOGLE_ACCOUNT_RESULT) {
            // Handle the select {@code startActivityForResult} from
            // {@code enforceActiveGoogleAccount()} when a Google Account wasn't present on the
            // device.
            if (resultCode == RESULT_OK) {
                String accountName =
                        data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                AccountUtils.setActiveAccount(this, accountName);
                onAuthSuccess(accountName, true);
            } else {
                LogUtil.w(TAG, "A Google Account is required to use this application.");
                // This application requires a Google Account to be selected.
                finish();
            }
            return;
        } else if (requestCode == SWITCH_USER_RESULT) {
            // Handle account change notifications after {@link SwitchUserActivity} has been invoked
            // (typically by {@link AppNavigationViewAsDrawerImpl}).
            if (resultCode == RESULT_OK) {
                onAccountChangeRequested();
                onStartLoginProcessRequested();
            }
        }
        if (mLoginAndAuthProvider == null || !mLoginAndAuthProvider.onActivityResult(requestCode,
                resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String accountName = AccountUtils.getActiveAccountName(BaseActivity.this);
                    if (TextUtils.isEmpty(accountName)) {
                        onRefreshingStateChanged(false);
                        return;
                    }

                    android.accounts.Account account = new android.accounts.Account(
                            accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, ScheduleContract.CONTENT_AUTHORITY);
                    onRefreshingStateChanged(syncActive);
                }
            });
        }
    };

    protected void onRefreshingStateChanged(boolean refreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    public LUtils getLUtils() {
        return mLUtils;
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return false;
    }
}
