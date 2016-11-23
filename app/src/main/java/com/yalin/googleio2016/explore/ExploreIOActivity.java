package com.yalin.googleio2016.explore;

import android.os.Bundle;
import android.widget.TextView;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.ui.BaseActivity;

public class ExploreIOActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_io);
    }

    @Override
    public void onAuthSuccess(String accountName, boolean newlyAuthenticated) {
        super.onAuthSuccess(accountName, newlyAuthenticated);
        ((TextView) findViewById(R.id.account)).setText(accountName);
    }

    @Override
    public void onAuthFailure(String accountName) {
        super.onAuthFailure(accountName);
        ((TextView) findViewById(R.id.account)).setText(accountName);
    }
}
