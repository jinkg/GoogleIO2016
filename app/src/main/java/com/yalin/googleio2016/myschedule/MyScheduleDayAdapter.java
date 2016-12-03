package com.yalin.googleio2016.myschedule;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.archframework.UpdatableView;
import com.yalin.googleio2016.archframework.UpdatableView.UserActionListener;
import com.yalin.googleio2016.model.ScheduleItem;
import com.yalin.googleio2016.myschedule.MyScheduleModel.MyScheduleUserActionEnum;
import com.yalin.googleio2016.util.ImageLoader;
import com.yalin.googleio2016.util.LUtils;
import com.yalin.googleio2016.util.UIUtils;

import java.util.ArrayList;

/**
 * YaLin
 * 2016/12/3.
 * <p>
 * Adapter that produces views to render (one day of) the "My Schedule" screen.
 */
public class MyScheduleDayAdapter implements ListAdapter, AbsListView.RecyclerListener {
    private static final String TAG = "MyScheduleDayAdapter";

    private final Context mContext;
    private final LUtils mLUtils;

    // list of items served by this adapter
    ArrayList<ScheduleItem> mItems = new ArrayList<>();

    // observers to notify about changes in the data
    ArrayList<DataSetObserver> mObservers = new ArrayList<>();

    ImageLoader mImageLoader;

    private final int mHourColorDefault;
    private final int mHourColorPast;
    private final int mTitleColorDefault;
    private final int mTitleColorPast;
    private final int mIconColorDefault;
    private final int mIconColorPast;
    private final int mColorConflict;
    private final int mColorBackgroundDefault;
    private final int mColorBackgroundPast;
    private final int mListSpacing;
    private final int mSelectableItemBackground;
    private final boolean mIsRtl;

    private UserActionListener<MyScheduleUserActionEnum> mListener;

    public MyScheduleDayAdapter(Context context, LUtils lUtils,
                                UserActionListener<MyScheduleUserActionEnum> listener) {
        mContext = context;
        mLUtils = lUtils;
        mListener = listener;
        mHourColorDefault = ContextCompat.getColor(context,
                R.color.my_schedule_hour_header_default);
        mHourColorPast = ContextCompat.getColor(context,
                R.color.my_schedule_hour_header_finished);
        mTitleColorDefault = ContextCompat.getColor(context,
                R.color.my_schedule_session_title_default);
        mTitleColorPast = ContextCompat.getColor(context,
                R.color.my_schedule_session_title_finished);
        mIconColorDefault = ContextCompat.getColor(context,
                R.color.my_schedule_icon_default);
        mIconColorPast = ContextCompat.getColor(context,
                R.color.my_schedule_icon_finished);
        mColorConflict = ContextCompat.getColor(context,
                R.color.my_schedule_conflict);
        mColorBackgroundDefault = ContextCompat.getColor(context, android.R.color.white);
        mColorBackgroundPast = ContextCompat.getColor(context,
                R.color.my_schedule_past_background);
        mListSpacing = context.getResources()
                .getDimensionPixelOffset(R.dimen.element_spacing_normal);
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});
        mSelectableItemBackground = a.getResourceId(0, 0);
        a.recycle();
        mIsRtl = UIUtils.isRtl(context);
    }

    @Override
    public void onMovedToScrapHeap(View view) {

    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (mObservers.contains(observer)) {
            mObservers.remove(observer);
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return position >= 0 && position < mItems.size() ? mItems.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    private static class ViewHolder {
        public TextView startTime;
        public TextView more;
        public ImageView icon;
        public TextView title;
        public TextView description;
        public Button feedback;
        public TextView browse;
        public View live;
        public View separator;
        public View touchArea;
    }
}
