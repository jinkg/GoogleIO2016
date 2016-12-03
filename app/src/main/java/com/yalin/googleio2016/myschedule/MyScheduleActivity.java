package com.yalin.googleio2016.myschedule;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;
import com.yalin.googleio2016.ui.BaseActivity;
import com.yalin.googleio2016.util.LogUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * YaLin
 * 2016/12/3.
 * <p>
 * This shows the schedule of the logged in user, organised per day.
 * <p/>
 * Depending on the device, this Activity uses either a {@link ViewPager} with a {@link
 * MyScheduleSingleDayFragment} for its page (the "narrow" layout) or a {@link
 * MyScheduleAllDaysFragment}, which uses a {@link MyScheduleSingleDayNoScrollView} for each day.
 * Each day data is backed by a {@link MyScheduleDayAdapter} (the "wide" layout).
 * <p/>
 * If the user attends the conference, all time slots that have sessions are shown, with a button to
 * allow the user to see all sessions in that slot.
 */
public class MyScheduleActivity extends BaseActivity {
    private static final String TAG = "MyScheduleActivity";

    /**
     * This is used in the narrow mode, to pass in the day index to the {@link
     * MyScheduleSingleDayFragment}.
     */
    public static final String ARG_CONFERENCE_DAY_INDEX
            = "com.yalin.googleio2016.ARG_CONFERENCE_DAY_INDEX";

    /**
     * The key used to save the tags for {@link MyScheduleSingleDayFragment}s so the automatically
     * recreated fragments can be reused by {@link #mViewPagerAdapter}.
     */
    private static final String SINGLE_DAY_FRAGMENTS_TAGS = "single_day_fragments_tags";

    /**
     * The key used to save the position in the {@link #mViewPagerAdapter} for the current {@link
     * MyScheduleSingleDayFragment}s.
     */
    private static final String CURRENT_SINGLE_DAY_FRAGMENT_POSITION =
            "current_single_day_fragments_position";

    /**
     * If true, we are in the wide (tablet landscape) mode where we show conference days side by
     * side; if false, we are in narrow (non tablet landscape) mode where we use a ViewPager and
     * show one conference day per page.
     */
    private boolean mWideMode = false;

    /**
     * This is used for narrow mode only, to switch between days, it is null in wide mode
     */
    private ViewPager mViewPager;

    /**
     * This is used for narrow mode only, it is empty in wide mode
     */
    private Set<MyScheduleSingleDayFragment> mMyScheduleSingleDayFragments
            = new HashSet<MyScheduleSingleDayFragment>();

    /**
     * This is used for narrow mode only, it is null in wide mode. Each page in the {@link
     * #mViewPager} is a {@link MyScheduleSingleDayFragment}.
     */
    private MyScheduleDayViewPagerAdapter mViewPagerAdapter;

    /**
     * This is used for narrow mode only, to display the conference days, it is null in wide mode
     */
    private TabLayout mTabLayout;

    /**
     * This is a view displayed when login has failed
     */
    private View mFailedLoginView;

    @Override
    protected NavigationItemEnum getSelfNavDrawerItem() {
        return NavigationItemEnum.MY_SCHEDULE;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);

        launchSessionDetailIfRequiredByIntent(getIntent());

        // TODO: 2016/12/3 add analytics

        String[] singleDayFragmentsTags = null;
        int currentSingleDayFragment = 0;

        if (savedInstanceState != null &&
                savedInstanceState.containsKey(SINGLE_DAY_FRAGMENTS_TAGS)) {
            singleDayFragmentsTags = savedInstanceState.getStringArray(SINGLE_DAY_FRAGMENTS_TAGS);
        }
        if (savedInstanceState != null &&
                savedInstanceState.containsKey(CURRENT_SINGLE_DAY_FRAGMENT_POSITION)) {
            currentSingleDayFragment =
                    savedInstanceState.getInt(CURRENT_SINGLE_DAY_FRAGMENT_POSITION);
        }

        overridePendingTransition(0, 0);
    }

    private void launchSessionDetailIfRequiredByIntent(Intent intent) {
        if (intent != null && !TextUtils.isEmpty(intent.getDataString())) {
            String intentDataString = intent.getDataString();
            try {
                Uri dataUri = Uri.parse(intentDataString);

                // Website sends sessionId in query parameter "sid". If present, show
                // SessionDetailActivity
                String sessionId = dataUri.getQueryParameter("sid");
                if (!TextUtils.isEmpty(sessionId)) {
                    LogUtil.d(TAG, "SessionId received from website: " + sessionId);
//                    SessionDetailActivity.startSessionDetailActivity(MyScheduleActivity.this,
//                            sessionId);
//                    finish();
                } else {
                    LogUtil.d(TAG, "No SessionId received from website");
                }
            } catch (Exception exception) {
                LogUtil.e(TAG, "Data uri existing but wasn't parsable for a session detail deep link");
            }
        }
    }

    /**
     * @param singleDayFragmentsTags   The tags of the recreated fragments, if this is an Activity
     *                                 recreation, or null
     * @param currentSingleDayFragment The position of the current single day fragment (ie the
     *                                 position of the current tab)
     */
    private void initViews(String[] singleDayFragmentsTags, int currentSingleDayFragment) {
        // Set up view to show login failure
        mFailedLoginView = findViewById(R.id.butter_bar);
        hideLoginFailureView();

        // Set up correct view mode
        detectNarrowOrWideMode();
        if (mWideMode) {

        } else {

        }
    }

    private void detectNarrowOrWideMode() {
        // When changing orientation, if previously in wide mode, the system recreates the wide
        // fragment, so need to check also that view pager isn't visible

//        mWideMode = getFragmentManager().findFragmentById(R.id.myScheduleWideFrag) != null &&
//                findViewById(R.id.view_pager).getVisibility() == View.GONE
        mWideMode = false;
    }

    private void setUpViewForWideMode() {

    }

    private void setUpViewPagerForNarrowMode(String[] singleDayFragmentsTags,
                                             int currentSingleDayFragment) {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
    }

    private void hideLoginFailureView() {
        mFailedLoginView.setVisibility(View.GONE);
    }
}
