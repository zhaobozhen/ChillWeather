package com.absinthe.chillweather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CityManagerActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CityManagerActivity.this, WeatherActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_manager);

    }
}
