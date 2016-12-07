package com.yalin.googleio2016.myschedule;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.google.common.annotations.VisibleForTesting;
import com.yalin.googleio2016.BuildConfig;
import com.yalin.googleio2016.Config;
import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.Model;
import com.yalin.googleio2016.archframework.QueryEnum;
import com.yalin.googleio2016.archframework.UserActionEnum;
import com.yalin.googleio2016.model.ScheduleHelper;
import com.yalin.googleio2016.model.ScheduleItem;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.settings.SettingsUtils;
import com.yalin.googleio2016.util.LogUtil;
import com.yalin.googleio2016.util.ParserUtils;
import com.yalin.googleio2016.util.ThrottledContentObserver;

import java.util.ArrayList;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * YaLin
 * 2016/12/3.
 */

public class MyScheduleModel implements Model<MyScheduleModel.MyScheduleQueryEnum,
        MyScheduleModel.MyScheduleUserActionEnum> {
    private static final String TAG = "MyScheduleModel";

    public static final int PRE_CONFERENCE_DAY_ID = 0;

    /**
     * Used for user action {@link MyScheduleUserActionEnum#SESSION_SLOT}
     */
    public static final String SESSION_URL_KEY = "SESSION_URL_KEY";

    /**
     * Used for user action {@link MyScheduleUserActionEnum#FEEDBACK}
     */
    public static final String SESSION_ID_KEY = "SESSION_ID_KEY";

    /**
     * Used for user action {@link MyScheduleUserActionEnum#FEEDBACK}
     */
    public static final String SESSION_TITLE_KEY = "SESSION_TITLE_KEY";

    /**
     * The key of {@link #mScheduleData} is the index of the day in the conference, starting at 1
     * for the first day of the conference, using {@link #PRE_CONFERENCE_DAY_ID} for the
     * preconference day, if any.
     */
    protected SparseArray<ArrayList<ScheduleItem>> mScheduleData =
            new SparseArray<>();

    // The ScheduleHelper is responsible for feeding data in a format suitable to the Adapter.
    private ScheduleHelper mScheduleHelper;

    private Context mContext;

    protected DataQueryCallback mScheduleDataQueryCallback;

    /**
     * @param scheduleHelper helper
     * @param context        Should be an Activity context
     */
    public MyScheduleModel(ScheduleHelper scheduleHelper, Context context) {
        mScheduleHelper = scheduleHelper;
        mContext = context;
    }

    /**
     * Initialises the pre conference data and data observers. This is not called from the
     * constructor, to allow for unit tests to bypass this (as this uses Android methods not
     * available in unit tests).
     *
     * @return the Model it can be chained with the constructor
     */
    public MyScheduleModel initStaticDataAndObservers() {
        if (showPreConferenceData(mContext)) {
            preparePreConferenceDayAdapter();
        }
        addDataObservers();
        return this;
    }

    /**
     * This method is an ad-hoc implementation of the pre conference day, which contains an item to
     * pick up the badge at registration desk
     */
    private void preparePreConferenceDayAdapter() {
        ScheduleItem item = new ScheduleItem();
        item.title = mContext.getString(R.string.my_schedule_badgepickup);
        item.startTime = ParserUtils.parseTime(BuildConfig.PRECONFERENCE_DAY_START);
        item.endTime = ParserUtils.parseTime(BuildConfig.PRECONFERENCE_DAY_END);
        item.type = ScheduleItem.BREAK;
        item.room = item.subtitle =
                mContext.getString(R.string.my_schedule_badgepickup_description);
        item.sessionType = ScheduleItem.SESSION_TYPE_MISC;
        mScheduleData.put(PRE_CONFERENCE_DAY_ID, new ArrayList<>(Arrays.asList(item)));
    }

    public static boolean showPreConferenceData(Context context) {
        return SettingsUtils.isAttendeeAtVenue(context);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                      String key) {
                    LogUtil.d(TAG, "sharedpreferences key " + key + " changed, maybe reloading data.");
                    if (SettingsUtils.PREF_LOCAL_TIMES.equals(key)) {
                        // mPrefChangeListener is observing as soon as the model is created but
                        // mScheduleDataQueryCallback is only created when the view has requested
                        // some data. There is a tiny amount of time when mPrefChangeListener is
                        // active but mScheduleDataQueryCallback is null. This was observed when
                        // going to MySchedule screen straight after the welcome flow.
                        if (mScheduleDataQueryCallback != null) {
                            mScheduleDataQueryCallback.onModelUpdated(MyScheduleModel.this,
                                    MyScheduleQueryEnum.SCHEDULE);
                        } else {
                            LogUtil.e(TAG, "sharedpreferences key " + key +
                                    " changed, but null schedule data query callback, cannot " +
                                    "inform model is updated");
                        }
                    } else if (BuildConfig.PREF_ATTENDEE_AT_VENUE.equals(key)) {
                        updateData(mScheduleDataQueryCallback);
                    }
                }
            };

    /**
     * Observe changes on base uri and in shared preferences
     */
    private void addDataObservers() {
        mContext.getContentResolver().registerContentObserver(
                ScheduleContract.BASE_CONTENT_URI, true, mObserver);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
    }

    private void removeDataObservers() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        sp.unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
    }

    /**
     * Visible for classes extending this model, so UI tests can be written to simulate the system
     * firing this observer.
     */
    @VisibleForTesting
    protected final ThrottledContentObserver mObserver = new ThrottledContentObserver(
            new ThrottledContentObserver.Callbacks() {
                @Override
                public void onThrottledContentObserverFired() {
                    LogUtil.d(TAG, "content may be changed, reloading data");
                    updateData(mScheduleDataQueryCallback);
                }
            });


    @Override
    public MyScheduleQueryEnum[] getQueries() {
        return MyScheduleQueryEnum.values();
    }

    @Override
    public MyScheduleUserActionEnum[] getUserActions() {
        return MyScheduleUserActionEnum.values();
    }

    /**
     * @param day The day of the conference, starting at 1 for the first day
     * @return the list of items, or an empty list if the day isn't found
     */
    public ArrayList<ScheduleItem> getConferenceDataForDay(int day) {
        if (mScheduleData.indexOfKey(day) >= 0) {
            return mScheduleData.get(day);
        } else {
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deliverUserAction(final MyScheduleUserActionEnum action, @Nullable Bundle args,
                                  final UserActionCallback callback) {
        switch (action) {
            case RELOAD_DATA:
                DataQueryCallback queryCallback = new DataQueryCallback() {
                    @Override
                    public void onModelUpdated(Model model, QueryEnum query) {
                        callback.onModelUpdated(MyScheduleModel.this, action);
                    }

                    @Override
                    public void onError(QueryEnum query) {
                        callback.onError(action);
                    }
                };
                if (mScheduleDataQueryCallback == null) {
                    mScheduleDataQueryCallback = queryCallback;
                }
                updateData(queryCallback);
                break;
            case SESSION_SLOT:
                if (args == null || !args.containsKey(SESSION_URL_KEY)) {
                    callback.onError(action);
                } else {
                    String uriStr = args.getString(SESSION_URL_KEY);

                    // TODO: 2016/12/6 add analytic
                    // ANALYTICS EVENT: Select a slot on My Agenda
                    // Contains: URI indicating session ID or time interval of slot
//                    AnalyticsHelper.sendEvent("My Schedule", "selectslot", uriStr);

                    // No need to notify presenter, nothing to do
                }
                break;
            case FEEDBACK:
                if (args == null || !args.containsKey(SESSION_ID_KEY)
                        || !args.containsKey(SESSION_TITLE_KEY)) {
                    callback.onError(action);
                } else {
                    String title = args.getString(SESSION_TITLE_KEY);
                    String id = args.getString(SESSION_ID_KEY);

                    // ANALYTICS EVENT: Click on the "Send Feedback" action from Schedule page.
                    // Contains: The session title.
//                    AnalyticsHelper.sendEvent("My Schedule", "Feedback", title);

                    // No need to notify presenter, nothing to do
                }
                break;
            case REDRAW_UI:
                // We use cached data
                callback.onModelUpdated(this, action);
                break;
            default:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void requestData(MyScheduleQueryEnum query,
                            @NonNull DataQueryCallback callback) {
        checkNotNull(query);
        checkNotNull(callback);
        switch (query) {
            case SCHEDULE:
                mScheduleDataQueryCallback = callback;
                updateData(mScheduleDataQueryCallback);
                break;
            default:
                callback.onError(query);
                break;
        }
    }

    @Override
    public void cleanUp() {
        removeDataObservers();
    }

    /**
     * This updates the data, by calling {@link ScheduleHelper#getScheduleDataAsync
     * (LoadScheduleDataListener, long, long)} for each day. It is protected and not private, to
     * allow us to extend this class and use mock data in UI tests (refer {@code
     * StubMyScheduleModel} in {@code androidTest}).
     */
    @SuppressWarnings("unchecked")
    protected void updateData(final DataQueryCallback callback) {
        for (int i = 0; i < Config.CONFERENCE_DAYS.length; i++) {
            /**
             * The key in {@link #mScheduleData} is 1 for the first day, 2 for the second etc
             */
            final int dayId = i + 1;

            // Immediately use cached data if available
            if (mScheduleData.indexOfKey(dayId) >= 0) {
                if (callback != null) {
                    callback.onModelUpdated(this, MyScheduleQueryEnum.SCHEDULE);
                }
            }

            // Update cached data
            mScheduleHelper.getScheduleDataAsync(new LoadScheduleDataListener() {
                @Override
                public void onDataLoaded(ArrayList<ScheduleItem> scheduleItems) {
                    updateCache(dayId, scheduleItems, callback);
                }
            }, Config.CONFERENCE_DAYS[i][0], Config.CONFERENCE_DAYS[i][1]);

        }
    }

    /**
     * This updates the cached data for the day with id {@code dayId} with {@code scheduleItems}
     * then notifies the {@code callback}.It is protected and not private, to allow us to extend
     * this class and use mock data in UI tests (refer {@code StubMyScheduleModel} in {@code
     * androidTest}).
     */
    @SuppressWarnings("unchecked")
    protected void updateCache(int dayId, ArrayList<ScheduleItem> scheduleItems,
                               DataQueryCallback callback) {
        mScheduleData.put(dayId, scheduleItems);
        if (callback != null) {
            callback.onModelUpdated(MyScheduleModel.this, MyScheduleQueryEnum.SCHEDULE);
        }
    }

    public enum MyScheduleQueryEnum implements QueryEnum {
        SCHEDULE(0, null);

        private int id;

        private String[] projection;

        MyScheduleQueryEnum(int id, String[] projection) {
            this.id = id;
            this.projection = projection;
        }


        @Override
        public int getId() {
            return id;
        }

        @Override
        public String[] getProjection() {
            return projection;
        }
    }

    public enum MyScheduleUserActionEnum implements UserActionEnum {
        RELOAD_DATA(1),
        // Click on a row in the schedule, it opens the session or a list of available sessions
        SESSION_SLOT(2),
        FEEDBACK(3),
        REDRAW_UI(4);

        private int id;

        MyScheduleUserActionEnum(int id) {
            this.id = id;
        }


        @Override
        public int getId() {
            return id;
        }
    }

    public interface LoadScheduleDataListener {
        void onDataLoaded(ArrayList<ScheduleItem> scheduleItems);
    }
}
