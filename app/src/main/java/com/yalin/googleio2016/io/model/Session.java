package com.yalin.googleio2016.io.model;

import java.util.Random;

/**
 * YaLin
 * 2016/11/29.
 */

public class Session {
    public String id;
    public String url;
    public String description;
    public String title;
    public String[] tags;
    public String startTimestamp;
    public String youtubeUrl;
    public String[] speakers;
    public String endTimestamp;
    public String hashtag;
    public String subtype;
    public String room;
    public String captionsUrl;
    public String photoUrl;
    public boolean isLivestream;
    public String mainTag;
    public String color;
    public RelatedContent[] relatedContent;
    public int groupingOrder;

    public class RelatedContent {
        public String id;
        public String name;

        @Override
        public String toString() {
            return "RelatedContent{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public String getImportHashCode() {
        return (new Random()).nextLong() + "";
/*        StringBuilder sb = new StringBuilder();
        sb.append("id").append(id == null ? "" : id)
                .append("description").append(description == null ? "" : description)
                .append("title").append(title == null ? "" : title)
                .append("url").append(url == null ? "" : url)
                .append("startTimestamp").append(startTimestamp == null ? "" : startTimestamp)
                .append("endTimestamp").append(endTimestamp == null ? "" : endTimestamp)
                .append("youtubeUrl").append(youtubeUrl == null ? "" : youtubeUrl)
                .append("subtype").append(subtype == null ? "" : subtype)
                .append("room").append(room == null ? "" : room)
                .append("hashtag").append(hashtag == null ? "" : hashtag)
                .append("isLivestream").append(isLivestream ? "true" : "false")
                .append("mainTag").append(mainTag)
                .append("captionsUrl").append(captionsUrl)
                .append("photoUrl").append(photoUrl)
                .append("relatedContent").append(relatedContent)
                .append("color").append(color)
                .append("groupingOrder").append(groupingOrder);
        for (String tag : tags) {
            sb.append("tag").append(tag);
        }
        for (String speaker : speakers) {
            sb.append("speaker").append(speaker);
        }
        return HashUtils.computeWeakHash(sb.toString());
*/
    }

    public String makeTagsList() {
        int i;
        if (tags == null || tags.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(tags[0]);
        for (i = 1; i < tags.length; i++) {
            sb.append(",").append(tags[i]);
        }
        return sb.toString();
    }

    public boolean hasTag(String tag) {
        for (String myTag : tags) {
            if (myTag.equals(tag)) {
                return true;
            }
        }
        return false;
    }
}
