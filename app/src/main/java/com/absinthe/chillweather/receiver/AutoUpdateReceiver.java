package com.absinthe.chillweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.absinthe.chillweather.WeatherActivity;
import com.absinthe.chillweather.gson.Weather;
import com.absinthe.chillweather.util.HttpUtil;
import com.absinthe.chillweather.util.Utility;
import com.absinthe.chillweather.util.WeatherAPI;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String weatherId = pref.getString("weather_id", null);
        String weatherUrl = WeatherAPI.WEATHER_API_URL + weatherId + WeatherAPI.HEWEATHER_KEY;

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseText = Objects.requireNonNull(response.body()).string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(context).edit();
                    editor.putString("weather", responseText);
                    editor.apply();
                    WeatherActivity.isNeedRefresh = true;
                    Utility.handleOnGoingNotification(context);
                }
            }
        });
        Log.d("AutoUpdateReceiver", "Service success.");
    }
}
