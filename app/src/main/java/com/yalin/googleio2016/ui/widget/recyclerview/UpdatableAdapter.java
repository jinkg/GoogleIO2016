package com.yalin.googleio2016.ui.widget.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Abstract base class for {@link RecyclerView.Adapter}s whose data can be updated.
 */
public abstract class UpdatableAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    public abstract void update(@NonNull T updatedData);
}
