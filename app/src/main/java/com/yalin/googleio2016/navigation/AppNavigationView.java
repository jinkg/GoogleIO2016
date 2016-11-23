package com.yalin.googleio2016.navigation;

import android.app.Activity;

import com.yalin.googleio2016.login.LoginStateListener;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;

/**
 * YaLin
 * 2016/11/23.
 */

public interface AppNavigationView {
    /**
     * Call this when the {@link Activity} is ready to process the NavigationView. Implements
     * general set up of the view.
     *
     * @param activity           The activity showing the NavigationView
     * @param loginStateListener The navigation contains state related to login, so a login listener
     *                           should be attached to it.
     * @param self               The {@link NavigationItemEnum} of the activity showing the
     *                           NavigationView. Pass in {@link NavigationItemEnum#INVALID} if the
     *                           activity should not display the NavigationView.
     */
    void activityReady(Activity activity, LoginStateListener loginStateListener,
                       NavigationItemEnum self);

    /**
     * Implements UI specific logic to perform initial set up for the NavigationView. This is
     * expected to be called only once.
     */
    void setUpView();

    /**
     * Call this when some action in the {@link Activity} requires the navigation items to be
     * refreshed (eg user logging in). Implements updating navigation items.
     */
    void updateNavigationItems();

    /**
     * Implements UI specific logic to display the {@code items}. This is expected to be called each
     * time the navigation items change.
     */
    void displayNavigationItems(NavigationItemEnum[] items);

    /**
     * Implements launching the {@link Activity} linked to the {@code item}.
     */
    void itemSelected(NavigationItemEnum item);

    /**
     * Implements UI specific logic to display the NavigationView. Note that if the NavigationView
     * should always be visible, this method is empty.
     */
    void showNavigation();
}
