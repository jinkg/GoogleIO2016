package com.yalin.googleio2016.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.yalin.googleio2016.io.model.Tag;
import com.yalin.googleio2016.provider.ScheduleContract;
import com.yalin.googleio2016.provider.ScheduleContractHelper;

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
        Uri uri = ScheduleContractHelper.setUriAsCalledFromSyncAdapter(
                ScheduleContract.Tags.CONTENT_URI);

        // since the number of tags is very small, for simplicity we delete them all and reinsert
        list.add(ContentProviderOperation.newDelete(uri).build());
        for (Tag tag : mTags.values()) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
            builder.withValue(ScheduleContract.Tags.TAG_ID, tag.tag);
            builder.withValue(ScheduleContract.Tags.TAG_CATEGORY, tag.category);
            builder.withValue(ScheduleContract.Tags.TAG_NAME, tag.name);
            builder.withValue(ScheduleContract.Tags.TAG_ORDER_IN_CATEGORY, tag.order_in_category);
            builder.withValue(ScheduleContract.Tags.TAG_ABSTRACT, tag._abstract);
            builder.withValue(ScheduleContract.Tags.TAG_COLOR, tag.color == null ? 0
                    : Color.parseColor(tag.color));
            builder.withValue(ScheduleContract.Tags.TAG_PHOTO_URL, tag.photoUrl);
            list.add(builder.build());
        }
    }

    @Override
    public void process(JsonElement element) {
        for (Tag tag : new Gson().fromJson(element, Tag[].class)) {
            mTags.put(tag.tag, tag);
        }
    }

    public HashMap<String, Tag> getTagMap() {
        return mTags;
    }
}
