package com.yalin.googleio2016.myschedule;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.PresenterImpl;
import com.yalin.googleio2016.archframework.UpdatableView;
import com.yalin.googleio2016.injection.ModelProvider;
import com.yalin.googleio2016.model.ScheduleHelper;
import com.yalin.googleio2016.navigation.NavigationModel.NavigationItemEnum;
import com.yalin.googleio2016.ui.BaseActivity;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.TimeUtils;


import java.util.Date;
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
public class MyScheduleActivity extends BaseActivity implements
        MyScheduleSingleDayFragment.Listener {
    private static final String TAG = "MyScheduleActivity";

    /**
     * This is used in the narrow mode, to pass in the day index to the {@link
     * MyScheduleSingleDayFragment}.
     */
    public static final String ARG_CONFERENCE_DAY_INDEX
            = "com.yalin.googleio2016.ARG_CONFERENCE_DAY_INDEX";

    public static final String EXTRA_DIALOG_TITLE
            = "com.yalin.googleio2016.EXTRA_DIALOG_TITLE";
    public static final String EXTRA_DIALOG_MESSAGE
            = "com.yalin.googleio2016.EXTRA_DIALOG_MESSAGE";
    public static final String EXTRA_DIALOG_YES
            = "com.yalin.googleio2016.EXTRA_DIALOG_YES";
    public static final String EXTRA_DIALOG_NO
            = "com.yalin.googleio2016.EXTRA_DIALOG_NO";
    public static final String EXTRA_DIALOG_URL
            = "com.yalin.googleio2016.EXTRA_DIALOG_URL";

    /**
     * Interval that a timer will redraw the UI during the conference, so that time sensitive
     * widgets, like the "Now" and "Ended" indicators can be properly updated.
     */
    private static final long INTERVAL_TO_REDRAW_UI = 1 * TimeUtils.MINUTE;

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

    public static int BASE_TAB_VIEW_ID = 12345;

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
            = new HashSet<>();

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

    /**
     * During the conference, this is set to the current day, eg 1 for the first day, 2 for the
     * second etc Outside of conference period, this is set to 1.
     */
    private int mToday;

    /**
     * True during the conference or pre conference, false otherwise
     */
    private boolean mConferenceInProgress;

    private boolean mDestroyed = false;

    private boolean mShowedAnnouncementDialog = false;

    private PresenterImpl mPresenter;

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

        initViews(singleDayFragmentsTags, currentSingleDayFragment);
        initPresenter();

        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        calculateCurrentDay();

        if (mConferenceInProgress) {
            scheduleNextUIUpdate();
        }

        showAnnouncementDialogIfNeeded(getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mViewPagerAdapter != null && mViewPagerAdapter.getFragments() != null) {
            MyScheduleSingleDayFragment[] singleDayFragments = mViewPagerAdapter.getFragments();
            String[] tags = new String[singleDayFragments.length];
            for (int i = 0; i < tags.length; i++) {
                tags[i] = singleDayFragments[i].getTag();
            }
            outState.putStringArray(SINGLE_DAY_FRAGMENTS_TAGS, tags);
            outState.putInt(CURRENT_SINGLE_DAY_FRAGMENT_POSITION, mViewPager.getCurrentItem());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDestroyed = true;
    }

    /**
     * Pre-process the {@code intent} received to open this activity to determine if it was a deep
     * link to a SessionDetail. Typically you wouldn't use this type of logic, but we need to
     * because of the path of session details page on the website is only /schedule and session ids
     * are part of the query parameters ("sid").
     */
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
            setUpViewForWideMode();
        } else {
            setUpViewPagerForNarrowMode(singleDayFragmentsTags, currentSingleDayFragment);
        }
    }

    private void initPresenter() {
        MyScheduleModel model =
                ModelProvider.provideMyScheduleMode(new ScheduleHelper(this), this);
        if (mWideMode) {
            // Do nothing.
        } else {
            MyScheduleSingleDayFragment[] fragments = mViewPagerAdapter.getFragments();
            UpdatableView[] views = new UpdatableView[fragments.length];
            for (int i = 0; i < fragments.length; i++) {
                views[i] = fragments[i];
            }
            mPresenter = new PresenterImpl(model, views,
                    MyScheduleModel.MyScheduleUserActionEnum.values(),
                    MyScheduleModel.MyScheduleQueryEnum.values());
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
        // not support
    }

    /**
     * @param singleDayFragmentsTags   The tags of the recreated fragments, if this is an Activity
     *                                 recreation, or null
     * @param currentSingleDayFragment The position of the current single day fragment (ie the
     *                                 position of the current tab)
     */
    private void setUpViewPagerForNarrowMode(String[] singleDayFragmentsTags,
                                             int currentSingleDayFragment) {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPagerAdapter = new MyScheduleDayViewPagerAdapter(this, getFragmentManager(),
                MyScheduleModel.showPreConferenceData(this));
        mViewPagerAdapter.setRetainedFragmentsTags(singleDayFragmentsTags);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.setCurrentItem(currentSingleDayFragment);


        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition(), true);
                TextView view = (TextView) findViewById(BASE_TAB_VIEW_ID + tab.getPosition());
                view.setContentDescription(
                        getString(R.string.talkback_selected,
                                getString(R.string.a11y_button, tab.getText())));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                TextView view = (TextView) findViewById(BASE_TAB_VIEW_ID + tab.getPosition());
                view.setContentDescription(getString(R.string.a11y_button, tab.getText()));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothing
            }
        });
        mViewPager.setPageMargin(getResources()
                .getDimensionPixelSize(R.dimen.my_schedule_page_margin));
        mViewPager.setPageMarginDrawable(R.drawable.page_margin);

        setTabLayoutContentDescriptionsForNarrowLayout();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        calculateCurrentDay();
        if (mViewPager != null) {
            showDay(mToday);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        launchSessionDetailIfRequiredByIntent(intent);
        LogUtil.d(TAG, "onNewIntent, extras " + intent.getExtras());
        if (intent.hasExtra(EXTRA_DIALOG_MESSAGE)) {
            mShowedAnnouncementDialog = false;
            showAnnouncementDialogIfNeeded(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.my_schedule, menu);
        return true;
    }

    private void setTabLayoutContentDescriptionsForNarrowLayout() {
        LayoutInflater inflater = getLayoutInflater();
        int gap = MyScheduleModel.showPreConferenceData(this) ? 1 : 0;
        for (int i = 0, count = mTabLayout.getTabCount(); i < count; i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            TextView view = (TextView)
                    inflater.inflate(R.layout.tab_my_schedule, mTabLayout, false);
            view.setId(BASE_TAB_VIEW_ID + i);
            view.setText(tab.getText());
            if (i == 0) {
                view.setContentDescription(
                        getString(R.string.talkback_selected,
                                getString(R.string.a11y_button, tab.getText())));
            } else {
                view.setContentDescription(
                        getString(R.string.a11y_button, tab.getText()));
            }
            if (Build.VERSION.SDK_INT >= 16) {
                view.announceForAccessibility(getString(R.string.my_schedule_tab_desc_a11y,
                        TimeUtils.getDayName(this, i - gap)));
            }
            tab.setCustomView(view);
        }
    }

    private void showAnnouncementDialogIfNeeded(Intent intent) {
        final String title = intent.getStringExtra(EXTRA_DIALOG_TITLE);
        final String message = intent.getStringExtra(EXTRA_DIALOG_MESSAGE);

        if (!mShowedAnnouncementDialog && !TextUtils.isEmpty(title) &&
                !TextUtils.isEmpty(message)) {
            LogUtil.d(TAG, "showAnnouncementDialogIfNeeded, title: " + title);
            LogUtil.d(TAG, "showAnnouncementDialogIfNeeded, message: " + message);
            final String yes = intent.getStringExtra(EXTRA_DIALOG_YES);
            LogUtil.d(TAG, "showAnnouncementDialogIfNeeded, yes: " + yes);
            final String no = intent.getStringExtra(EXTRA_DIALOG_NO);
            LogUtil.d(TAG, "showAnnouncementDialogIfNeeded, no: " + no);
            final String url = intent.getStringExtra(EXTRA_DIALOG_URL);
            LogUtil.d(TAG, "showAnnouncementDialogIfNeeded, url: " + url);
            final SpannableString spannable = new SpannableString(message);
            Linkify.addLinks(spannable, Linkify.WEB_URLS);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setNegativeButton(no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setPositiveButton(yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();
            final TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            if (messageView != null) {
                // makes the embedded links in the text clickable, if there are any
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            mShowedAnnouncementDialog = true;
        }
    }

    @Override
    public void onAccountChangeRequested() {
        super.onAccountChangeRequested();
        hideLoginFailureView();
        reloadData();
    }

    private void reloadData() {
        if (mPresenter != null) {
            mPresenter.onUserAction(MyScheduleModel.MyScheduleUserActionEnum.RELOAD_DATA, null);
        }
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        if (mWideMode) {
            return false;
        }
        for (MyScheduleSingleDayFragment fragment : mMyScheduleSingleDayFragments) {
            if (Build.VERSION.SDK_INT >= 15) {
                if (!fragment.getUserVisibleHint()) {
                    continue;
                }
            }
            return ViewCompat.canScrollVertically(fragment.getListView(), -1);
        }
        return false;
    }

    private void calculateCurrentDay() {
        long now = TimeUtils.getCurrentTime(this);

        // If we are before or after the conference, the first day is considered the current day
        mToday = 1;
        mConferenceInProgress = false;

        for (int i = 0; i < Config.CONFERENCE_DAYS.length; i++) {
            if (now >= Config.CONFERENCE_DAYS[i][0] && now <= Config.CONFERENCE_DAYS[i][1]) {
                mToday = i + 1;
                mConferenceInProgress = true;
                break;
            }
        }
    }

    /**
     * @param day Pass in 1 for the first day, 2 for the second etc
     */
    private void showDay(int day) {
        int preConferenceDays = MyScheduleModel.showPreConferenceData(this) ? 1 : 0;
        mViewPager.setCurrentItem(day - 1 + preConferenceDays);
    }

    private void hideLoginFailureView() {
        mFailedLoginView.setVisibility(View.GONE);
    }

    private Runnable mUpdateUIRunnable = new Runnable() {

        @Override
        public void run() {
            MyScheduleActivity activity = MyScheduleActivity.this;
            if (activity.hasBeenDestroyed()) {
                LogUtil.d(TAG, "Activity is not valid anymore. Stopping UI Updater");
                return;
            }

            LogUtil.d(TAG, "Running MySchedule UI updater (now=" +
                    new Date(TimeUtils.getCurrentTime(activity)) + ")");

            mPresenter.onUserAction(MyScheduleModel.MyScheduleUserActionEnum.REDRAW_UI, null);

            if (mConferenceInProgress) {
                scheduleNextUIUpdate();
            }
        }
    };

    private Handler mUpdateUIHandler = new Handler();

    private void scheduleNextUIUpdate() {
        // Remove existing UI update runnable, if any
        mUpdateUIHandler.removeCallbacks(mUpdateUIRunnable);

        // Post runnable with delay
        mUpdateUIHandler.postDelayed(mUpdateUIRunnable, INTERVAL_TO_REDRAW_UI);
    }

    private boolean hasBeenDestroyed() {
        return mDestroyed;
    }

    @Override
    public void onSingleDayFragmentAttached(MyScheduleSingleDayFragment fragment) {
        mMyScheduleSingleDayFragments.add(fragment);
    }

    @Override
    public void onSingleDayFragmentDetached(MyScheduleSingleDayFragment fragment) {
        mMyScheduleSingleDayFragments.remove(fragment);
    }
}
