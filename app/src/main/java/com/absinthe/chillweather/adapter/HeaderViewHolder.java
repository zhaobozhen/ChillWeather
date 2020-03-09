package com.absinthe.chillweather.adapter;

import android.view.View;
import android.widget.TextView;

import com.absinthe.chillweather.R;
import com.thesurix.gesturerecycler.GestureViewHolder;

public class HeaderViewHolder extends GestureViewHolder {

    TextView mHeaderText;

    HeaderViewHolder(final View view) {
        super(view);
        mHeaderText = view.findViewById(R.id.tv_header);
    }

    @Override
    public boolean canDrag() {
        return false;
    }

    @Override
    public boolean canSwipe() {
        return false;
    }
}
