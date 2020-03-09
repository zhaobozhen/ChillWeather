package com.absinthe.chillweather;

import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.absinthe.chillweather.util.InitSharedPreferences;
import com.absinthe.chillweather.util.Utility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //从Shortcuts打开
        Intent intent = getIntent();
        InitSharedPreferences.init(this, intent);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            if (shortcutManager != null && shortcutManager.getDynamicShortcuts().size() == 0) {
                Utility.setShortcuts(this);
            }
        }
        finish();
    }
}
