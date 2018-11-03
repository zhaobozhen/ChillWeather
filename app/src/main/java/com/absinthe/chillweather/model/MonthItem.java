package com.absinthe.chillweather.model;

public interface MonthItem {

    enum MonthItemType {
        HEADER, MONTH
    }

    MonthItemType getType();

    String getName();
}
