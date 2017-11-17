package com.rfchina.internet.rfvoiceprintrecognitiondemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by luoyican on 2017/10/12.
 */

public class SharePreferenceUtil {
    public static void save(Context context, String fileName, String values) {
        SharedPreferences mSharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(fileName, values);
        editor.commit();
    }

    public static String getValues(Context context, String fileName) {
        SharedPreferences preferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        return preferences.getString(fileName, "");
    }
}
