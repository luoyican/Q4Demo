package com.rfchina.internet.rfvoiceprintrecognitiondemo;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by luoyican on 2017/10/9.
 */

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID +"=59ccacff");
    }
}
