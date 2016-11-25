package com.yalin.googleio2016.explore;

import android.os.Bundle;
import android.widget.TextView;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.navigation.NavigationModel;
import com.yalin.googleio2016.ui.BaseActivity;

public class ExploreIOActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_io);
    }

    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.EXPLORE;
    }
}
