package com.yalin.googleio2016.io.model;

import com.google.gson.annotations.SerializedName;

/**
 * YaLin
 * 2016/11/29.
 */

public class Tag {
    public String tag;
    public String name;
    public String category;
    public String color;
    @SerializedName("abstract")
    public String _abstract;
    public int order_in_category;
    public String photoUrl;
}
