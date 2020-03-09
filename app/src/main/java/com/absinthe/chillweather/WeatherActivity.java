package com.absinthe.chillweather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.preference.PreferenceManager;

import com.absinthe.chillweather.databinding.ActivityWeatherBinding;
import com.absinthe.chillweather.gson.BingPic;
import com.absinthe.chillweather.gson.Forecast;
import com.absinthe.chillweather.gson.Suggestion;
import com.absinthe.chillweather.gson.Weather;
import com.absinthe.chillweather.service.AutoUpdateService;
import com.absinthe.chillweather.util.HttpUtil;
import com.absinthe.chillweather.util.Share;
import com.absinthe.chillweather.util.UpdateUtil;
import com.absinthe.chillweather.util.Utility;
import com.absinthe.chillweather.util.WeatherAPI;
import com.absinthe.chillweather.view.ViewFade;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.IOException;
import java.util.Calendar;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.absinthe.chillweather.util.WeatherAPI.HEWEATHER_KEY;
import static com.absinthe.chillweather.util.WeatherAPI.WEATHER_API_URL;

public class WeatherActivity extends AppCompatActivity {

    private ActivityWeatherBinding mBinding;
    public static String mWeatherId;
    public static Weather weather;
    public static boolean isNeedRefresh;
    public static int mUpdateDay;
    public static boolean mOnGoingNotification;  //天气常驻通知栏
    public static boolean mRefreshService;  //后台刷新
    public static boolean mOnBingPicSwitch; //是否开启必应每日一图
    public static boolean mAutoUpdateCheck; //是否开启自动检查更新

    private String TAG = "WeatherActivity";
    private long mExitTime;

    private View navHeaderLayout;
    private ImageView ivNavHeaderPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWeatherBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

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
            mBinding.srlSwipeRefresh.post(() -> {
                mBinding.srlSwipeRefresh.setRefreshing(true);
                mBinding.svWeatherInfo.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            });
            isNeedRefresh = false;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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

        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        mOnGoingNotification = settings.getBoolean("on_notification_switch", false);
        mRefreshService = settings.getBoolean("refresh_background_switch", false);
        mOnBingPicSwitch = settings.getBoolean("bing_update_switch", true);
        mAutoUpdateCheck = settings.getBoolean("auto_update_check", false);

        //loadBackgroundPic();

        mBinding.srlSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        //如果开启后台刷新则取消每次开启刷新
        isNeedRefresh = true;

        mBinding.layoutTitle.btnNavMenu.setOnClickListener(view -> mBinding.dlWeatherDrawer.openDrawer(GravityCompat.START));
        mBinding.layoutTitle.btnShareWeather.setOnClickListener(v -> Share.shotShare(getApplicationContext(), getWindow().getDecorView()));

        //注册侧滑导航栏
        mBinding.nvWeatherNavigation.setNavigationItemSelectedListener(menuItem -> {
            Intent intent;
            switch (menuItem.getItemId()) {
                case R.id.city_manage:
                    intent = new Intent(WeatherActivity.this, RecyclerActivity.class);
                    startActivity(intent);
                    mBinding.dlWeatherDrawer.closeDrawers();
                    break;
                case R.id.customize_bg:
                    intent = new Intent(WeatherActivity.this, ChooseBgActivity.class);
                    startActivity(intent);
                    mBinding.dlWeatherDrawer.closeDrawers();
                    break;
                case R.id.about:
                    intent = new Intent(WeatherActivity.this, AboutActivity.class);
                    startActivity(intent);
                    mBinding.dlWeatherDrawer.closeDrawers();
                    break;
                case R.id.settings:
                    intent = new Intent(WeatherActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    mBinding.dlWeatherDrawer.closeDrawers();
                    break;
                case R.id.check_update:
                    final RxPermissions rxPermissions = new RxPermissions(this);
                    rxPermissions
                            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .subscribe(granted -> {
                                if (granted) {
                                    UpdateUtil.checkUpdate(this, UpdateUtil.SHOW_TOAST);
                                } else {
                                    // Oops permission denied
                                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }
                            });
                    mBinding.dlWeatherDrawer.closeDrawers();
                    break;
            }
            return true;
        });

        navHeaderLayout = mBinding.nvWeatherNavigation.getHeaderView(0);
        ivNavHeaderPic = navHeaderLayout.findViewById(R.id.iv_nav_header);
        Glide.with(WeatherActivity.this)
                .load(R.drawable.bg_nav_header_pic)
                .into(ivNavHeaderPic);

        ivNavHeaderPic.setOnTouchListener((v, event) -> {
            switch (event.getAction()) { //当前状态
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_DOWN:
                    Glide.with(WeatherActivity.this)
                            .load(R.drawable.bg_nav_header_pic_clicked)
                            .into(ivNavHeaderPic);
                    break;
                case MotionEvent.ACTION_UP:
                default:
                    Glide.with(WeatherActivity.this)
                            .load(R.drawable.bg_nav_header_pic)
                            .into(ivNavHeaderPic); //防止长按无法回弹
                    break;
            }
            return true; //返回为true,说明事件已经完成了，不会再被其他事件监听器调用
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
            if (manager != null) {
                manager.createNotificationChannel(mChannel);
            }
        }

        mBinding.srlSwipeRefresh.setOnRefreshListener(() -> requestWeather(mWeatherId));

        if (settings.getBoolean("auto_update_check", false)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (!prefs.getBoolean("isUpdateIgnore", false)) {
                UpdateUtil.checkUpdate(this, UpdateUtil.NOT_SHOW_TOAST);
            }
        }
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
                final String responseText = Objects.requireNonNull(response.body()).string();
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
        mBinding.srlSwipeRefresh.setRefreshing(false);
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
        Typeface typeface = Typeface.createFromAsset(getAssets(), "GoogleSans-Regular.ttf");

        mBinding.layoutTitle.tvTitleCity.setText(cityName);
        mBinding.layoutNow.tvDegree.setText(degree);
        mBinding.layoutNow.tvFeelDegree.setText(feelDegree);
        mBinding.layoutNow.tvWeatherInfo.setText(weatherInfo);

        mBinding.layoutNow.tvDegree.setTypeface(typeface);
        mBinding.layoutNow.tvFeelDegree.setTypeface(typeface);

        mBinding.layoutForecast.llForecast.removeAllViews();

        int iter = 0;
        String[] date = {"今天", "明天", "后天"};

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this)
                    .inflate(R.layout.forecast_item, mBinding.layoutForecast.llForecast, false);
            TextView dateAndConditionText = view.findViewById(R.id.tv_forecast_date_and_info);
            TextView maxMinText = view.findViewById(R.id.tv_max_min_degree);

            dateAndConditionText.setText(date[iter++] + "-" + forecast.dayCondition);
            maxMinText.setText(forecast.temperatureMax + "℃" + " / " + forecast.temperatureMin + "℃");

            Drawable image = getResources().getDrawable(Utility.WeatherIconSelector(forecast.dayCondition, Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
            int h = maxMinText.getLineHeight();
            int w = maxMinText.getLineHeight();

            image.setBounds(0, 0, h, w);
            maxMinText.setCompoundDrawables(null, null, image, null);
            maxMinText.setCompoundDrawablePadding(10);

            dateAndConditionText.setTypeface(typeface);
            maxMinText.setTypeface(typeface);

            mBinding.layoutForecast.llForecast.addView(view);
        }
        mBinding.layoutSun.tvSunRise.setText(weather.forecastList.get(0).sunrise);
        mBinding.layoutSun.tvSunSet.setText(weather.forecastList.get(0).sunset);
        mBinding.layoutWind.tvWindDirection.setText(weather.now.windDirection);
        mBinding.layoutWind.tvWindPower.setText(weather.now.windPower);
        mBinding.layoutWind.tvHumidity.setText(weather.now.humidity + "%");

        mBinding.layoutSun.tvSunRise.setTypeface(typeface);
        mBinding.layoutSun.tvSunSet.setTypeface(typeface);
        mBinding.layoutWind.tvWindPower.setTypeface(typeface);
        mBinding.layoutWind.tvHumidity.setTypeface(typeface);

        mBinding.layoutSun.sunView.setSunrise(Integer.parseInt(weather.forecastList.get(0).sunrise.substring(0, 2)),
                Integer.parseInt(weather.forecastList.get(0).sunrise.substring(3)));
        mBinding.layoutSun.sunView.setSunset(Integer.parseInt(weather.forecastList.get(0).sunset.substring(0, 2)),
                Integer.parseInt(weather.forecastList.get(0).sunrise.substring(3)));
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        // 设置当前时间
        mBinding.layoutSun.sunView.setCurrentTime(hour, minute);

        for (Suggestion suggestion : weather.suggestionList) {
            switch (suggestion.lifeType) {
                case "comf":
                    mBinding.layoutSuggestion.tvComfort.setText(suggestion.detail);
                    break;
                case "drsg":
                    mBinding.layoutSuggestion.tvDressing.setText(suggestion.detail);
                    break;
                case "uv":
                    mBinding.layoutSuggestion.tvUltraviolet.setText(suggestion.detail);
                    break;
                default:
            }
        }
        ViewFade.fadeIn(mBinding.svWeatherInfo, 0F, 1F, 150);
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
            mUpdateDay = Integer.parseInt(updateDate.substring(8, 10));
        }

        boolean isNextDay = mUpdateDay != Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Log.d(TAG, "Today:" + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + ". SubString:" + mUpdateDay);

        if ((customBgUriString != null) && !mOnBingPicSwitch) {
            Uri customBgUri = Uri.parse(customBgUriString);
            Glide.with(this).load(customBgUri).into(mBinding.ivBingPic);
        } else {
            Glide.with(this)
                    .load(bingPic)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(mBinding.ivBingPic);
            if (isNextDay || bingPic == null) {
                loadBingPic();
            }
        }
    }

    /**
     * 加载必应每日一图为背景
     */

    private void loadBingPic() {
        Log.d(TAG, "BingUpdate");
        String requestBingPic = WeatherAPI.requestBingPic;
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                final String content = Objects.requireNonNull(response.body()).string();
                final BingPic bingPic = Utility.handleBingPicResponse(content);
                assert bingPic != null;
                final String pic = "http://cn.bing.com" + bingPic.picUrl;

                Glide.with(WeatherActivity.this)
                        .load(pic)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .preload();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", pic);
                editor.apply();

            }
        });
    }

    /**
     * 实现“再按一次退出应用”功能
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mBinding.dlWeatherDrawer.isDrawerOpen(findViewById(R.id.nv_weather_navigation))) {
                mBinding.dlWeatherDrawer.closeDrawers();
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