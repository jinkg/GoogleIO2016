package com.yalin.googleio2016.io;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.gson.JsonElement;
import com.yalin.googleio2016.io.map.model.Tile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * YaLin
 * 2016/11/29.
 */

public class MapPropertyHandler extends JSONHandler {

    // maps floor# to tile overlay for that floor
    private HashMap<String, Tile> mTileOverlays = new HashMap<>();

    public MapPropertyHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperation(ArrayList<ContentProviderOperation> list) {

    }

    @Override
    public void process(JsonElement element) {

    }

    public Collection<Tile> getTileOverlays() {
        return mTileOverlays.values();
    }
}
