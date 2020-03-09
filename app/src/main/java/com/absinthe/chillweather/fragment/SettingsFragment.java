package com.absinthe.chillweather.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.absinthe.chillweather.R;
import com.absinthe.chillweather.WeatherActivity;
import com.absinthe.chillweather.service.AutoUpdateService;
import com.absinthe.chillweather.util.UpdateUtil;
import com.absinthe.chillweather.util.Utility;
import com.absinthe.chillweather.util.WeatherAPI;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        Log.d(TAG, "onCreate");

        setPreferencesFromResource(R.xml.settings, null);
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
                WeatherAPI.HEWEATHER_KEY = Objects.requireNonNull(sharedPreferences.getString(key, null));
                break;
            case "refresh_background_switch":
                if (sharedPreferences.getBoolean(key, false)) {
                    Intent intent = new Intent(getContext(), AutoUpdateService.class);
                    Objects.requireNonNull(getActivity()).startService(intent);
                    getPreferenceScreen().findPreference("refresh_freq_drop_down").setEnabled(true);
                    getPreferenceScreen().findPreference("refresh_freq_drop_down").setShouldDisableView(false);
                } else {
                    Intent intent = new Intent(getContext(), AutoUpdateService.class);
                    Objects.requireNonNull(getActivity()).stopService(intent);
                    getPreferenceScreen().findPreference("refresh_freq_drop_down").setEnabled(false);
                    getPreferenceScreen().findPreference("refresh_freq_drop_down").setShouldDisableView(true);
                }
                break;
            case "bing_update_switch":
                WeatherActivity.mOnBingPicSwitch = !WeatherActivity.mOnBingPicSwitch;
                WeatherActivity.isNeedRefresh = true;
                break;
            case "auto_update_check":
                final RxPermissions rxPermissions = new RxPermissions(this);

                rxPermissions
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                // All requested permissions are granted
                                UpdateUtil.checkUpdate(getActivity(), UpdateUtil.NOT_SHOW_TOAST);
                            } else {
                                // At least one permission is denied
                                Toast.makeText(getContext(), getString(R.string.you_must_allow_all_permissions), Toast.LENGTH_SHORT).show();
                            }
                        });
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(TAG, "onPreferenceChange: key = " + preference.getKey() + ", newValue = " + newValue.toString());
        return true;
    }
}