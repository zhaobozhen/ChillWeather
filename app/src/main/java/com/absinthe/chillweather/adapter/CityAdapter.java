package com.absinthe.chillweather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.absinthe.chillweather.R;
import com.absinthe.chillweather.model.CityModel;
import com.absinthe.chillweather.model.CityHeader;
import com.absinthe.chillweather.model.CityItem;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureViewHolder;

import androidx.annotation.LayoutRes;

public class CityAdapter extends GestureAdapter<CityItem, GestureViewHolder> {

    private final Context mCtx;
    private final int mItemResId;

    public CityAdapter(final Context ctx, @LayoutRes final int itemResId) {
        mCtx = ctx;
        mItemResId = itemResId;
    }

    @Override
    public GestureViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        if (viewType == CityItem.CityItemType.CITY.ordinal()) {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(mItemResId, parent, false);
            return new CityViewHolder(itemView);
        } else {
            final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_item, parent, false);
            return new HeaderViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final GestureViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        final CityItem cityItem = getItem(position);

        if (cityItem.getType() == CityItem.CityItemType.CITY) {
            final CityViewHolder cityViewHolder = (CityViewHolder) holder;
            final CityModel city = (CityModel) cityItem;
            cityViewHolder.mCityText.setText(city.getName());

            Glide.with(mCtx).load(city.getDrawableId()).apply(RequestOptions.centerCropTransform()).into(cityViewHolder.mCityPicture);
        } else {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            final CityHeader monthHeader = (CityHeader) cityItem;
            headerViewHolder.mHeaderText.setText(monthHeader.getName());
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return getItem(position).getType().ordinal();
    }
}
