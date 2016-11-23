package com.yalin.googleio2016.welcome;

import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.util.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * YaLin
 * 2016/11/23.
 */

public class WelcomeActivity extends AppCompatActivity
        implements WelcomeFragment.WelcomeFragmentContainer {

    WelcomeFragment mContentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mContentFragment = getCurrentFragment(this);
        if (mContentFragment == null) {
            finish();
        } else {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.welcome_content, mContentFragment);
            fragmentTransaction.commit();

            final ImageView iv = (ImageView) findViewById(R.id.logo);
            final AnimatedVectorDrawableCompat logo =
                    AnimatedVectorDrawableCompat.create(this, R.drawable.avd_hash_io_16);
            if (iv != null && logo != null) {
                iv.setImageDrawable(logo);

                if (UIUtils.animationEnabled(getContentResolver())) {
                    logo.start();
                }
            }
        }
    }

    public static WelcomeFragment getCurrentFragment(Context context) {
        List<WelcomeFragment> welcomeActivityContents = getWelcomeFragments();

        for (WelcomeFragment fragment : welcomeActivityContents) {
            if (fragment.shouldDisplay(context)) {
                return fragment;
            }
        }
        return null;
    }

    public static boolean shouldDisplay(Context context) {
        WelcomeFragment fragment = getCurrentFragment(context);
        return fragment != null;
    }

    private static List<WelcomeFragment> getWelcomeFragments() {
        return new ArrayList<>(Arrays.asList(
                new TosFragment(),
                new AccountFragment()
        ));
    }

    @Override
    public Button getPrimaryButton() {
        return (Button) findViewById(R.id.button_accept);
    }

    @Override
    public void setPrimaryButtonEnabled(Boolean enabled) {
        getPrimaryButton().setEnabled(enabled);
    }

    @Override
    public Button getSecondaryButton() {
        return (Button) findViewById(R.id.button_decline);
    }

    @Override
    public void setButtonBarVisibility(boolean isVisible) {
        findViewById(R.id.welcome_button_bar).setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (!isVisible) {
            ((ViewGroup.MarginLayoutParams) findViewById(R.id.welcome_scrolling_content)
                    .getLayoutParams()).bottomMargin = 0;
        }
    }
}
