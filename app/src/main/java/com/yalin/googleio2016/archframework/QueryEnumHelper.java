package com.yalin.googleio2016.archframework;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Provides static methods to help with processing {@link QueryEnum}.
 */
public class QueryEnumHelper {

    /**
     * Converts an integer id to the corresponding {@link QueryEnum}.
     * <p/>
     * Typically, used to convert the loaderId, as provided by the {@link
     * android.app.LoaderManager}.
     *
     * @param id    The id of the query.
     * @param enums The list of possible {@link QueryEnum}.
     * @return the {@link QueryEnum} with the given id or null if none found.
     */
    public static QueryEnum getQueryForId(int id, QueryEnum[] enums) {
        QueryEnum match = null;
        if (enums != null) {
            for (QueryEnum anEnum : enums) {
                if (anEnum != null && id == anEnum.getId()) {
                    match = anEnum;
                }
            }
        }
        return match;
    }
}
