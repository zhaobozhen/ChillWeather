package com.absinthe.chillweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("fl")
    public String feelTemperature;  //体感温度

    @SerializedName("cond_txt")
    public String info;

    @SerializedName("wind_dir")
    public String windDirection; //风向

    @SerializedName("wind_sc")
    public String windPower;    //风力

    @SerializedName("hum")
    public String humidity; //湿度;
}
