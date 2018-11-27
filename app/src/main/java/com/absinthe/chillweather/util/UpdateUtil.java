package com.absinthe.chillweather.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.absinthe.chillweather.R;

import org.json.JSONObject;

import java.io.IOException;

import androidx.annotation.NonNull;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateUtil {
    public static final int SHOW_TOAST = 0;
    public static final int NOT_SHOW_TOAST = 1;

    private static int handleVersionCodeResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return Integer.valueOf(jsonObject.getString("VersionCode"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String handleVersionNameResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("VersionName");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int getVersionCode(Context context) {
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

    private static String getVersionName(Context context) {
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

    public static void checkUpdate(Activity activity, int flag) {
        String jsonUrl = "https://raw.githubusercontent.com/zhaobozhen/ChillWeather/master/app/src/main/assets/version.json";
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        if (flag == UpdateUtil.SHOW_TOAST) {
            Toast.makeText(activity, "正在检查更新...", Toast.LENGTH_SHORT).show();
        }

        HttpUtil.sendOkHttpRequest(jsonUrl, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                if (flag == UpdateUtil.SHOW_TOAST) {
                    activity.runOnUiThread(() -> Toast.makeText(activity, "检查失败", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                assert response.body() != null;
                final String responseText = response.body().string();
                Log.d("checkUpdate", "responseText:" + responseText);

                int cloudVersionCode = UpdateUtil.handleVersionCodeResponse(responseText);


                activity.runOnUiThread(() -> {
                    if (cloudVersionCode == UpdateUtil.getVersionCode(activity) && flag == UpdateUtil.SHOW_TOAST) {
                        Toast.makeText(activity, "已是最新版本。", Toast.LENGTH_SHORT).show();
                    } else if (cloudVersionCode > UpdateUtil.getVersionCode(activity)) {
                        String versionName = UpdateUtil.handleVersionNameResponse(responseText);
                        String downloadUrl = "https://github.com/zhaobozhen/ChillWeather/releases/download/"
                                + versionName
                                + "/app-release.apk";

                        alertDialogBuilder.setTitle("发现新版本")
                                .setMessage("当前版本:" + UpdateUtil.getVersionName(activity) + ", 最新版本:" + UpdateUtil.handleVersionNameResponse(responseText))
                                .setNegativeButton(R.string.negative_button, (dialog, which) -> {

                                })
                                .setPositiveButton(R.string.positive_button, (dialog, which) -> {
                                    if (versionName != null) {
                                        //创建下载任务,downloadUrl就是下载链接
                                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                                        //指定下载路径和下载文件名
                                        request.setDestinationInExternalPublicDir("/download/", "update.apk");
                                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                        //获取下载管理器
                                        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                                        //将下载任务加入下载队列，否则不会进行下载
                                        downloadManager.enqueue(request);
                                    }
                                });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();//将dialog显示出来
                    } else {
                        if (flag == UpdateUtil.SHOW_TOAST) {
                            Toast.makeText(activity, "似乎哪里出错了？", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}