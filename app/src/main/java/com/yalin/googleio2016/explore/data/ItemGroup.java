package com.yalin.googleio2016.explore.data;

import android.support.annotation.Nullable;

import com.yalin.googleio2016.model.TagMetadata;

import java.util.ArrayList;

/**
 * YaLin
 * 2016/11/30.
 */

public class ItemGroup {
    private String mTitleId;
    private String mTitle;
    private String mId;
    private String mPhotoUrl;
    private ArrayList<SessionData> sessions = new ArrayList<>();

    public void addSessionData(SessionData session) {
        sessions.add(session);
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    public void formatTitle(TagMetadata tagMetadata) {
        if (tagMetadata != null && tagMetadata.getTagById(mTitleId) != null) {
            mTitle = tagMetadata.getTagById(mTitleId).getName();
        }
    }

    public String getTitleId() {
        return mTitleId;
    }

    public void setTitleId(String titleId) {
        mTitleId = titleId;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        mPhotoUrl = photoUrl;
    }

    public ArrayList<SessionData> getSessions() {
        return sessions;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemGroup)) {
            return false;
        }

        final ItemGroup itemGroup = (ItemGroup) o;

        return mId != null ? mId.equals(itemGroup.mId) : itemGroup.mId == null;

    }

    @Override
    public int hashCode() {
        return mId != null ? mId.hashCode() : 0;
    }
}
