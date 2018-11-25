package com.absinthe.chillweather.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.absinthe.chillweather.BuildConfig;
import com.absinthe.chillweather.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 导入预置城市数据库
 */

class DBManager {
    private static final String DB_NAME = "cities_data.db"; //保存的数据库文件名
    private static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    private static final String DB_PATH = "/data"
            + Environment.getDataDirectory().getAbsolutePath() + "/"
            + PACKAGE_NAME + "/databases";  //在手机里存放数据库的位置

    private SQLiteDatabase database;
    private Context context;

    DBManager(Context context) {
        this.context = context;
    }

    void openDatabase() {
        this.database = this.openDatabase(DB_PATH + "/" + DB_NAME);
    }

    private SQLiteDatabase openDatabase(String dbFile) {
        try {
            if (!(new File(dbFile).exists())) { //判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
                InputStream is = this.context.getResources().openRawResource(R.raw.cities_data); //欲导入的数据库
                FileOutputStream fos = new FileOutputStream(dbFile);
                int BUFFER_SIZE = 400000;
                byte[] buffer = new byte[BUFFER_SIZE];
                int count;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
            return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
        } catch (FileNotFoundException e) {
            Log.e("Database", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Database", "IO exception");
            e.printStackTrace();
        }
        return null;
    }

    void closeDatabase() {
        this.database.close();
    }
}