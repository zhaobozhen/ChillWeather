package com.absinthe.chillweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.absinthe.chillweather.util.InitSharedPreferences;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //从Shortcuts打开
        Intent intent = getIntent();
        InitSharedPreferences.init(this, intent);
        finish();
    }
}
