package com.rfchina.internet.rfvoiceprintrecognitiondemo;

import android.app.Application;
import android.content.Context;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by luoyican on 2017/10/9.
 */

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID + "=59ccacff");
        context = getApplicationContext();
    }

    public static Context getMyContext() {
        return context;
    }
}
