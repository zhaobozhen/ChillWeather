package com.absinthe.chillweather;

import android.content.Context;
import android.view.MenuItem;

import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;

import androidx.annotation.NonNull;

public class AboutLicensesActivity extends MaterialAboutActivity {
    protected int colorIcon = R.color.mal_color_icon_light_theme;

    @NonNull
    @Override
    protected MaterialAboutList getMaterialAboutList(@NonNull final Context c) {
        return AboutActivity.createMaterialAboutLicenseList(c, colorIcon);
    }

    @Override
    protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_licenses);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
