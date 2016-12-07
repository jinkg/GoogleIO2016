package com.yalin.googleio2016.util;

import android.content.ContentProvider;
import android.net.Uri;
import android.text.format.Time;

import java.util.regex.Pattern;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Various utility methods used by {@link com.yalin.googleio2016.io.JSONHandler}.
 */
public class ParserUtils {
    /**
     * Used to sanitize a string to be {@link Uri} safe.
     */
    private static final Pattern sSanitizePattern = Pattern.compile("[^a-z0-9-_]");

    /**
     * Sanitize the given string to be {@link Uri} safe for building
     * {@link ContentProvider} paths.
     */
    public static String sanitizeId(String input) {
        if (input == null) {
            return null;
        }
        return sSanitizePattern.matcher(input.replace("+", "plus").toLowerCase()).replaceAll("");
    }

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
