package com.yalin.googleio2016.login;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.yalin.googleio2016.explore.ExploreIOActivity;
import com.yalin.googleio2016.injection.LoginAndAuthProvider;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.testutils.LoginUtils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.yalin.googleio2016.testutils.SyncUtils.waitText;

/**
 * YaLin
 * 2016/11/23.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class BaseActivity_ActiveAccountSuccessfulLoginTest {
    private String mAccountName;

    private StubLoginAndAuth mStubLoginAndAuth;

    @Rule
    public ActivityTestRule<ExploreIOActivity> mActivityRule =
            new ActivityTestRule<ExploreIOActivity>(ExploreIOActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    SettingsUtils.markTosAccepted(InstrumentationRegistry.getTargetContext(), true);

                    mAccountName = LoginUtils.setFirstAvailableAccountAsActive(
                            InstrumentationRegistry.getTargetContext());

                    mStubLoginAndAuth = new StubLoginAndAuth(mAccountName, true, true);
                    LoginAndAuthProvider.setStubLoginAndAuth(mStubLoginAndAuth);
                }

                @Override
                protected void afterActivityLaunched() {
                    mStubLoginAndAuth.setListener(mActivityRule.getActivity());
                }
            };

    @After
    public void cleanUp() {
        LoginAndAuthProvider.setStubLoginAndAuth(null);
    }

    @Test
    public void accountName_IsDisplayed() {
        onView(isRoot()).perform(waitText(mAccountName, TimeUnit.SECONDS.toMillis(5)));
//        onView(withText(mAccountName)).check(matches(isDisplayed()));
    }
}
