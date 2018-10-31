package com.absinthe.chillweather;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import moe.shizuku.preference.ListPreference;
import moe.shizuku.preference.Preference;
import moe.shizuku.preference.PreferenceFragment;

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
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        //Log.d(TAG, onPreferenceChange: key = %1$s, newValue = %2$s, preference.getKey(), newValue.toString()));
        return true;
    }

}