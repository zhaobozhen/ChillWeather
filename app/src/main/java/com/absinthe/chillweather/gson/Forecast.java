package com.absinthe.chillweather.gson;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    public String date;
    public String sr;   //日出时间
    public String ss;   //日落时间

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;

    public class Temperature {
        public String max;
        public String min;
    }

    public class More {
        @SerializedName("txt_d")
        public String info;
    }
}
