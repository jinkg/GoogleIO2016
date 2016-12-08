package com.yalin.googleio2016.explore;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.navigation.NavigationModel;
import com.yalin.googleio2016.ui.BaseActivity;
import com.yalin.googleio2016.ui.SearchActivity;

/**
 * Display a summary of what is happening at Google I/O this year. Theme and topic cards are
 * displayed based on the session data. Conference messages are also displayed as cards..
 */
public class ExploreIOActivity extends BaseActivity implements Toolbar.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent launchIntent = getIntent();
        if (launchIntent != null && (!Intent.ACTION_MAIN.equals(launchIntent.getAction())
                || !launchIntent.hasCategory(Intent.CATEGORY_LAUNCHER))) {
            overridePendingTransition(0, 0);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_io);
        setTitle(R.string.title_explore);

        // TODO: 2016/12/8
        // ANALYTICS SCREEN: View the Explore I/O screen
        // Contains: Nothing (Page name is a constant)
//        AnalyticsHelper.sendScreenView(SCREEN_LABEL);
    }

    @Override
    protected NavigationModel.NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationModel.NavigationItemEnum.EXPLORE;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Add the search button to the toolbar.
        Toolbar toolbar = getToolbar();
        toolbar.inflateMenu(R.menu.explore_io_menu);
        toolbar.setOnMenuItemClickListener(this);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
        }
        return false;
    }
}
