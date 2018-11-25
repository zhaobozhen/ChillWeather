package com.absinthe.chillweather.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.json.JSONObject;

public class UpdateUtil {
    public static int handleVersionCodeResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return Integer.valueOf(jsonObject.getString("VersionCode"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String handleVersionNameResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("VersionName");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getVersionCode(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionCode = "";

        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Integer.valueOf(versionCode);
    }

    public static String getVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";

        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }
}