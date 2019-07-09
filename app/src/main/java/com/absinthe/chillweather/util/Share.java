package com.absinthe.chillweather.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import com.absinthe.chillweather.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

public class Share {
    public static void shotShare(Context context, View view){
        //截屏
        String path = screenShot(context, view);
        //分享
        if(!Objects.requireNonNull(path).isEmpty()){
            shareImage(context,path);
        }
    }

    private static String screenShot(Context context, View view){
        String imagePath;
        Bitmap bitmap= loadBitmapFromView(view);
        if(bitmap != null){
            try {
                // 图片文件路径
                imagePath = context.getExternalCacheDir() + "share.png";
                File file = new File(imagePath);
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                return imagePath;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**分享**/
    private static void shareImage(Context context, String imagePath){
        if (imagePath != null){
            Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
            File file = new File(imagePath);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));// 分享的内容
            intent.setType("image/*");// 分享发送的数据类型
            Intent chooser = Intent.createChooser(intent, context.getString(R.string.share_weather_title));
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            if(intent.resolveActivity(context.getPackageManager()) != null){
                context.startActivity(chooser);
            }
        } else {
            Toast.makeText(context, "Srceenshot first.", Toast.LENGTH_LONG).show();
        }
    }

    private static Bitmap loadBitmapFromView(View v) {
        if (v == null) {
            return null;
        }
        Bitmap screenshot;
        screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(screenshot);
        c.translate(-v.getScrollX(), -v.getScrollY());
        v.draw(c);
        return screenshot;
    }
}