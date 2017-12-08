package com.xloong.library.bluesocket;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;


import java.io.IOException;
import java.util.List;

/**
 * Service 端等待被连接的线程
 *
 * @author bingbing
 * @date 16/4/6
 */
public class BlueServiceThread extends BlueSocketBaseThread {


    private BluetoothServerSocket mBlueServiceSocket;
    private BluetoothSocket mBlueSocket;

    public BlueServiceThread(Handler handler) {
        super(handler);
    }

    @Override
    public BluetoothSocket getSocket() {
        return mBlueSocket;
    }

    @Override
    public void run() {

        if (!isRunning) return;

        try {
            mBlueServiceSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, UUID_ANDROID_DEVICE);
            sendMessage(BlueSocketStatus.LISTENING);
            //监听连接,等待客户端连接,此处会阻塞线程,如果客户端没有连接服务端,此处一直会等待,知道有设备连接
            while (true) {
                mBlueSocket = mBlueServiceSocket.accept();
                Log.e("dddd","请求连接");
                if (mBlueSocket != null) {
                    sendMessage(BlueSocketStatus.ACCEPTED);
                } else {
                    sendMessage(BlueSocketStatus.DISCONNECTION);
                }
            }

        } catch (IOException e) {
            sendMessage(BlueSocketStatus.DISCONNECTION);
        }
    }

    @Override
    public void cancle() {
        super.cancle();
        try {
            if (mBlueServiceSocket != null)
                mBlueServiceSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (mBlueSocket != null)
                mBlueSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBlueServiceSocket = null;
        mBlueSocket = null;
    }
}
