package com.yalin.googleio2016.util;

import java.util.Locale;

/**
 * YaLin
 * 2016/11/29.
 */

public class HashUtils {
    public static String computeWeakHash(String string) {
        return String.format(Locale.US, "%08x%08x", string.hashCode(), string.length());
    }
}
