package com.absinthe.chillweather.model;

public class MonthHeader implements MonthItem {

    private String mName;

    public MonthHeader(final String name) {
        mName = name;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public MonthItemType getType() {
        return MonthItemType.HEADER;
    }
}
