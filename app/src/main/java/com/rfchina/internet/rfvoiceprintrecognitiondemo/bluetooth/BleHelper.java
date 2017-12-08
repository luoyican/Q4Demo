package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by luoyican on 2017/11/24.
 */

public class BleHelper {
    /**
     * GATT连接
     * 为了兼容
     * 如果api >= 22，调用带有 tranport参数的。
     * 在我的360手机上，总是连接 connectGatt失败，经测试，调用带有  BluetoothDevice.TRANSPORT_LE才能调用成功
     *
     * @param device BluetoothDevice
     */
    public static BluetoothGatt connectDeviceWithGATT(BluetoothDevice device, Context context, boolean autoConntect, BluetoothGattCallback bluetoothGattCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Little hack with reflect to use the connect gatt with defined transport in Lollipop
            Method connectGattMethod = null;
            try {
                connectGattMethod = device.getClass().getMethod("connectGatt", Context.class, boolean.class, BluetoothGattCallback.class, int.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            try {
                if (connectGattMethod != null) {
                    return (BluetoothGatt) connectGattMethod.invoke(device, context, autoConntect, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return device.connectGatt(context, autoConntect, bluetoothGattCallback);
    }
}
