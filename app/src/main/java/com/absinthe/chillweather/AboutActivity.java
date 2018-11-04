package com.absinthe.chillweather;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.danielstone.materialaboutlibrary.ConvenienceBuilder;
import com.danielstone.materialaboutlibrary.MaterialAboutActivity;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.danielstone.materialaboutlibrary.util.OpenSourceLicense;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class AboutActivity extends MaterialAboutActivity {
    protected int colorIcon = R.color.mal_color_icon_light_theme;

        @Override
        @NonNull
        protected MaterialAboutList getMaterialAboutList(@NonNull final Context context) {
            MaterialAboutCard.Builder cardBuilder = new MaterialAboutCard.Builder();

            cardBuilder.addItem(new MaterialAboutTitleItem.Builder()
                    .text("Chill Weather")
                    .desc("For you.")
                    .icon(R.mipmap.ic_launcher_foreground)
                    .build());

            cardBuilder.addItem(ConvenienceBuilder.createVersionActionItem(context,
                    new IconicsDrawable(context)
                            .icon(CommunityMaterial.Icon2.cmd_information_outline)
                            .color(ContextCompat.getColor(context, colorIcon))
                            .sizeDp(18),
                    "Version",
                    false));

            cardBuilder.addItem(new MaterialAboutActionItem.Builder()
                    .text("Changelog")
                    .icon(new IconicsDrawable(context)
                            .icon(CommunityMaterial.Icon2.cmd_history)
                            .color(ContextCompat.getColor(context, colorIcon))
                            .sizeDp(18))
                    .setOnClickAction(ConvenienceBuilder.createWebViewDialogOnClickAction(context, "Releases", "https://github.com/zhaobozhen/ChillWeather/releases", true, false))
                    .build());

            cardBuilder.addItem(new MaterialAboutActionItem.Builder()
                    .text("Licenses")
                    .icon(new IconicsDrawable(context)
                            .icon(CommunityMaterial.Icon.cmd_book)
                            .color(ContextCompat.getColor(context, colorIcon))
                            .sizeDp(18))
                    .setOnClickAction(new MaterialAboutItemOnClickAction() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(getApplicationContext(), AboutLicensesActivity.class);
                            startActivity(intent);
                        }
                    })
                    .build());

            MaterialAboutCard.Builder authorCardBuilder = new MaterialAboutCard.Builder();
            authorCardBuilder.title("作者");

            authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                    .text("小白兔")
                    .subText("To 小花猫")
                    .icon(new IconicsDrawable(context)
                            .icon(CommunityMaterial.Icon.cmd_account)
                            .color(ContextCompat.getColor(context, colorIcon))
                            .sizeDp(18))
                    .build());

            authorCardBuilder.addItem(new MaterialAboutActionItem.Builder()
                    .text("Fork on GitHub")
                    .icon(new IconicsDrawable(context)
                            .icon(CommunityMaterial.Icon.cmd_github_circle)
                            .color(ContextCompat.getColor(context, colorIcon))
                            .sizeDp(18))
                    .setOnClickAction(ConvenienceBuilder.createWebsiteOnClickAction(context, Uri.parse("https://github.com/zhaobozhen")))
                    .build());

            return new MaterialAboutList(cardBuilder.build(),
                    authorCardBuilder.build());
    }

        @Override
        protected CharSequence getActivityTitle() {
        return getString(R.string.mal_title_about);
    }

    public static MaterialAboutList createMaterialAboutLicenseList(final Context context, int colorIcon) {
        MaterialAboutCard materialAboutLibraryLicenseCard1 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "Android-Iconics", "2018", "Mike Penz",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard2 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "material-about-library", "2016", "Daniel Stone",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard3 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "SearchDialog", "2016", "wenwenwen888",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard4 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "Glide", "2014", "Google, Inc",
                OpenSourceLicense.BSD);

        MaterialAboutCard materialAboutLibraryLicenseCard5 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "Gson", "2008", "Google, Inc",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard6 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "OkHttp", "2018", "square",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard7 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "LitePal", "2018", "Tony Green",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard8 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "MaterialPreference", "2018", "Rikka",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard9 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "Gesture Recycler", "2018", "thesurix",
                OpenSourceLicense.APACHE_2);

        MaterialAboutCard materialAboutLibraryLicenseCard10 = ConvenienceBuilder.createLicenseCard(context,
                new IconicsDrawable(context)
                        .icon(CommunityMaterial.Icon.cmd_book)
                        .color(ContextCompat.getColor(context, colorIcon))
                        .sizeDp(18),
                "Butter Knife", "2013", "Jake Wharton",
                OpenSourceLicense.APACHE_2);

        return new MaterialAboutList(materialAboutLibraryLicenseCard1,
                materialAboutLibraryLicenseCard2,
                materialAboutLibraryLicenseCard3,
                materialAboutLibraryLicenseCard4,
                materialAboutLibraryLicenseCard5,
                materialAboutLibraryLicenseCard6,
                materialAboutLibraryLicenseCard7,
                materialAboutLibraryLicenseCard8,
                materialAboutLibraryLicenseCard9,
                materialAboutLibraryLicenseCard10);
    }
}
