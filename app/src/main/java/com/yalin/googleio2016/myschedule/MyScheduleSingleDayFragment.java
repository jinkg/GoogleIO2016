package com.yalin.googleio2016.myschedule;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.UpdatableView;
import com.yalin.googleio2016.myschedule.MyScheduleModel.MyScheduleQueryEnum;
import com.yalin.googleio2016.myschedule.MyScheduleModel.MyScheduleUserActionEnum;

/**
 * YaLin
 * 2016/12/3.
 * <p>
 * This is used by the {@link android.support.v4.view.ViewPager} used by the narrow layout in {@link
 * MyScheduleActivity}. It is a {@link ListFragment} that shows schedule items for a day, using
 * {@link MyScheduleDayAdapter} as its data source.
 */
public class MyScheduleSingleDayFragment extends ListFragment
        implements UpdatableView<MyScheduleModel, MyScheduleQueryEnum, MyScheduleUserActionEnum> {

    private String mContentDescription = null;

    private View mRoot = null;

    /**
     * This is 1 for the first day of the conference, 2 for the second, and so on, and {@link
     * MyScheduleModel#PRE_CONFERENCE_DAY_ID} for the preconference day
     */
    private int mDayId = 1;

    private MyScheduleDayAdapter mViewAdapter;

    private UserActionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_my_schedule_singleday, container, false);
        if (mContentDescription != null) {
            mRoot.setContentDescription(mContentDescription);
        }
        setRetainInstance(true);
        return mRoot;
    }

    public void setContentDescription(String desc) {
        mContentDescription = desc;
        if (mRoot != null) {
            mRoot.setContentDescription(mContentDescription);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResume() {
        super.onResume();
        mListener.onUserAction(MyScheduleUserActionEnum.RELOAD_DATA, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {
        mDayId = getArguments().getInt(MyScheduleActivity.ARG_CONFERENCE_DAY_INDEX, 0);

        TypedArray ids = getResources().obtainTypedArray(R.array.myschedule_listview_ids);
        int listViewId = ids.getResourceId(mDayId, 0);
        getListView().setId(listViewId);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((Listener) getActivity()).onSingleDayFragmentAttached(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ((Listener) getActivity()).onSingleDayFragmentDetached(this);
    }

    @Override
    public void displayData(MyScheduleModel model, MyScheduleQueryEnum query) {
        switch (query) {
            case SCHEDULE:
                updateSchedule(model);
                break;
            default:
                break;
        }
    }

    @Override
    public void displayErrorMessage(MyScheduleQueryEnum query) {
        // Not showing any error
    }

    @Override
    public void displayUserActionResult(MyScheduleModel mode,
                                        MyScheduleUserActionEnum userAction,
                                        boolean success) {
        switch (userAction) {
            case RELOAD_DATA:
                updateSchedule(mode);
                break;
            case SESSION_SLOT:
                break;
            case FEEDBACK:
                break;
            default:
                break;
        }
    }

    @Override
    public Uri getDataUri(MyScheduleQueryEnum query) {
        // Not used by the model
        return null;
    }

    @Override
    public void addListener(UpdatableView.UserActionListener listener) {
        mListener = listener;
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    private void updateSchedule(MyScheduleModel model) {
        if (isVisible()) {
            if (mViewAdapter == null) {
                mViewAdapter = new MyScheduleDayAdapter(getActivity(),
                        ((MyScheduleActivity) getActivity()).getLUtils(), mListener);
            }
            mViewAdapter.updateItems(model.getConferenceDataForDay(mDayId));
            if (getListAdapter() == null) {
                setListAdapter(mViewAdapter);
                getListView().setRecyclerListener(mViewAdapter);
            }
        } else {
            /**
             * Ignore the updated model. The data will be request when the Fragment becomes visible
             * again (in {@link #onResume()}.
             */
        }
    }

    interface Listener {
        void onSingleDayFragmentAttached(MyScheduleSingleDayFragment fragment);

        void onSingleDayFragmentDetached(MyScheduleSingleDayFragment fragment);
    }
}
