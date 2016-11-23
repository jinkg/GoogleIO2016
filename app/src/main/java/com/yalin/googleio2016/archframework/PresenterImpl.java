package com.yalin.googleio2016.archframework;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * YaLin
 * 2016/11/23.
 */

public class PresenterImpl implements Presenter, UpdatableView.UserActionListener {

    public PresenterImpl(Model model, UpdatableView view, UserActionEnum[] validUserActions,
                         QueryEnum[] initialQueries) {
        this(model, new UpdatableView[]{view}, validUserActions, initialQueries);
    }

    public PresenterImpl(Model model, @Nullable UpdatableView[] view, UserActionEnum[] validUserActions,
                         QueryEnum[] initialQueries) {

    }

    @Override
    public void loadInitialQueries() {

    }

    @Override
    public void onUserAction(UserActionEnum action, @Nullable Bundle args) {

    }
}
