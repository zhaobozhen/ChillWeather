package com.absinthe.chillweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String cityId;

    public Update update;
    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
