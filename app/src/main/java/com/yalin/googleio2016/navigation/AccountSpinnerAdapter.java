package com.yalin.googleio2016.navigation;

import android.accounts.Account;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.yalin.googleio2016.R;
import com.yalin.googleio2016.ui.widget.BezelImageView;
import com.yalin.googleio2016.util.AccountUtils;
import com.yalin.googleio2016.util.ImageLoader;

/**
 * YaLin
 * 2016/11/23.
 */

public class AccountSpinnerAdapter extends ArrayAdapter<Account> {
    private final ImageLoader mImageLoader;

    public AccountSpinnerAdapter(Context context, int textViewResourceId, Account[] accounts,
                                 ImageLoader imageLoader) {
        super(context, textViewResourceId, accounts);
        mImageLoader = imageLoader;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView =
                    LayoutInflater.from(getContext()).inflate(R.layout.account_spinner, parent, false);
            holder.name = (TextView) convertView.findViewById(R.id.profile_name_text);
            holder.email = (TextView) convertView.findViewById(R.id.profile_email_text);
            convertView.setTag(R.layout.account_spinner, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(R.layout.account_spinner);
        }

        holder.name.setText(AccountUtils.getPlusName(getContext()));
        holder.email.setText(getItem(position).name);

        return convertView;
    }

    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        DropViewHolder holder;

        if (convertView == null) {
            holder = new DropViewHolder();
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.account_spinner_dropdown, parent, false);
            holder.image = (BezelImageView) convertView.findViewById(R.id.profile_image);
            holder.email = (TextView) convertView.findViewById(R.id.profile_email_text);
            convertView.setTag(R.layout.account_spinner_dropdown, holder);
        } else {
            holder = (DropViewHolder) convertView.getTag(R.layout.account_spinner_dropdown);
        }

        String profileImageUrl = AccountUtils.getPlusImageUrl(getContext(), getItem(position).name);
        if (profileImageUrl != null) {
            mImageLoader.loadImage(AccountUtils.getPlusImageUrl(getContext(), getItem(position).name),
                    holder.image);
        } else {
            holder.image.setImageResource(R.drawable.ic_default_avatar);
        }
        String email = getItem(position).name;
        holder.email.setText(email);
        String chosenAccount = AccountUtils.getActiveAccountName(getContext());
        Resources res = getContext().getResources();
        holder.email.setContentDescription(email.equals(chosenAccount) ?
                res.getString(R.string.talkback_selected, email) :
                res.getString(R.string.talkback_not_selected, email));

        return convertView;
    }

    static class ViewHolder {
        TextView name;
        TextView email;
    }

    static class DropViewHolder {
        BezelImageView image;
        TextView email;
    }
}
