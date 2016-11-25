package com.yalin.googleio2016.navigation;

import android.accounts.Account;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.util.AccountUtils;
import com.yalin.googleio2016.util.ImageLoader;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * YaLin
 * 2016/11/23.
 * <p>
 * This is the implementation of {@link AppNavigationView} using a {@link DrawerLayout}. This
 * extends {@link AppNavigationViewAbstractImpl} so only UI specific methods are implemented.
 */
public class AppNavigationViewAsDrawerImpl extends AppNavigationViewAbstractImpl
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "NavigationViewDrawer";

    // Delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // Fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MIAN_CONTENT_FADEOUT_DURATION = 150;

    /**
     * Key to track whether the {@link #mAccountSpinner} drop down view is visible when saving the
     * state of this view.
     */
    private static final String ACCOUNT_SPINNER_DROP_DOWN_VISIBLE =
            "account_spinner_drop_down_visible";

    private DrawerLayout mDrawerLayout;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;

    private NavigationView mNavigationView;

    private Spinner mAccountSpinner;

    private boolean mAccountSpinnerDropDownViewVisible;

    /**
     * True if the {@link #mAccountSpinner} should be shown in drop down mode when this view is
     * first loaded.
     */
    private boolean mSetAccountSpinnerInDropViewModeWhenFirstShown;

    private AccountSpinnerAdapter mAccountSpinnerAdapter;

    private Handler mHandler;

    private ImageLoader mImageLoader;

    private NavigationDrawerStateListener mNavigationDrawerStateListener;

    public AppNavigationViewAsDrawerImpl(ImageLoader imageLoader,
                                         NavigationDrawerStateListener listener) {
        mImageLoader = imageLoader;
        mNavigationDrawerStateListener = listener;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        NavigationItemEnum item = NavigationItemEnum.getById(menuItem.getItemId());
        onNavDrawerItemClicked(item);
        return true;
    }

    @Override
    public void displayNavigationItems(NavigationItemEnum[] items) {
        createNavDrawerItems(items);
        setSelectedNavDrawerItem(mSelfItem);
        setupAccountBox();
    }

    @Override
    public void setUpView() {
        mHandler = new Handler();

        if (mSelfItem == NavigationItemEnum.INVALID) {
            mDrawerLayout = null;
            return;
        }

        mDrawerLayout = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }

        mDrawerLayout.setStatusBarBackgroundColor(
                UIUtils.getThemeColor(mActivity, R.attr.colorPrimaryDark,
                        R.color.theme_primary_dark));

        mNavigationView = (NavigationView) mActivity.findViewById(R.id.nav_view);

        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                mNavigationDrawerStateListener.onNavDrawerSlide(slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mNavigationDrawerStateListener.onNavDrawerStateChanged(true, false);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                mNavigationDrawerStateListener.onNavDrawerStateChanged(false, false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                mNavigationDrawerStateListener.onNavDrawerStateChanged(isNavDrawerOpen(),
                        newState != DrawerLayout.STATE_IDLE);
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        if (!SettingsUtils.isFirstRunProcessComplete(mActivity)) {
            SettingsUtils.markFirstRunProcessesDone(mActivity, true);
            mDrawerLayout.openDrawer(GravityCompat.START);
        }

        setupAccountBox();
    }

    @Override
    public void showNavigation() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void createNavDrawerItems(NavigationItemEnum[] items) {
        if (mNavigationView != null) {
            Menu menu = mNavigationView.getMenu();
            for (NavigationItemEnum item : items) {
                MenuItem menuItem = menu.findItem(item.getId());
                if (menuItem != null) {
                    menuItem.setVisible(true);
                    menuItem.setIcon(item.getIconResource());
                    menuItem.setTitle(item.getTitleResource());
                } else {
                    LogUtil.e(TAG, "Menu Item for navigation item with title " +
                            (item.getTitleResource() != 0 ? mActivity.getResources().getString(
                                    item.getTitleResource()) : "") + " not found");
                }
            }
            mNavigationView.setNavigationItemSelectedListener(this);
        }
    }

    private void setSelectedNavDrawerItem(NavigationItemEnum item) {
        if (mNavigationView != null && item != NavigationItemEnum.INVALID) {
            mNavigationView.getMenu().findItem(item.getId()).setChecked(true);
        }
    }

    private void setupAccountBox() {
        final View chosenAccountView = mActivity.findViewById(R.id.chosen_account_view);

        if (chosenAccountView == null) {
            return;
        }

        Account chosenAccount = AccountUtils.getActiveAccount(mActivity);
        if (chosenAccount == null) {
            chosenAccountView.setVisibility(View.GONE);
            return;
        } else {
            chosenAccountView.setVisibility(View.VISIBLE);
        }

        ImageView coverImageView = (ImageView) chosenAccountView
                .findViewById(R.id.profile_cover_image);
        ImageView profileImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_image);

        String imageUrl = AccountUtils.getPlusImageUrl(mActivity);
        if (imageUrl != null) {
            mImageLoader.loadImage(imageUrl, profileImageView);
        }

        String coverImageUrl = AccountUtils.getPlusImageUrl(mActivity);
        if (coverImageUrl != null) {
            mActivity.findViewById(R.id.profile_cover_image_placeholder).setVisibility(View.GONE);
            coverImageView.setVisibility(View.VISIBLE);
            coverImageView.setContentDescription(mActivity.getString(
                    R.string.navview_header_user_image_content_description));
            mImageLoader.loadImage(coverImageUrl, coverImageView);
            coverImageView.setColorFilter(ContextCompat.getColor(mActivity, R.color.light_content_scrim));
        } else {
            mActivity.findViewById(R.id.profile_cover_image_placeholder)
                    .setVisibility(View.VISIBLE);
            coverImageView.setVisibility(View.GONE);
        }

//        List<Account> accounts = Arrays.asList(
//                AccountUtils.getActiveAccount(getContext()));
        List<Account> accounts = new ArrayList<>();
        accounts.add(AccountUtils.getActiveAccount(getContext()));
        accounts.add(new Account("yalin@gmail.com", GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE));
        populateAccountList(accounts);
    }

    /**
     * @param accounts The list of available accounts, the current one being at position 0.
     */
    private void populateAccountList(List<Account> accounts) {
        mAccountSpinner = (Spinner) mActivity.findViewById(R.id.account_spinner);
        mAccountSpinner.getBackground().setColorFilter(
                ContextCompat.getColor(mActivity, R.color.body_text_1_inverse),
                PorterDuff.Mode.SRC_ATOP);
        mAccountSpinnerAdapter = new AccountSpinnerAdapter(mActivity, R.id.profile_name_text,
                accounts.toArray(new Account[accounts.size()]), mImageLoader);
        mAccountSpinner.setAdapter(mAccountSpinnerAdapter);
        mAccountSpinner.setSelection(0);

        mAccountSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    Intent switchUser = new Intent(getContext(), SwitchUserActivity.class);
//                    mActivity.startActivityForResult(switchUser, BaseActivity.SWITCH_USER_RESULT);
//                    return true;
//                }
                return false;
            }
        });
        mAccountSpinner.setEnabled(true);
    }

    public boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    public void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void onNavDrawerItemClicked(final NavigationItemEnum item) {
        if (item == mSelfItem) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (isSpecialItem(item)) {
            itemSelected(item);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    itemSelected(item);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            setSelectedNavDrawerItem(item);

            // todo add alpha animation
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private boolean isSpecialItem(NavigationItemEnum item) {
        return item == NavigationItemEnum.SETTINGS;
    }

    public void markAccountSpinnerAsNotShowingDropDownView() {
        mAccountSpinnerDropDownViewVisible = false;
    }

    public void onSaveInstanceState(Bundle outState) {
        if (mAccountSpinnerAdapter != null) {
            outState.putBoolean(ACCOUNT_SPINNER_DROP_DOWN_VISIBLE,
                    mAccountSpinnerDropDownViewVisible);
        }
    }

    public void onRestoreInstanceState(Bundle saveInstanceState) {
        if (saveInstanceState != null &&
                saveInstanceState.containsKey(ACCOUNT_SPINNER_DROP_DOWN_VISIBLE)) {
            mSetAccountSpinnerInDropViewModeWhenFirstShown =
                    saveInstanceState.getBoolean(ACCOUNT_SPINNER_DROP_DOWN_VISIBLE);
        }
    }

    public interface NavigationDrawerStateListener {
        void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating);

        void onNavDrawerSlide(float slideOffset);
    }
}
