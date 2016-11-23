package com.yalin.googleio2016.navigation;

import android.accounts.Account;
import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * YaLin
 * 2016/11/23.
 */

public class AccountSpinnerAdapter extends ArrayAdapter<Account> {
    public AccountSpinnerAdapter(Context context, int resource) {
        super(context, resource);
    }
}
