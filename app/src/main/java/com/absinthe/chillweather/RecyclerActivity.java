package com.absinthe.chillweather;

import android.os.Bundle;

import com.absinthe.chillweather.fragment.CityManagerFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class RecyclerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler);

        final Fragment fragment = new CityManagerFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.main_placeholder, fragment).commit();
    }
}
