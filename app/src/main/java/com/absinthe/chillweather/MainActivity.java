package com.absinthe.chillweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.absinthe.chillweather.util.DBManager;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public DBManager dbHelper;
    public static final String PACKAGE_NAME = "com.absinthe.chillweather";
    public static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME + "/databases";  //在手机里存放数据库的位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSharedPreferences();
    }

    private void initSharedPreferences() {
        //获取设置偏好数据

        //获取天气偏好数据
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("weather_id", null) != null) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        } else {
            File dir = new File(DB_PATH);
            dir.mkdir();
            dbHelper = new DBManager(this);
            dbHelper.openDatabase();
            dbHelper.closeDatabase();
            Intent intent = new Intent(MainActivity.this, ChooseAreaActivity.class);
            startActivity(intent);
            finish();
        }
    }
}