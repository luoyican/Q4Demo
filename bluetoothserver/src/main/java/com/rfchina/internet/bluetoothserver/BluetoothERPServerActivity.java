package com.rfchina.internet.bluetoothserver;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.xloong.library.bluesocket.BlueSocketBaseThread;
import com.xloong.library.bluesocket.BluetoothSocketHelper;
import com.xloong.library.bluesocket.message.IMessage;
import com.xloong.library.bluesocket.message.ImageMessage;
import com.xloong.library.bluesocket.message.StringMessage;

import java.util.UUID;

public class BluetoothERPServerActivity extends Activity {
    private static final UUID UUID_SERVER = UUID.fromString("0000110B-0000-1000-8000-00805F9B34FB");
    private TextView txtResult;
    private String lastMsg = "";
    private Handler mHandler = new Handler();

    @Override

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_for_service);
        txtResult = (TextView) findViewById(R.id.txtResult);

        requestPermission();
        initBluetoothDevice();
        enableBeDiscovery();

        BluetoothSocketHelper mHelper = new BluetoothSocketHelper();
        mHelper.setBlueSocketListener(new BluetoothSocketHelper.BlueSocketListener() {
            @Override
            public void onBlueSocketStatusChange(BlueSocketBaseThread.BlueSocketStatus status, BluetoothDevice remoteDevice) {

            }

            @Override
            public void onBlueSocketMessageReceiver(IMessage message) {
                if (message instanceof StringMessage) {
                    String msg = ((StringMessage) message).getContent();
                    Log.d("DDDDD",msg);
                    setMsg(txtResult, msg.contains("YES"));
                    lastMsg = msg;
//                    Toast.makeText(BluetoothERPServerActivity.this, ((StringMessage) message).getContent(), Toast.LENGTH_SHORT).show();
//                    if (!lastMsg.equals(msg)) {
//
//                    }
                } else if (message instanceof ImageMessage) {
                    Toast.makeText(BluetoothERPServerActivity.this, ((ImageMessage) message).getContent().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mHelper.strat();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    private void initBluetoothDevice() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showTip("此设备不支持蓝牙功能");
            Log.d("dddd", "此设备不支持蓝牙功能");
            return;
        }
        bluetoothAdapter.setName("BLUTOOTHTEST");
//        GATT(bluetoothAdapter);
        if (!bluetoothAdapter.isEnabled()) {//判断蓝牙是不是已经开启
            showTip("请先开启蓝牙功能");
            Log.d("dddd", "请先开启蓝牙功能");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void GATT(BluetoothAdapter bluetoothAdapter){
            AdvertiseSettings settings = new AdvertiseSettings.Builder()
                    .setConnectable(true)
                    .build();

            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .setIncludeDeviceName(true)
                    .setIncludeTxPowerLevel(true)
                    .build();

            AdvertiseData scanResponseData = new AdvertiseData.Builder()
                    .addServiceUuid(new ParcelUuid(UUID_SERVER))
                    .setIncludeTxPowerLevel(true)
                    .build();


            AdvertiseCallback callback = new AdvertiseCallback() {

                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    Log.d("dddd", "BLE advertisement added successfully");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    Log.e("dddd", "Failed to add BLE advertisement, reason: " + errorCode);
                }
            };
        bluetoothAdapter.getBluetoothLeAdvertiser().startAdvertising(settings,advertiseData,scanResponseData,callback);
    }

    private void enableBeDiscovery() {
        // 此方法使自身的蓝牙设备可以被其他蓝牙设备扫描到，
        // 注意时间阈值。0 - 300 秒。
        // 通常设置时间为120秒。
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        // 第二个参数可设置的范围是0~300秒，在此时间区间（窗口期）内可被发现
        // 任何不在此区间的值都将被自动设置成120秒。
        //0永久被扫描
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);

        startActivity(discoverableIntent);
    }

    private void showTip(String s) {
        Toast.makeText(BluetoothERPServerActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void setMsg(final TextView txtResult, final boolean isYes) {
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                Log.e("runOnUiThread", "" + isYes);
                if (isYes) {
                    txtResult.setText("OPEN");
                    txtResult.setBackgroundColor(Color.GREEN);
                } else {
                    txtResult.setText("REFUSE");
                    txtResult.setBackgroundColor(Color.RED);
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtResult.setText("Door Closed");
                        txtResult.setBackgroundColor(Color.WHITE);
                    }
                }, 1500);
            }
        };
        mHandler.removeCallbacksAndMessages(null);
       mHandler.post(runnable);

    }
}

