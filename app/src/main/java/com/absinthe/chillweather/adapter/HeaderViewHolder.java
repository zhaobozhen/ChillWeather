package com.absinthe.chillweather.adapter;

import android.view.View;
import android.widget.TextView;

import com.absinthe.chillweather.R;
import com.thesurix.gesturerecycler.GestureViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;


public class HeaderViewHolder extends GestureViewHolder {

    @BindView(R.id.header_text)
    TextView mHeaderText;

    HeaderViewHolder(final View view) {
        super(view);
        ButterKnife.bind(this, view);
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
