package com.yalin.googleio2016.io;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.gson.JsonElement;

import java.util.ArrayList;

/**
 * YaLin
 * 2016/11/28.
 */

public abstract class JSONHandler {

    protected static Context mContext;

    public JSONHandler(Context context) {
        mContext = context;
    }

    public abstract void makeContentProviderOperation(ArrayList<ContentProviderOperation> list);

    public abstract void process(JsonElement element);
}
