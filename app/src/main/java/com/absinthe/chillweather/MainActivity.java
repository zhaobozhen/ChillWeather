package com.absinthe.chillweather;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.absinthe.chillweather.util.InitSharedPreferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InitSharedPreferences.init(this);
        finish();
    }
}
