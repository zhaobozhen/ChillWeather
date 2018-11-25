package com.absinthe.chillweather;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.absinthe.chillweather.gson.BingPic;
import com.absinthe.chillweather.gson.Suggestion;
import com.absinthe.chillweather.service.AutoUpdateService;
import com.absinthe.chillweather.view.SunView;
import com.absinthe.chillweather.view.ViewFade;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.absinthe.chillweather.gson.Forecast;
import com.absinthe.chillweather.gson.Weather;
import com.absinthe.chillweather.util.HttpUtil;
import com.absinthe.chillweather.util.Utility;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public static final String WEATHER_API_URL = "https://free-api.heweather.com/s6/weather?location=";
    public static String HEWEATHER_KEY = "&key=2be849896dec411faff5cdae2dae045a";
    public static String mWeatherId;
    public static Weather weather;
    public static boolean isNeedRefresh;
    public static int mUpdateDay;
    public static boolean mOnGoingNotification;  //天气常驻通知栏
    public static boolean mRefreshService;  //后台刷新
    public static boolean mOnBingPicSwitch; //是否开启必应每日一图

    public SwipeRefreshLayout swipeRefresh;
    public DrawerLayout drawerLayout;
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView degreeText;
    private TextView feelDegreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView sunRiseText;
    private TextView sunSetText;
    private TextView windDirectionText;
    private TextView windPowerText;
    private TextView humidityText;
    private SunView sunView;
    private TextView comfortText;
    private TextView dressingText;
    private TextView uvText;
    private ImageView bingPicImg;
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mWeatherId = prefs.getString("weather_id", null);
        if (mWeatherId == null) {
            finish();
        }

        if (isNeedRefresh) {
            loadBackgroundPic();
            swipeRefresh.post(() -> {
                swipeRefresh.setRefreshing(true);
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            });
            isNeedRefresh = false;
        }
    }

    public void initView() {
        //设置透明状态栏
        View decorView = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_weather);
        //初始化各个控件
        weatherLayout = findViewById(R.id.sv_weather_info);
        titleCity = findViewById(R.id.tv_title_city);
        degreeText = findViewById(R.id.tv_degree);
        feelDegreeText = findViewById(R.id.tv_feel_degree);
        weatherInfoText = findViewById(R.id.tv_weather_info);
        forecastLayout = findViewById(R.id.ll_forecast);
        sunRiseText = findViewById(R.id.tv_sun_rise);
        sunSetText = findViewById(R.id.tv_sun_set);
        windDirectionText = findViewById(R.id.tv_wind_direction);
        windPowerText = findViewById(R.id.tv_wind_power);
        humidityText = findViewById(R.id.tv_humidity);
        sunView = findViewById(R.id.sun_view);
        comfortText = findViewById(R.id.tv_comfort);
        dressingText = findViewById(R.id.tv_dressing);
        uvText = findViewById(R.id.tv_ultraviolet);
        bingPicImg = findViewById(R.id.iv_bing_pic);
        drawerLayout = findViewById(R.id.dl_weather_drawer);
        Button navButton = findViewById(R.id.btn_nav_menu);

        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        mOnGoingNotification =  settings.getBoolean("on_notification_switch", false);
        mRefreshService = settings.getBoolean("refresh_background_switch", false);
        mOnBingPicSwitch = settings.getBoolean("bing_update_switch", true);

        loadBackgroundPic();

        swipeRefresh = findViewById(R.id.srl_swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        //如果开启后台刷新则取消每次开启刷新
        isNeedRefresh = true;

        navButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));

        //注册侧滑导航栏
        NavigationView navigationView = findViewById(R.id.nv_weather_navigation);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            Intent intent;
            switch (menuItem.getItemId()) {
                case R.id.city_manage:
                    intent = new Intent(WeatherActivity.this, RecyclerActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    break;
                case R.id.customize_bg:
                    intent = new Intent(WeatherActivity.this, ChooseBgActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    break;
                case R.id.about:
                    intent = new Intent(WeatherActivity.this, AboutActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    break;
                case R.id.settings:
                    intent = new Intent(WeatherActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    break;
                case R.id.check_update:
                    intent = new Intent(WeatherActivity.this, UpdateActivity.class);
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    break;
            }
            return true;
        });

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "weather_channel";
            CharSequence name = "天气通知栏";
            String Description = "For my little honey.";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(false);
            mChannel.setVibrationPattern(new long[]{0});
            mChannel.setSound(null, null);
            manager.createNotificationChannel(mChannel);
        }

        swipeRefresh.setOnRefreshListener(() -> requestWeather(mWeatherId));
    }

    /**
     * 根据天气ID请求城市天气信息
     */

    public void requestWeather(final String weatherId) {
        String weatherUrl = WEATHER_API_URL + weatherId + HEWEATHER_KEY;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(WeatherActivity.this, getString(R.string.failed_to_acquire_weather_info), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(() -> {
                    if (weather != null && "ok".equals(weather.status)) {
                        @SuppressLint("CommitPrefEdits")
                        SharedPreferences.Editor editor = PreferenceManager
                                .getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", responseText);
                        editor.putString("update_date", weather.update.updateTime);
                        editor.apply();
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(WeatherActivity.this, getString(R.string.failed_to_acquire_weather_info), Toast.LENGTH_SHORT).show();
                    }
                    Utility.handleOnGoingNotification(getApplicationContext());
                });
            }
        });
        swipeRefresh.setRefreshing(false);
    }

    /**
     * 处理并展示Weather实体类中的数据
     */

    @SuppressLint("SetTextI18n")
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String degree = weather.now.temperature + "℃";
        String feelDegree = "体感 " + weather.now.feelTemperature + "℃";
        String weatherInfo = weather.now.info;
        Typeface typeface = Typeface.createFromAsset(getAssets(),"GoogleSans-Regular.ttf");

        titleCity.setText(cityName);
        degreeText.setText(degree);
        feelDegreeText.setText(feelDegree);
        weatherInfoText.setText(weatherInfo);

        degreeText.setTypeface(typeface);
        feelDegreeText.setTypeface(typeface);

        forecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.tv_forecast_date);
            TextView infoText = view.findViewById(R.id.tv_forecast_info);
            TextView maxMinText = view.findViewById(R.id.tv_max_min_degree);
            ImageView weatherIcon = view.findViewById(R.id.iv_weather_icon);

            dateText.setText(Integer.valueOf(forecast.date.substring(5, 7)) + "月" + Integer.valueOf(forecast.date.substring(8, 10)) + "日");
            infoText.setText(forecast.dayCondition);
            maxMinText.setText(forecast.temperatureMax + "℃" + " / " + forecast.temperatureMin + "℃");
            weatherIcon.setImageResource(Utility.WeatherIconSelector(forecast.dayCondition, Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

            dateText.setTypeface(typeface);
            maxMinText.setTypeface(typeface);

            forecastLayout.addView(view);
        }
        sunRiseText.setText(weather.forecastList.get(0).sunrise);
        sunSetText.setText(weather.forecastList.get(0).sunset);
        windDirectionText.setText(weather.now.windDirection);
        windPowerText.setText(weather.now.windPower);
        humidityText.setText(weather.now.humidity + "%");

        sunRiseText.setTypeface(typeface);
        sunSetText.setTypeface(typeface);
        windPowerText.setTypeface(typeface);
        humidityText.setTypeface(typeface);

        sunView.setSunrise(Integer.valueOf(weather.forecastList.get(0).sunrise.substring(0, 2)),
                Integer.valueOf(weather.forecastList.get(0).sunrise.substring(3)));
        sunView.setSunset(Integer.valueOf(weather.forecastList.get(0).sunset.substring(0, 2)),
                Integer.valueOf(weather.forecastList.get(0).sunrise.substring(3)));
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // 设置当前时间
        sunView.setCurrentTime(hour, minute);

        for (Suggestion suggestion : weather.suggestionList) {
            switch (suggestion.lifeType) {
                case "comf":
                    comfortText.setText(suggestion.detail);
                    break;
                case "drsg":
                    dressingText.setText(suggestion.detail);
                    break;
                case "uv":
                    uvText.setText(suggestion.detail);
                    break;
                default:
            }
        }
        ViewFade.fadeIn(weatherLayout, 0F, 1F, 150);
        Utility.handleOnGoingNotification(getApplicationContext());

        if (mRefreshService) {
            Intent intent = new Intent(this, AutoUpdateService.class);
            startService(intent);
        }
    }

    private void loadBackgroundPic() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = prefs.getString("bing_pic", null);
        String customBgUriString = prefs.getString("custom_bg_uri", null);
        String updateDate = prefs.getString("update_date", null);

        if (updateDate != null) {
            mUpdateDay = Integer.valueOf(updateDate.substring(8, 10));
        }

        boolean isNextDay = mUpdateDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Log.d("DATE", "Today:"+Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ". SubString:"+mUpdateDay);

        if ((customBgUriString != null) && !mOnBingPicSwitch) {
            Uri customBgUri = Uri.parse(customBgUriString);
            Glide.with(this).load(customBgUri).into(bingPicImg);
        } else {
            if (!isNextDay && bingPic != null) {
                Glide.with(this)
                        .load(bingPic)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(bingPicImg);
            } else {
                loadBingPic();
            }
        }
    }

    /**
     * 加载必应每日一图为背景
     */

    private void loadBingPic() {
        Log.d("loadBingPic", "BingUpdate");
        String requestBingPic = "https://cn.bing.com/HPImageArchive.aspx?format=js&n=1";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                final String content = response.body().string();
                final BingPic bingPic = Utility.handleBingPicResponse(content);
                assert bingPic != null;
                final String pic = "http://cn.bing.com" + bingPic.picUrl;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", pic);
                editor.apply();
                runOnUiThread(() -> Glide.with(WeatherActivity.this)
                        .load(pic)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(bingPicImg));
            }
        });
    }

    /**
     * 实现“再按一次退出应用”功能
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.nv_weather_navigation))) {
                drawerLayout.closeDrawers();
            } else if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, getString(R.string.tap_again_to_quit), Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}