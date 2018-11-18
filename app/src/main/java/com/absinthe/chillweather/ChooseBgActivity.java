package com.absinthe.chillweather;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.absinthe.chillweather.util.Glide4Engine;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import java.util.List;

public class ChooseBgActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CHOOSE = 23;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_bg);

        final RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions
                .request(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        // All requested permissions are granted
                        Matisse.from(this)
                                .choose(MimeType.ofImage())
                                .countable(true)
                                .maxSelectable(1)
                                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                                .thumbnailScale(0.85f)
                                .imageEngine(new Glide4Engine())
                                .forResult(REQUEST_CODE_CHOOSE);
                    } else {
                        // At least one permission is denied
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            List<Uri> mSelected;
            mSelected = Matisse.obtainResult(data);
            @SuppressLint("CommitPrefEdits")
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ChooseBgActivity.this).edit();
            editor.putString("custom_bg_uri", mSelected.get(0).toString());
            editor.apply();
            editor = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
            editor.putBoolean("bing_update_switch", false);
            editor.apply();
            WeatherActivity.mOnBingPicSwitch = false;
            WeatherActivity.isNeedRefresh = true;
        }
        finish();
    }
}
