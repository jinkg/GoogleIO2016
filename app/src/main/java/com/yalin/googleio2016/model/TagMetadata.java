package com.yalin.googleio2016.model;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.yalin.googleio2016.archframework.QueryEnum;
import com.yalin.googleio2016.provider.ScheduleContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * YaLin
 * 2016/11/30.
 */

public class TagMetadata {

    // List of tags in each category, sorted by the category sort order.
    private HashMap<String, ArrayList<Tag>> mTagsInCategory = new HashMap<>();

    // Hash map from tag ID to tag.
    private HashMap<String, Tag> mTagsById = new HashMap<>();

    // Hash map from tag name to tag id.
    private HashMap<String, String> mTagsByName = new HashMap<>();

    public static CursorLoader createCursorLoader(Context context) {
        return new CursorLoader(context, ScheduleContract.Tags.CONTENT_URI,
                TagsQueryEnum.TAG.getProjection(), null, null, null);
    }

    protected TagMetadata() {
    }

    public TagMetadata(Cursor cursor) {
        // Not using while(cursor.moveToNext()) because it would lead to issues when writing tests.
        // Either we would mock cursor.moveToNext() to return true and the test would have infinite
        // loop, or we would mock cursor.moveToNext() to return false, and the test would be for an
        // empty cursor.
        int count = cursor.getCount();
        for (int i = 0; i < count; i++) {
            cursor.moveToPosition(i);
            Tag tag = new Tag(cursor.getString(cursor.getColumnIndex(ScheduleContract.Tags.TAG_ID)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.Tags.TAG_NAME)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.Tags.TAG_CATEGORY)),
                    cursor.getInt(
                            cursor.getColumnIndex(ScheduleContract.Tags.TAG_ORDER_IN_CATEGORY)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.Tags.TAG_ABSTRACT)),
                    cursor.getInt(cursor.getColumnIndex(ScheduleContract.Tags.TAG_COLOR)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.Tags.TAG_PHOTO_URL)));
            mTagsById.put(tag.getId(), tag);
            mTagsByName.put(tag.getName(), tag.getId());
            if (!mTagsInCategory.containsKey(tag.getCategory())) {
                mTagsInCategory.put(tag.getCategory(), new ArrayList<Tag>());
            }
            mTagsInCategory.get(tag.getCategory()).add(tag);
        }

        for (ArrayList<Tag> list : mTagsInCategory.values()) {
            Collections.sort(list);
        }
    }


    /**
     * @return the tag with the {@code tagId}, if found.
     */
    public Tag getTagById(String tagId) {
        return mTagsById.containsKey(tagId) ? mTagsById.get(tagId) : null;
    }

    /**
     * @return the tag with the {@code tagName} if found.
     */
    private Tag getTagByName(String tagName) {
        String tagId = mTagsByName.containsKey(tagName) ? mTagsByName.get(tagName) : null;
        return tagId != null ? getTagById(tagId) : null;
    }

    /**
     * @return the tag with the id matching the {@code searchString}, if found; if not found,
     * returns the tag with the name matching the {@code searchString}, if found.
     */
    public Tag getTag(String searchString) {
        Tag tagById = getTagById(searchString);
        if (tagById != null) {
            return tagById;
        } else {
            return getTagByName(searchString);
        }
    }

    public enum TagsQueryEnum implements QueryEnum {
        TAG(0, new String[]{
                BaseColumns._ID,
                ScheduleContract.Tags.TAG_ID,
                ScheduleContract.Tags.TAG_NAME,
                ScheduleContract.Tags.TAG_CATEGORY,
                ScheduleContract.Tags.TAG_ORDER_IN_CATEGORY,
                ScheduleContract.Tags.TAG_ABSTRACT,
                ScheduleContract.Tags.TAG_COLOR,
                ScheduleContract.Tags.TAG_PHOTO_URL
        });

        private int id;

        private String[] projection;

        TagsQueryEnum(int id, String[] projection) {
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

    static public class Tag implements Comparable<Tag> {
        private String mId;
        private String mName;
        private String mCategory;
        private int mOrderInCategory;
        private String mAbstract;
        private int mColor;
        private String mPhotoUrl;

        public Tag(String id, String name, String category, int orderInCategory, String _abstract,
                   int color, String photoUrl) {
            mId = id;
            mName = name;
            mCategory = category;
            mOrderInCategory = orderInCategory;
            mAbstract = _abstract;
            mColor = color;
            mPhotoUrl = photoUrl;
        }

        public String getId() {
            return mId;
        }

        public String getName() {
            return mName;
        }

        public String getCategory() {
            return mCategory;
        }

        public int getOrderInCategory() {
            return mOrderInCategory;
        }

        public String getAbstract() {
            return mAbstract;
        }

        public int getColor() {
            return mColor;
        }

        public String getPhotoUrl() {
            return mPhotoUrl;
        }

        @Override
        public int compareTo(Tag another) {
            return mOrderInCategory - another.mOrderInCategory;
        }

        @Override
        public String toString() {
            return "TagMetadata.Tag: id = " + mId + " name = " + mName;
        }
    }
}
