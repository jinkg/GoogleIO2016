package com.yalin.googleio2016.debug;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.navigation.NavigationModel;
import com.yalin.googleio2016.ui.BaseActivity;

/**
 * YaLin
 * 2016/11/25.
 */

public class DebugActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
    }

    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.DEBUG;
    }
}
