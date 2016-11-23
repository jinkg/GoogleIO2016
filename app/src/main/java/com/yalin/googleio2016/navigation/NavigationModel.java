package com.yalin.googleio2016.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.Model;
import com.yalin.googleio2016.archframework.QueryEnum;
import com.yalin.googleio2016.archframework.UserActionEnum;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationQueryEnum;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationUserActionEnum;

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
        return new NavigationQueryEnum[0];
    }

    @Override
    public NavigationUserActionEnum[] getUserActions() {
        return new NavigationUserActionEnum[0];
    }

    @Override
    public void deliverUserAction(NavigationUserActionEnum action, @Nullable Bundle args, UserActionCallback callback) {

    }

    @Override
    public void requestData(NavigationQueryEnum query, DataQueryCallback callback) {

    }

    @Override
    public void cleanUp() {

    }

    public enum NavigationItemEnum {
        SIGN_IN(R.id.signin_nav_item, R.string.navdrawer_item_sign_in, 0, null),
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
