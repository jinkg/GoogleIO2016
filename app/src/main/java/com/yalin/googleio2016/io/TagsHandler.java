package com.yalin.googleio2016.io;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.gson.JsonElement;
import com.yalin.googleio2016.io.model.Tag;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * YaLin
 * 2016/11/29.
 */

public class TagsHandler extends JSONHandler {
    private HashMap<String, Tag> mTags = new HashMap<>();

    public TagsHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperation(ArrayList<ContentProviderOperation> list) {

    }

    @Override
    public void process(JsonElement element) {

    }

    public HashMap<String, Tag> getTagMap() {
        return mTags;
    }
}
