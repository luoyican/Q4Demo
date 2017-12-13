package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;
import com.rfchina.internet.rfvoiceprintrecognitiondemo.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by luoyican on 2017/12/1.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothBLEForClientActivity extends Activity {
    //UUID需要外围设备（蓝牙硬件树莓派）定好告知中央设备（手机）
    private static UUID uuidServer = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static UUID uuidCharRead = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static UUID uuidCharWrite = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private static UUID uuidDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static String serverName = "rf open door server";

    private ImageView ivBack;
    private TextView txtResult;
    private ListView listView;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Handler mHandler = new Handler();
    private BluetoothBleForClientAdapter bluetoothBleForClientAdapter;
    private List<BluetoothGatt> bluetoothGatts;
    private BluetoothGattCharacteristic characteristic4Read;
    private BluetoothGattCharacteristic characteristic4Write;

    private List<BluetoothBleForClientAdapter.BluetoothBleBean> mBluetoothDevices;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_ble_for_client);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        txtResult = (TextView) findViewById(R.id.txtResult);
        listView = (ListView) findViewById(R.id.listView);

        init();
    }

    private void init() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBluetoothDevices = new ArrayList<>();
        bluetoothGatts = new ArrayList<>();
        startScan();

        bluetoothBleForClientAdapter = new BluetoothBleForClientAdapter(mBluetoothDevices);
        listView.setAdapter(bluetoothBleForClientAdapter);
    }

    private void startScan() {
        requestPermission();
        versionCheck();
        bleCheck();
        bluetoothIsOpen();
        searchBluetoothDevice();
    }

    //搜索周边蓝牙设备 30s
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void searchBluetoothDevice() {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                stopSearch();
//            }
//        }, 30 * 1000);
        bluetoothLeScanner.startScan(mLeScanCallback);//新
    }

    //停止搜索
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopSearch() {
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(mLeScanCallback);

        Log.e("eeeee", "end");
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName())) {
//                return;
            }
            BluetoothDevice device = result.getDevice();
            Log.d("onScanResult", "Device name: " + device.getName());
            Log.d("onScanResult", "Device address: " + device.getAddress());
            ScanRecord scanRecord = result.getScanRecord();
            //搜索满足条件的设备，全部添加到准备连接的设备列表（去除重复）
            if (scanRecord != null && scanRecord.getServiceUuids() != null && scanRecord.getServiceUuids().size() > 0) {
                List<ParcelUuid> uuids = scanRecord.getServiceUuids();
                if (isContaind(uuids, uuidServer.toString())) {
                    for (ParcelUuid uuid : uuids) {
                        if (uuidServer.toString().equals(uuid.toString())) {//&& scanRecord.getServiceData(uuid) != null&&!TextUtils.isEmpty(new String(scanRecord.getServiceData(uuid)))&&serverName.equals(new String(scanRecord.getServiceData(uuid)))
                            BluetoothBleForClientAdapter.BluetoothBleBean bb = new BluetoothBleForClientAdapter.BluetoothBleBean(device, result.getRssi(), scanRecord);
                            if (!isContain(mBluetoothDevices, bb)) {
                                Log.e("eeeee", "device:" + device.getName());
                                mBluetoothDevices.add(bb);
                                bluetoothBleForClientAdapter.notifyDataSetChanged();
                                BluetoothGatt bg = gattConnect(bb.getBluetoothDevice());
                                if (bg != null) bluetoothGatts.add(bg);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                Log.d("onBatchScanResults", "Device name: " + result.getDevice().getName());
                Log.d("onBatchScanResults", "Device address: " + result.getDevice().getAddress());
                Log.d("onBatchScanResults", "Device service UUIDs: " + result.getDevice().getUuids());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("onScanFailed", "搜索蓝牙失败 errorCode:" + errorCode);
            if (errorCode == 2) {
                if (bluetoothAdapter != null) {
                    // 一旦发生错误，除了重启蓝牙再没有其它解决办法
                    bluetoothAdapter.disable();
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //要等待蓝牙彻底关闭，然后再打开，才能实现重启效果
                                if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                                    bluetoothAdapter.enable();
                                    break;
                                }
                            }
                        }

                    }).start();
                }
            }
        }
    };

    //建立gatt连接
    private BluetoothGatt gattConnect(BluetoothDevice bluetoothDevice) {
        BluetoothGatt bg = bluetoothDevice.connectGatt(BluetoothBLEForClientActivity.this, false, new MyBluetoothGattCallback(bluetoothDevice));
        Log.e("eeeee", "gattConnect:" + bluetoothDevice.getName());
        return bg;
    }

    //初始化bluetoothGattCharacteristic
    private void initCharacteristic(BluetoothGatt bluetoothGatt) {
        Log.e("eeeee", "initAndSendCharacteristic");
        if (bluetoothGatt == null)
            return;
        BluetoothGattService service = bluetoothGatt.getService(uuidServer);
        if (service != null) {
            characteristic4Read = service.getCharacteristic(uuidCharRead);
            characteristic4Write = service.getCharacteristic(uuidCharWrite);
        }
        if (characteristic4Write == null || characteristic4Read == null)
            return;
        bluetoothGatt.setCharacteristicNotification(characteristic4Read, true);
        BluetoothGattDescriptor descriptor = characteristic4Read.getDescriptor(uuidDescriptor);
        if (descriptor == null) throw new NullPointerException();
        descriptor.setValue("door server".getBytes());
        bluetoothGatt.writeDescriptor(descriptor);
    }

    //发送消息
    private void sendMsg(BluetoothGatt bluetoothGatt, String msg) {
        if (characteristic4Write == null) return;
        characteristic4Write.setValue(msg);
        bluetoothGatt.writeCharacteristic(characteristic4Write);
    }

    //只有明确设置setCharacteristicNotification才能收到通知
    public boolean enableNotification(BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        boolean success = false;
        BluetoothGattService service = gatt.getService(serviceUUID);
        if (service != null) {
            BluetoothGattCharacteristic characteristic = findNotifyCharacteristic(service, characteristicUUID);
            if (characteristic != null) {
                success = gatt.setCharacteristicNotification(characteristic, true);
                if (success) {
                    // 来源：http://stackoverflow.com/questions/38045294/oncharacteristicchanged-not-called-with-ble
                    for (BluetoothGattDescriptor dp : characteristic.getDescriptors()) {
                        if (dp != null) {
                            if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            } else if ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                                dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                            }
                            gatt.writeDescriptor(dp);
                        }
                    }
                }
            }
        }
        return success;
    }

    private BluetoothGattCharacteristic findNotifyCharacteristic(BluetoothGattService service, UUID characteristicUUID) {
        BluetoothGattCharacteristic characteristic = null;
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
                    && characteristicUUID.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        if (characteristic != null)
            return characteristic;
        for (BluetoothGattCharacteristic c : characteristics) {
            if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
                    && characteristicUUID.equals(c.getUuid())) {
                characteristic = c;
                break;
            }
        }
        return characteristic;
    }


    class MyBluetoothGattCallback extends BluetoothGattCallback {
        private int time;
        private BluetoothDevice mBluetoothDevice;

        public MyBluetoothGattCallback(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
            Log.e("ddddd", "onConnectionStateChange ");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("dddddt", "已连接到 " + gatt.getDevice().getName());
                time = 0;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_CONNECTING)
                Log.e("dddddt", "准备连接到 " + gatt.getDevice().getName());
            else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                Log.e("dddddt", "准备断开 " + gatt.getDevice().getName());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("dddddt", "与 " + gatt.getDevice().getName() + "断开连接");
                if (time++ < 5)
                    gattConnect(gatt.getDevice());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            for (BluetoothGattService service : gatt.getServices()) {
                Log.e("ddddd", "service  " + service.getUuid().toString());
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : service.getCharacteristics()) {
                    Log.e("ddddd", "characteristic  " + bluetoothGattCharacteristic.getUuid().toString());
                }
            }
            initCharacteristic(gatt);
            Log.e("ddddd", "onServicesDiscovered " + gatt.executeReliableWrite() + "  " + gatt.beginReliableWrite());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e("ddddd", "onCharacteristicRead " + gatt.executeReliableWrite());
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e("ddddd", "onCharacteristicWrite " + status);
        }

        /**
         * 收到消息
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
            Log.e("ddddd", "onCharacteristicChanged");
            String msg = new String(characteristic.getValue());
            Log.e("dddddt", "接收数据 " + msg);
            setTxtResult(isPermission(msg));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.e("ddddd", "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.e("ddddd", "onDescriptorWrite");
            sendMsg(gatt, "hello world!");
        }

        @Override
        public void onReliableWriteCompleted(final BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.e("ddddd", "onReadRemoteRssi" + rssi);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.e("ddddd", "onMtuChanged");
        }
    }

//    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
//
//    };

    private boolean isPermission(String msg) {
        if (msg.contains("yes")) {
            return true;
        }
        return false;
    }

    private void setTxtResult(final boolean isYes) {
        Runnable runnable = new Runnable() {
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
                }, 3000);
            }
        };
        mHandler.removeCallbacksAndMessages(null);
        mHandler.post(runnable);
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

    //蓝牙BLE需要5.0以后
    private void versionCheck() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            ToastUtil.show("当前版本SDK" + Build.VERSION.SDK_INT + "< Android5.0不支持BLE蓝牙");
            return;
        }
    }

    //判断是否支持蓝牙BLE
    private void bleCheck() {
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.show("不支持蓝牙BLE");
            return;
        }
    }

    //判断蓝牙开启
    private void bluetoothIsOpen() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            ToastUtil.show("此设备不支持蓝牙功能");
            Log.d("dddd", "此设备不支持蓝牙功能");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {//判断蓝牙是不是已经开启
            ToastUtil.show("请先开启蓝牙功能");
            Log.d("dddd", "请先开启蓝牙功能");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
    }

    private boolean isContain(List<BluetoothBleForClientAdapter.BluetoothBleBean> lists, BluetoothBleForClientAdapter.BluetoothBleBean bean) {
        for (BluetoothBleForClientAdapter.BluetoothBleBean b : lists) {
            if (b.getBluetoothDevice().getAddress().equals(bean.getBluetoothDevice().getAddress())) {
                return true;
            }
        }
        return false;
    }

    private <T> boolean isContaind(List<T> lists, String bean) {
        for (T b : lists) {
            if (b.toString().equals(bean)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    searchBluetoothDevice();
                } else {
                    Log.e("eeee", "定位权限没有允许，功能无法使用");
                }
        }
    }

}
