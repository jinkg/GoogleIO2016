package com.yalin.googleio2016.util;

import com.yalin.googleio2016.Config;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Utility methods for parsing sessions tags.
 */
public class TagUtils {
    public static boolean isTrackTag(String tagString) {
        return tagString != null && tagString.startsWith(
                Config.Tags.CATEGORY_TRACK + Config.Tags.CATEGORY_SEP);
    }

    public static boolean isThemeTag(String tagString) {
        return tagString != null && tagString.startsWith(
                Config.Tags.CATEGORY_THEME + Config.Tags.CATEGORY_SEP);
    }
}
