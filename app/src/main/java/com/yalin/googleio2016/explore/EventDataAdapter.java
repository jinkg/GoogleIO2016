package com.yalin.googleio2016.explore;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.explore.data.EventCard;
import com.yalin.googleio2016.explore.data.EventData;
import com.yalin.googleio2016.ui.widget.recyclerview.UpdatableAdapter;
import com.yalin.googleio2016.util.ActivityUtils;

import java.util.List;

/**
 * YaLin
 * 2016/12/1.
 * <p>
 * A {@link RecyclerView.Adapter} for a list of {@link EventData} cards.
 */
public class EventDataAdapter
        extends UpdatableAdapter<List<EventCard>, EventDataAdapter.EventCardViewHolder> {

    private final Activity mHost;

    private final LayoutInflater mInflater;

    private final ColorDrawable[] mBackgroundColors;

    private final List<EventCard> mCards;

    public EventDataAdapter(@NonNull Activity activity,
                            @NonNull List<EventCard> eventCards) {
        mHost = activity;
        mInflater = LayoutInflater.from(activity);
        mCards = eventCards;

        // load the background colors
        int[] colors = mHost.getResources().getIntArray(R.array.session_tile_backgrounds);
        mBackgroundColors = new ColorDrawable[colors.length];
        for (int i = 0; i < colors.length; i++) {
            mBackgroundColors[i] = new ColorDrawable(colors[i]);
        }
    }

    @Override
    public void update(@NonNull List<EventCard> updatedData) {
        // No-op for this class; no update-able state
    }

    @Override
    public EventCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EventCardViewHolder(
                mInflater.inflate(R.layout.explore_io_event_data_item_list_tile, parent, false));
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    @Override
    public void onBindViewHolder(EventCardViewHolder holder, int position) {
        final EventCard card = mCards.get(position);
        holder.itemView.setBackground(
                mBackgroundColors[position % mBackgroundColors.length]);
        holder.mCardContent = mCards.get(position);
        holder.mTitleView.setText(card.getDescription());
        holder.mActionNameView.setText(card.getActionString());
    }

    class EventCardViewHolder extends RecyclerView.ViewHolder {

        final TextView mTitleView;
        final TextView mActionNameView;
        EventCard mCardContent;

        public EventCardViewHolder(View itemView) {
            super(itemView);
            mTitleView = (TextView) itemView.findViewById(R.id.title_text);
            mActionNameView = (TextView) itemView.findViewById(R.id.action_text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (mCardContent != null && mCardContent.isValid()) {
                        if (EventCard.ACTION_TYPE_LINK.equalsIgnoreCase(mCardContent.getActionType())) {
                            try {
                                Intent myIntent =
                                        new Intent(Intent.ACTION_VIEW,
                                                Uri.parse(mCardContent.getActionUrl()));
                                mHost.startActivity(myIntent);
                                return;
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(mHost, "Browser not available.", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                        if (EventCard.ACTION_TYPE_MAP.equalsIgnoreCase(mCardContent.getActionType())) {
                            // TODO: 2016/12/1
//                            ActivityUtils.createBackStack(mHost,
//                                    new Intent(mHost, MapActivity.class));
//                            mHost.finish();
                            return;
                        }
                        if (EventCard.ACTION_TYPE_SESSION.equalsIgnoreCase(mCardContent.getActionType())) {
                            // TODO: 2016/12/1
//                            SessionDetailActivity.startSessionDetailActivity(mHost, mCardContent.getActionExtra());
                        }
                    }
                }
            });
        }
    }
}
