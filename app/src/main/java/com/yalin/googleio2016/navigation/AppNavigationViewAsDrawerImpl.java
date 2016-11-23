package com.yalin.googleio2016.navigation;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.Spinner;

import com.yalin.googleio2016.util.ImageLoader;

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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void displayNavigationItems(NavigationModel.NavigationItemEnum[] items) {

    }

    @Override
    public void setUpView() {

    }

    @Override
    public void showNavigation() {

    }

    public interface NavigationDrawerStateListener {
        void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating);

        void onNavDrawerSlide(float slideOffset);
    }
}
