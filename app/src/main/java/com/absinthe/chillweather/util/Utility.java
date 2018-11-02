package com.absinthe.chillweather.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.absinthe.chillweather.MainActivity;
import com.absinthe.chillweather.R;
import com.absinthe.chillweather.WeatherActivity;
import com.absinthe.chillweather.gson.BingPic;
import com.absinthe.chillweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import androidx.core.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Utility {
    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            Log.d("HeWeather",weatherContent);
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将返回的JSON数据解析成BingPic实体类
     */
    public static BingPic handleBingPicResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("images");
            String picContent = jsonArray.getJSONObject(0).toString();
            Log.d("HeWeather",picContent);
            return new Gson().fromJson(picContent, BingPic.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建或更新天气常驻通知栏
     */
    public static void handleOnGoingNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        if (WeatherActivity.mOnGoingNotification) {
            Intent intent = new Intent(context, WeatherActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
            Weather weather = WeatherActivity.weather;
            Notification notification = new NotificationCompat.Builder(context, "weather_channel")
                    .setContentTitle(weather.basic.cityName + " " + weather.now.temperature + " " + weather.now.info)
                    .setContentText("小白兔守护中..")
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_noti_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
                    .setVibrate(new long[]{0})
                    .setOngoing(true)
                    .build();
            manager.notify(1, notification);
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
        }
    }
}
