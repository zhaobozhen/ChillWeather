package com.absinthe.chillweather.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.absinthe.chillweather.model.CityItem;
import com.absinthe.chillweather.model.CityModel;

public class SharedPrefsStrListUtil {
    /** 数据存储的XML名称 **/
    private final static String SETTING = "SharedPrefsStrList";

    /**
     * 存储数据(Int)
     *
     * @param context
     * @param key
     * @param value
     */
    private static void putIntValue(Context context, String key, int value) {
        Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                .edit();
        sp.putInt(key, value);
        sp.apply();
    }

    /**
     * 存储数据(String)
     *
     * @param context
     * @param key
     * @param value
     */
    private static void putStringValue(Context context, String key, String value) {
        Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                .edit();
        sp.putString(key, value);
        sp.apply();
    }

    /**
     * 存储List<CityItem>
     *
     * @param context
     * @param key
     *            List<CityItem>对应的key
     * @param cityList
     *            对应需要存储的List<CityItem>
     */
    public static void putStrListValue(Context context, String key,
                                       List<CityItem> cityList) {
        if (null == cityList) {
            return;
        }
        // 保存之前先清理已经存在的数据，保证数据的唯一性
        removeStrList(context, key);
        int size = cityList.size();
        putIntValue(context, key + "Size", size);
        for (int i = 0; i < size; i++) {
            putStringValue(context, key + "Name" + (i + 1), cityList.get(i).getName());
            putStringValue(context, key + "WeatherId" + (i + 1), cityList.get(i).getWeatherId());
            putIntValue(context, key + "DrawableId" + (i + 1), cityList.get(i).getDrawableId());
        }
    }

    public static void putStrValueInList(Context context, String key,
                                       String cityName, String weatherId, int drawableId) {
        // 保存之前先清理已经存在的数据，保证数据的唯一性
        if (!isExisted(context, key, cityName)) {
            int size = getIntValue(context, key + "Size", 0);
            putStringValue(context, key + "Name" + (size + 1), cityName);
            putStringValue(context, key + "WeatherId" + (size + 1), weatherId);
            putIntValue(context, key + "DrawableId" + (size + 1), drawableId);
            putIntValue(context, key + "Size", size + 1);
        }
    }

    /**
     * 取出数据（int)
     *
     * @param context
     * @param key
     * @param defValue
     *            默认值
     * @return
     */
    private static int getIntValue(Context context, String key, int defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING,
                Context.MODE_PRIVATE);
        return sp.getInt(key, defValue);
    }

    /**
     * 取出数据（String)
     *
     * @param context
     * @param key
     * @param defValue
     *            默认值
     * @return
     */
    private static String getStringValue(Context context, String key,
                                         String defValue) {
        SharedPreferences sp = context.getSharedPreferences(SETTING,
                Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }

    /**
     * 取出List<String>
     *
     * @param context
     * @param key
     *            List<String> 对应的key
     * @return List<String>
     */
    public static List<CityItem> getStrListValue(Context context, String key) {
        List<CityItem> cityList = new ArrayList<CityItem>();
        int size = getIntValue(context, key + "Size", 0);
        for (int i = 1; i <= size; i++) {
            cityList.add(new CityModel(getStringValue(context, key + "Name" + i, null),
                    getStringValue(context, key + "WeatherId" + i, null),
                    getIntValue(context, key + "DrawableId" + i, 0)));
        }
        return cityList;
    }

    /**
     * 清空List<String>所有数据
     *
     * @param context
     * @param key
     *            List<String>对应的key
     */
    public static void removeStrList(Context context, String key) {
        int size = getIntValue(context, key + "Size", 0);
        if (0 == size) {
            return;
        }
        remove(context, key + "Size");
        for (int i = 0; i < size; i++) {
            remove(context, key + "Name" + i);
            remove(context, key + "WeatherId" + i);
            remove(context, key + "DrawableId" + i);
        }
    }

    /**
     * @Description TODO 清空List<String>单条数据
     * @param context
     * @param key
     *            List<String>对应的key
     * @param str
     *            List<String>中的元素String
     */
    public static void removeStrListItem(Context context, String key, String str) {
        int size = getIntValue(context, key + "Size", 0);
        if (0 == size) {
            return;
        }
        List<CityItem> cityList = getStrListValue(context, key);
        // 待删除的List<String>数据暂存
        List<CityItem> newList = new ArrayList<CityItem>();
        for (int i = 0; i < size; i++) {
            if (!str.equals(cityList.get(i).getName())) {
                newList.add(cityList.get(i));
            }
        }
        // 删除元素重新建立索引写入数据
        clear(context);
        putStrListValue(context, key, newList);
    }

    /**
     * 清空对应key数据
     *
     * @param context
     * @param key
     */
    public static void remove(Context context, String key) {
        Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                .edit();
        sp.remove(key);
        sp.apply();
    }

    /**
     * 清空所有数据
     *
     * @param context
     */
    public static void clear(Context context) {
        Editor sp = context.getSharedPreferences(SETTING, Context.MODE_PRIVATE)
                .edit();
        sp.clear();
        sp.apply();
    }

    public static boolean isExisted(Context context, String key, String name) {
        List<CityItem> cityList = getStrListValue(context, key);
        for (CityItem cityItem : cityList) {
            if (cityItem.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
