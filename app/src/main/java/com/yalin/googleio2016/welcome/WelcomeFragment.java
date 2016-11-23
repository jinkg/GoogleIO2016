package com.yalin.googleio2016.welcome;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.yalin.googleio2016.explore.ExploreIOActivity;

/**
 * YaLin
 * 2016/11/23.
 */

public abstract class WelcomeFragment extends Fragment {

    protected Activity mActivity;

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (mActivity instanceof WelcomeFragmentContainer) {
            WelcomeFragmentContainer activity = (WelcomeFragmentContainer) mActivity;
            attachToPrimaryButton(activity.getPrimaryButton());
            attachToSecondaryButton(activity.getSecondaryButton());
            activity.setButtonBarVisibility(shouldShowButtonBar());
        }
        return view;
    }

    protected void attachToPrimaryButton(Button button) {
        button.setText(getPrimaryButtonText());
        button.setOnClickListener(getPrimaryButtonListener());
    }

    protected void attachToSecondaryButton(Button button) {
        String secondaryButtonText = getSecondaryButtonText();
        View.OnClickListener secondaryButtonClickListener = getSecondaryButtonListener();
        if (!TextUtils.isEmpty(secondaryButtonText) && secondaryButtonClickListener != null) {
            button.setVisibility(View.VISIBLE);
            button.setText(secondaryButtonText);
            button.setOnClickListener(secondaryButtonClickListener);
        }
    }

    protected String getResourceString(int id) {
        if (mActivity != null) {
            return mActivity.getResources().getString(id);
        }
        return null;
    }

    public abstract boolean shouldDisplay(Context context);

    protected abstract String getPrimaryButtonText();

    protected abstract String getSecondaryButtonText();

    protected abstract View.OnClickListener getPrimaryButtonListener();

    protected abstract View.OnClickListener getSecondaryButtonListener();

    protected boolean shouldShowButtonBar() {
        return true;
    }

    void doNext() {
        Intent intent = new Intent(mActivity, ExploreIOActivity.class);
        startActivity(intent);
        mActivity.finish();
    }

    protected abstract class WelcomeFragmentOnClickListener implements View.OnClickListener {
        Activity mActivity;

        public WelcomeFragmentOnClickListener(Activity activity) {
            mActivity = activity;
        }
    }

    interface WelcomeFragmentContainer {
        Button getPrimaryButton();

        void setPrimaryButtonEnabled(Boolean enabled);

        Button getSecondaryButton();

        void setButtonBarVisibility(boolean isVisible);
    }
}
