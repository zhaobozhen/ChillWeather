package com.absinthe.chillweather.model;

import androidx.annotation.DrawableRes;

public class CityModel implements CityItem {

    private String mName;
    private String mWeatherId;
    private int mDrawableId;

    public CityModel(final String name, final String weatherId, @DrawableRes final int drawableId) {
        mName = name;
        mWeatherId = weatherId;
        mDrawableId = drawableId;
    }

    @Override
    public CityItemType getType() {
        return CityItemType.CITY;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getWeatherId() {
        return mWeatherId;
    }

    @Override
    public int getDrawableId() {
        return mDrawableId;
    }
}
