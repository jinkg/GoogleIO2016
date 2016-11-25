package com.yalin.googleio2016.debug;

import com.yalin.googleio2016.navigation.NavigationModel;
import com.yalin.googleio2016.ui.BaseActivity;

/**
 * YaLin
 * 2016/11/25.
 */

public class DebugActivity extends BaseActivity {
    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.DEBUG;
    }
}
