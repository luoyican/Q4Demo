package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by luoyican on 2017/11/13.
 */

public class BluetoothHelper {
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothHelper bluetoothHelper;
    private static final String OK = "确认--可通行";
    private static final String NO = "拒绝--无权限";
    private Handler mHandler = new Handler();

    private BluetoothHelper() {
    }

    public static BluetoothHelper getBluetoothHelper() {
        if (bluetoothHelper == null) {
            bluetoothHelper = new BluetoothHelper();
        }
        return bluetoothHelper;
    }

    private BluetoothServerSocket serverSocket;

    protected void getMessage(final BluetoothAdapter bluetoothAdapter, final BluetoothDevice bluetoothDevice, final ReceiveListener receiveListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream is = null;
                try {
                    if (serverSocket == null) {
                        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("serverSocket", uuid);
                    }
                    BluetoothSocket accept = null;
                    serverSocket.accept();
                    is = accept.getInputStream();
                    byte[] bytes = new byte[1024];
                    int length = is.read(bytes);
                    String msg = new String(bytes, 0, length);
                    Log.e("dddddt", msg);
                    boolean result = isPermision(msg);
                    if (result == true) {
                        receiveListener.onReceive(OK);
//                        sendMessages(bluetoothDevice, OK);
                    } else {
                        receiveListener.onReceive(NO);
//                        sendMessages(bluetoothDevice, NO);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private BluetoothSocket socket;

    protected void sendMessages(final BluetoothDevice bluetoothDevice, final String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OutputStream os = null;
                try {
                    if (socket == null) {
                        socket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                        socket.connect();
                    }
                    os = socket.getOutputStream();
                    os.write(msg.getBytes());
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean isPermision(String msg) {
        if ("yes".equals(msg)) return true;
        return false;
    }

    public interface ReceiveListener {
        void onReceive(String answer);
    }

}
