package com.yalin.googleio2016.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.MyScheduleActivity;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.Model;
import com.yalin.googleio2016.archframework.QueryEnum;
import com.yalin.googleio2016.archframework.UserActionEnum;
import com.yalin.googleio2016.debug.DebugActivity;
import com.yalin.googleio2016.explore.ExploreIOActivity;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationQueryEnum;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationUserActionEnum;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.util.AccountUtils;

/**
 * YaLin
 * 2016/11/23.
 */

public class NavigationModel implements Model<NavigationQueryEnum, NavigationUserActionEnum> {

    private Context mContext;

    private NavigationItemEnum[] mItems;

    public NavigationModel(Context context) {
        mContext = context;
    }

    public NavigationItemEnum[] getItems() {
        return mItems;
    }

    @Override
    public NavigationQueryEnum[] getQueries() {
        return NavigationQueryEnum.values();
    }

    @Override
    public NavigationUserActionEnum[] getUserActions() {
        return NavigationUserActionEnum.values();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deliverUserAction(NavigationUserActionEnum action,
                                  @Nullable Bundle args,
                                  UserActionCallback callback) {
        switch (action) {
            case RELOAD_ITEMS:
                mItems = null;
                populateNavigationItems();
                callback.onModelUpdated(this, action);
                break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void requestData(NavigationQueryEnum query, DataQueryCallback callback) {
        switch (query) {
            case LOAD_ITEMS:
                if (mItems != null) {
                    callback.onModelUpdated(this, query);
                } else {
                    populateNavigationItems();
                    callback.onModelUpdated(this, query);
                }
                break;
        }
    }

    private void populateNavigationItems() {
        boolean attendeeAtVenue = SettingsUtils.isAttendeeAtVenue(mContext);
        boolean loggedIn = AccountUtils.hasActiveAccount(mContext);
        boolean debug = BuildConfig.DEBUG;

        NavigationItemEnum[] items;

        if (loggedIn) {
            if (attendeeAtVenue) {
                items = NavigationConfig.NAVIGATION_ITEMS_LOGGEDIN_ATTENDING;
            } else {
                items = NavigationConfig.NAVIGATION_ITEMS_LOGGEDIN_REMOTE;
            }
        } else {
            if (attendeeAtVenue) {
                items = NavigationConfig.NAVIGATION_ITEMS_LOGGEDOUT_ATTENDING;
            } else {
                items = NavigationConfig.NAVIGATION_ITEMS_LOGGEDOUT_REMOTE;
            }
        }

        if (debug) {
            items = NavigationConfig.appendItem(items, NavigationItemEnum.DEBUG);
        }

        mItems = NavigationConfig.filterOutItemsDisabledInBuildConfig(items);
    }

    @Override
    public void cleanUp() {
        mContext = null;
    }

    public enum NavigationItemEnum {
        MY_SCHEDULE(R.id.myschedule_nav_item, R.string.navdrawer_item_my_schedule,
                R.drawable.ic_navview_schedule, MyScheduleActivity.class),
        EXPLORE(R.id.explore_nav_item, R.string.navdrawer_item_explore,
                R.drawable.ic_navview_explore, ExploreIOActivity.class, true),
        SIGN_IN(R.id.signin_nav_item, R.string.navdrawer_item_sign_in, 0, null),
        DEBUG(R.id.debug_nav_item, R.string.navdrawer_item_debug,
                R.drawable.ic_navview_settings, DebugActivity.class),
        SETTINGS(1, 0, 0, null),
        INVALID(12, 0, 0, null);

        private int id;

        private int titleResource;

        private int iconResource;

        private Class classToLaunch;

        private boolean finishCurrentActivity;

        NavigationItemEnum(int id, int titleResource, int iconResource, Class classToLaunch) {
            this(id, titleResource, iconResource, classToLaunch, false);
        }

        NavigationItemEnum(int id, int titleResource, int iconResource, Class classToLaunch,
                           boolean finishCurrentActivity) {
            this.id = id;
            this.titleResource = titleResource;
            this.iconResource = iconResource;
            this.classToLaunch = classToLaunch;
            this.finishCurrentActivity = finishCurrentActivity;
        }

        public int getId() {
            return id;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getIconResource() {
            return iconResource;
        }

        public Class getClassToLaunch() {
            return classToLaunch;
        }

        public boolean finishCurrentActivity() {
            return finishCurrentActivity;
        }

        public static NavigationItemEnum getById(int id) {
            NavigationItemEnum[] values = NavigationItemEnum.values();
            for (int i = 0; i < values().length; i++) {
                if (values[i].getId() == id) {
                    return values[i];
                }
            }
            return INVALID;
        }
    }

    public enum NavigationQueryEnum implements QueryEnum {
        LOAD_ITEMS(0);

        private int id;

        NavigationQueryEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return new String[0];
        }
    }

    public enum NavigationUserActionEnum implements UserActionEnum {
        RELOAD_ITEMS(0);

        private int id;

        NavigationUserActionEnum(int id) {
            this.id = id;
        }

        @Override
        public int getId() {
            return id;
        }
    }
}
