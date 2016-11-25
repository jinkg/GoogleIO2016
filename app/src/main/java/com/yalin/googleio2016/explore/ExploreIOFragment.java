package com.yalin.googleio2016.explore;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yalin.googleio2016.R;

/**
 * YaLin
 * 2016/11/25.
 */

public class ExploreIOFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_explore_io, container, false);
        return root;
    }
}
