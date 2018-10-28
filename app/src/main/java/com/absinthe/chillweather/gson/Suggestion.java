package com.absinthe.chillweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("brf")
    public String comfort;  //生活指数

    @SerializedName("txt")
    public String detail;   //详细信息

    @SerializedName("type")
    public String lifeType; //生活指数类型
}