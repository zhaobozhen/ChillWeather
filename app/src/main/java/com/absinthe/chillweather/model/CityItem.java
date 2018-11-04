package com.absinthe.chillweather.model;

public interface CityItem {

    enum CityItemType {
        HEADER, CITY
    }

    CityItemType getType();

    String getName();

    String getWeatherId();

    int getDrawableId();
}
