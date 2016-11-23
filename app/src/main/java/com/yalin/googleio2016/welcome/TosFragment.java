package com.yalin.googleio2016.welcome;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.settings.SettingsUtils;

/**
 * YaLin
 * 2016/11/23.
 */

public class TosFragment extends WelcomeFragment {
    @Override
    public boolean shouldDisplay(Context context) {
        return !SettingsUtils.isTosAccepted(context);
    }

    @Override
    protected String getPrimaryButtonText() {
        return getResourceString(R.string.accept);
    }

    @Override
    protected String getSecondaryButtonText() {
        return null;
    }

    @Override
    protected View.OnClickListener getPrimaryButtonListener() {
        return new WelcomeFragmentOnClickListener(mActivity) {
            @Override
            public void onClick(View v) {
                SettingsUtils.markTosAccepted(mActivity, true);
                doNext();
            }
        };
    }

    @Override
    protected View.OnClickListener getSecondaryButtonListener() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.welcome_tos_fragment, container, false);
    }
}
