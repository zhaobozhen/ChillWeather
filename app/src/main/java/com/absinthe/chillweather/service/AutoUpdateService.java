package com.absinthe.chillweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import com.absinthe.chillweather.WeatherActivity;
import com.absinthe.chillweather.gson.Weather;
import com.absinthe.chillweather.util.HttpUtil;
import com.absinthe.chillweather.util.Utility;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        String refreshFreq =  settings.getString("refresh_freq_drop_down", null);
        int timeInterval;
        assert refreshFreq != null;
        if (refreshFreq.equals("0")) {
            timeInterval = 60 * 60 * 1000 / 2;  //半小时
        } else {
            timeInterval = Integer.valueOf(refreshFreq) * 60 * 60 * 1000;
        }

        long triggerAtTime = SystemClock.elapsedRealtime() + timeInterval;
        Intent i = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherId = pref.getString("weather_id", null);
        String weatherUrl = WeatherActivity.WEATHER_API_URL + weatherId + WeatherActivity.HEWEATHER_KEY;

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                String responseText = response.body().string();
                Weather weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = PreferenceManager
                            .getDefaultSharedPreferences(AutoUpdateService.this).edit();
                    editor.putString("weather", responseText);
                    editor.apply();
                }
                Utility.handleOnGoingNotification(getApplicationContext());
            }
        });
        Log.d("AutoUpdateService", "Service success.");
    }
}
