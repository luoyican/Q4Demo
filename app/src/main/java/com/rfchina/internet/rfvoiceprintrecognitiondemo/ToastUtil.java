package com.rfchina.internet.rfvoiceprintrecognitiondemo;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import static com.rfchina.internet.rfvoiceprintrecognitiondemo.MyApplication.getMyContext;

/**
 * Created by luoyican on 2017/12/1.
 */

public class ToastUtil {
    private static Toast toast;

    private static void instanceToast() {
        if (toast == null) {
            toast = Toast.makeText(getMyContext(), "", Toast.LENGTH_LONG);
        }
    }

    public static void show(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) return;
        instanceToast();
        toast.setText(charSequence);
        toast.show();
    }
}
