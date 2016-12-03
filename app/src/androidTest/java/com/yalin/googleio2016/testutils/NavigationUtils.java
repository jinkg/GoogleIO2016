package com.yalin.googleio2016.testutils;

import android.support.v7.widget.AppCompatCheckedTextView;
import android.view.View;

import com.yalin.googleio2016.R;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * YaLin
 * 2016/12/3.
 * <p>
 * Methods to help with navigation testing
 */
public class NavigationUtils {
    public static void showNavigation() {
        onView(withId(R.id.drawer_layout)).perform(open());
    }

    public static void checkNavigationItemIsDisplayed(int navigationItemStringResource) {
        NavigationUtils.showNavigation();

        onView(getNavigationItemWithString(navigationItemStringResource)).check(
                matches(isDisplayed()));
    }

    private static Matcher<View> getNavigationItemWithString(int stringResource) {
        return allOf(isAssignableFrom(AppCompatCheckedTextView.class), withText(stringResource));
    }
}
