package com.absinthe.chillweather.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.absinthe.chillweather.BuildConfig;
import com.absinthe.chillweather.RecyclerActivity;
import com.absinthe.chillweather.WeatherActivity;

import java.io.File;

public class InitSharedPreferences {
    private static final String TAG = "InitSharedPreferences";
    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME + "/databases";  //在手机里存放数据库的位置

    public static void init(Context context, Intent i) {
        //获取设置偏好数据
        Log.d(TAG, "Package Path:" + PACKAGE_NAME);

        //从Shortcuts打开
        String intentWeatherId = i.getStringExtra("weather_id");
        if (intentWeatherId != null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString("weather_id", intentWeatherId);
            editor.apply();
        }

        //获取天气偏好数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getString("weather_id", null) == null) {
            File dir = new File(DB_PATH);
            boolean isLoaded = dir.mkdir();
            if (!isLoaded) {
                Toast.makeText(context, "Database loads failed.", Toast.LENGTH_SHORT).show();
            }

            DBManager dbHelper = new DBManager(context);
            dbHelper.openDatabase();
            dbHelper.closeDatabase();

            Intent intent = new Intent(context, RecyclerActivity.class);
            context.startActivity(intent);
            return;
        }
        Intent intent = new Intent(context, WeatherActivity.class);
        context.startActivity(intent);
    }
}
