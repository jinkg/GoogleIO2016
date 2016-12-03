package com.yalin.googleio2016.login;

import android.provider.Settings;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.explore.ExploreIOActivity;
import com.yalin.googleio2016.testutils.LoginUtils;
import com.yalin.googleio2016.util.AccountUtils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * YaLin
 * 2016/12/3.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BaseActivity_InactiveAccountTest {
    private static final String ACCOUNT_NAME = null;

    @Rule
    public ActivityTestRule<ExploreIOActivity> mActivityRule =
            new ActivityTestRule<ExploreIOActivity>(ExploreIOActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    super.beforeActivityLaunched();

                    // Set inactive account
                    AccountUtils.setActiveAccount(InstrumentationRegistry.getTargetContext(),
                            ACCOUNT_NAME);
                }
            };

    @After
    public void cleanUpInactiveAccount() {
        LoginUtils.setFirstAvailableAccountAsActive(InstrumentationRegistry.getTargetContext());
    }

    /**
     * The test will fail on API < 17, due to the looping animation in {@link
     * com.yalin.googleio2016.welcome.WelcomeActivity}. On API 17+, the activity checks the
     * settings for {@link Settings.Global#ANIMATOR_DURATION_SCALE} and doesn't run the animation if
     * it is turned off.
     */
    @SdkSuppress(minSdkVersion = 17)
    @Test
    public void welcomeActivityForAccount_IsDisplayed() {
        onView(withText(R.string.welcome_select_account)).check(matches(isDisplayed()));
    }
}
