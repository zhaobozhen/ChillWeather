package com.absinthe.chillweather;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import com.absinthe.chillweather.gson.Weather;
import com.absinthe.chillweather.util.Utility;

import java.util.Objects;

import androidx.core.app.NotificationCompat;
import moe.shizuku.preference.ListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceFragment;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * An example of the usage of {@link PreferenceFragment}.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        Log.d("SettingsFragment", "onCreate");
        getPreferenceManager().setDefaultPackages(new String[]{BuildConfig.APPLICATION_ID + "."});

        getPreferenceManager().setSharedPreferencesName("settings");
        getPreferenceManager().setSharedPreferencesMode(Context.MODE_PRIVATE);

        setPreferencesFromResource(R.xml.settings, null);

        ListPreference listPreference;
        listPreference = (ListPreference) findPreference("day_weather_time_drop_down");
        listPreference.setEntries(new CharSequence[]{"关闭", "6:00", "7:00", "8:00"});
        listPreference.setEntryValues(new CharSequence[]{"0", "6", "7", "8"});
        if (listPreference.getValue() == null) {
            listPreference.setValueIndex(0);
        }
        listPreference.setOnPreferenceChangeListener(this);

        listPreference = (ListPreference) findPreference("night_weather_time_drop_down");
        listPreference.setEntries(new CharSequence[]{"关闭", "19:00", "20:00", "21:00"});
        listPreference.setEntryValues(new CharSequence[]{"0", "19", "20", "21"});
        if (listPreference.getValue() == null) {
            listPreference.setValueIndex(0);
        }
        listPreference.setOnPreferenceChangeListener(this);

        listPreference = (ListPreference) findPreference("refresh_mode_drop_down");
        listPreference.setEntries(new CharSequence[]{"刷新所有城市", "只刷新当前城市"});
        listPreference.setEntryValues(new CharSequence[]{"0", "1"});
        if (listPreference.getValue() == null) {
            listPreference.setValueIndex(0);
        }
        listPreference.setOnPreferenceChangeListener(this);

        listPreference = (ListPreference) findPreference("refresh_freq_drop_down");
        listPreference.setEntries(new CharSequence[]{"30分钟", "1小时", "2小时", "3小时"});
        listPreference.setEntryValues(new CharSequence[]{"0", "1", "2", "3"});
        if (listPreference.getValue() == null) {
            listPreference.setValueIndex(0);
        }
        listPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public DividerDecoration onCreateItemDecoration() {
        return new CategoryDivideDividerDecoration();
        //return new DefaultDividerDecoration();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("SettingsFragment", "onPause");
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("SettingsFragment", "onResume");
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged " + key);
        switch (key) {
            case "on_notification_switch":
                WeatherActivity.mOnGoingNotification = !WeatherActivity.mOnGoingNotification;
                Utility.handleOnGoingNotification(Objects.requireNonNull(getActivity()));
                break;
            case "change_weather_api_edit_text":
                WeatherActivity.HEWEATHER_KEY = Objects.requireNonNull(sharedPreferences.getString(key, null));
                break;
            case "auto_locate_switch":
                ChooseAreaActivity.mAutoLocation = !ChooseAreaActivity.mAutoLocation;
                break;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange: key = "+preference.getKey()+", newValue = "+newValue.toString());
        return true;
    }

}