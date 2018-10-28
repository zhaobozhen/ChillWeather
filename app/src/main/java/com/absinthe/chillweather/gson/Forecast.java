package com.absinthe.chillweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    public String date;

    @SerializedName("tmp_max")
    public String temperatureMax;

    @SerializedName("tmp_min")
    public String temperatureMin;

    @SerializedName("sr")
    public String sunrise;

    @SerializedName("ss")
    public String sunset;

    @SerializedName("cond_txt_d")
    public String dayCondition; //白天天气状况

    @SerializedName("wind_dir")
    public String windDirection; //风向

    @SerializedName("wind_sc")
    public String windPower;    //风力

    @SerializedName("hum")
    public String humidity; //湿度

    @SerializedName("pop")
    public String rainProbability; //降水概率
}
