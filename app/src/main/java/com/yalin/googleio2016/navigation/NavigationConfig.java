package com.yalin.googleio2016.navigation;

import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * YaLin
 * 2016/11/25.
 * Configuration file for items to show in the {@link AppNavigationView}. This is used by the {@link
 * NavigationModel}.
 */
public class NavigationConfig {

    private final static NavigationItemEnum[] COMMON_ITEMS_AFTER_CUSTOM =
            new NavigationItemEnum[]{
                    NavigationItemEnum.SETTINGS
            };

    public final static NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDIN_ATTENDING =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.MY_SCHEDULE,
                            NavigationItemEnum.EXPLORE},
                    COMMON_ITEMS_AFTER_CUSTOM);

    public final static NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDIN_REMOTE =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.MY_SCHEDULE,
                            NavigationItemEnum.EXPLORE},
                    COMMON_ITEMS_AFTER_CUSTOM);

    public final static NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDOUT_ATTENDING =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.SIGN_IN,
                            NavigationItemEnum.MY_SCHEDULE,
                            NavigationItemEnum.EXPLORE},
                    COMMON_ITEMS_AFTER_CUSTOM);

    public final static NavigationItemEnum[] NAVIGATION_ITEMS_LOGGEDOUT_REMOTE =
            concatenateItems(new NavigationItemEnum[]{NavigationItemEnum.SIGN_IN,
                            NavigationItemEnum.MY_SCHEDULE,
                            NavigationItemEnum.EXPLORE},
                    COMMON_ITEMS_AFTER_CUSTOM);

    private static NavigationItemEnum[] concatenateItems(NavigationItemEnum[] first,
                                                         NavigationItemEnum[] second) {
        NavigationItemEnum[] items = new NavigationItemEnum[first.length + second.length];
        System.arraycopy(first, 0, items, 0, first.length);
        System.arraycopy(second, 0, items, first.length, second.length);
        return items;
    }

    public static NavigationItemEnum[] appendItem(NavigationItemEnum[] first,
                                                  NavigationItemEnum second) {
        return concatenateItems(first, new NavigationItemEnum[]{second});
    }


    public static NavigationItemEnum[] filterOutItemsDisabledInBuildConfig(
            NavigationItemEnum[] items) {
        List<NavigationItemEnum> enabledItems = new ArrayList<NavigationItemEnum>();
        for (NavigationItemEnum item : items) {
            boolean includeItem = true;
            switch (item) {
                case EXPLORE:
                    includeItem = BuildConfig.ENABLE_EXPLORE_IN_NAVIGATION;
                    break;
                case SIGN_IN:
                    includeItem = BuildConfig.ENABLE_SIGNIN_IN_NAVIGATION;
                    break;
                case SETTINGS:
                    includeItem = BuildConfig.ENABLE_SETTINGS_IN_NAVIGATION;
                    break;
                case DEBUG:
                    includeItem = BuildConfig.DEBUG;
                    break;
            }

            if (includeItem) {
                enabledItems.add(item);
            }
        }
        return enabledItems.toArray(new NavigationItemEnum[enabledItems.size()]);
    }
}
