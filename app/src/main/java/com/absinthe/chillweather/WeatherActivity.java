package com.absinthe.chillweather;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.absinthe.chillweather.gson.BingPic;
import com.absinthe.chillweather.gson.Suggestion;
import com.absinthe.chillweather.service.AutoUpdateService;
import com.absinthe.chillweather.util.ViewFade;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    public static final String WEATHER_API_URL = "https://free-api.heweather.com/s6/weather?location=";
    public static String HEWEATHER_KEY = "&key=2be849896dec411faff5cdae2dae045a";
    public static String mWeatherId;
    public static Weather weather;
    public static boolean isNeedRefresh = true;
    public static boolean mOnGoingNotification;  //天气常驻通知栏
    public static boolean mRefreshService;  //后台刷新

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
    private TextView comfortText;
    private TextView dressingText;
    private TextView uvText;
    private ImageView bingPicImg;
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置透明状态栏
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_weather);
        //初始化各个控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        degreeText = findViewById(R.id.degree_text);
        feelDegreeText = findViewById(R.id.feel_degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        sunRiseText = findViewById(R.id.sun_rise);
        sunSetText = findViewById(R.id.sun_set);
        windDirectionText = findViewById(R.id.wind_direction);
        windPowerText = findViewById(R.id.wind_power);
        humidityText = findViewById(R.id.humidity);
        comfortText = findViewById(R.id.comfort_text);
        dressingText = findViewById(R.id.dressing_text);
        uvText = findViewById(R.id.uv_text);
        bingPicImg = findViewById(R.id.bing_pic_img);
        drawerLayout = findViewById(R.id.drawer_layout);
        Button navButton = findViewById(R.id.nav_button);

        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        mOnGoingNotification =  settings.getBoolean("on_notification_switch", false);
        mRefreshService = settings.getBoolean("refresh_background_switch", false);

        //如果开启后台刷新则取消每次开启刷新
        if (settings.getBoolean("refresh_background_switch", false)) {
            isNeedRefresh = false;
        }

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        //注册侧滑导航栏
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.city_manage:
                        intent = new Intent(WeatherActivity.this, RecyclerActivity.class);
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
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
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

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });

        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

        if (isNeedRefresh) {
            swipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefresh.setRefreshing(true);
                    requestWeather(mWeatherId);
                }
            });
            swipeRefresh.setRefreshing(false);
            isNeedRefresh = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mWeatherId = prefs.getString("weather_id", null);
        String weatherString = prefs.getString("weather", null);
        weather = Utility.handleWeatherResponse(weatherString);
        assert weather != null;

        if (weatherString != null ) {
            if (mWeatherId != null && (!mWeatherId.equals(weather.basic.cityId))) {
                //无缓存时去服务器查询天气
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            } else {
                //有缓存时直接解析天气数据
                showWeatherInfo(weather);
            }
        } else {
            //无缓存时去服务器查询天气
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
    }

    /**
     * 根据天气ID请求城市天气信息
     */

    public void requestWeather(final String weatherId) {
        String weatherUrl = WEATHER_API_URL + weatherId + HEWEATHER_KEY;
        Log.d("HeWeather", weatherId);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, getString(R.string.failed_to_acquire_weather_info), Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, getString(R.string.failed_to_acquire_weather_info), Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                        Utility.handleOnGoingNotification(getApplicationContext());
                    }
                });
            }
        });
        loadBingPic();
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
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);

            dateText.setText(forecast.date.substring(5, 7) + "月" + forecast.date.substring(8, 10) + "日");
            infoText.setText(forecast.dayCondition);
            maxText.setText(forecast.temperatureMax + "℃");
            minText.setText(forecast.temperatureMin + "℃");

            dateText.setTypeface(typeface);
            maxText.setTypeface(typeface);
            minText.setTypeface(typeface);

            forecastLayout.addView(view);
        }
        sunRiseText.setText(weather.forecastList.get(0).sunrise);
        sunSetText.setText(weather.forecastList.get(0).sunset);
        windDirectionText.setText(weather.now.windDirection);
        windPowerText.setText(weather.now.windPower);
        humidityText.setText(weather.now.humidity);

        sunRiseText.setTypeface(typeface);
        sunSetText.setTypeface(typeface);
        windPowerText.setTypeface(typeface);
        humidityText.setTypeface(typeface);

        String comfort;
        String dressing;
        String ultraviolet;
        for (Suggestion suggestion : weather.suggestionList) {
            switch (suggestion.lifeType) {
                case "comf":
                    comfort = "舒适度：" + suggestion.detail;
                    comfortText.setText(comfort);
                    break;
                case "drsg":
                    dressing = "穿衣指数：" + suggestion.detail;
                    dressingText.setText(dressing);
                    break;
                case "uv":
                    ultraviolet = "紫外线指数：" + suggestion.detail;
                    uvText.setText(ultraviolet);
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

    /**
     * 加载必应每日一图为背景
     */

    private void loadBingPic() {
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(pic).into(bingPicImg);
                    }
                });
            }
        });
    }

    /**
     * 实现“再按一次退出应用”功能
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.nav_view))) {
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