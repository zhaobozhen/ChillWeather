package com.absinthe.chillweather.model;

public class CityHeader implements CityItem {

    private String mName;
    private String mWeatherId;
    private int mDrawableId;

    public CityHeader(final String name, final String weatherId, final int drawableId) {
        mName = name;
        mWeatherId = weatherId;
        mDrawableId = drawableId;
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

    @Override
    public CityItemType getType() {
        return CityItemType.HEADER;
    }
}
