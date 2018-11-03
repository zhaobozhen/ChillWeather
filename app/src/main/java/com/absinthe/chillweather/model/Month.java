package com.absinthe.chillweather.model;

import androidx.annotation.DrawableRes;

public class Month implements MonthItem {

    private String mName;
    private int mDrawableId;

    public Month(final String name, @DrawableRes final int drawableId) {
        mName = name;
        mDrawableId = drawableId;
    }

    @Override
    public MonthItemType getType() {
        return MonthItemType.MONTH;
    }

    @Override
    public String getName() {
        return mName;
    }

    public int getDrawableId() {
        return mDrawableId;
    }
}
