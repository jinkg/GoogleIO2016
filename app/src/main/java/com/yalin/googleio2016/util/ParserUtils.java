package com.yalin.googleio2016.util;

import android.text.format.Time;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Various utility methods used by {@link com.yalin.googleio2016.io.JSONHandler}.
 */
public class ParserUtils {

    /**
     * Parse the given string as a RFC 3339 timestamp, returning the value as
     * milliseconds since the epoch.
     */
    public static long parseTime(String timestamp) {
        final Time time = new Time();
        time.parse3339(timestamp);
        return time.toMillis(false);
    }
}
