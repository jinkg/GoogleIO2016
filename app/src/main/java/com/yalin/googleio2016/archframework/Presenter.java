package com.yalin.googleio2016.archframework;

/**
 * YaLin
 * 2016/11/23.
 */

public interface Presenter<Q extends QueryEnum, UA extends UserActionEnum> {
    void loadInitialQueries();
}
