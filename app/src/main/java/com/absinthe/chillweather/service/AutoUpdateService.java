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
import com.absinthe.chillweather.model.GlobalValues;
import com.absinthe.chillweather.util.HttpUtil;
import com.absinthe.chillweather.util.Utility;
import com.absinthe.chillweather.util.WeatherAPI;

import java.io.IOException;
import java.util.Objects;

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
        Intent i = new Intent();
        intent.setAction(GlobalValues.TIMER_ACTION_REPEATING);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000, triggerAtTime, pi);

        return super.onStartCommand(intent, flags, startId);
    }
}
