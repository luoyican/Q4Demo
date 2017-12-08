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
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by luoyican on 2017/11/22.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothBLEClientActivity extends Activity {
    private ImageView ivBack;
    private TextView txtSearch, txtSendMsg;
    private TextView txtTip;
    private ListView listView;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private Handler mHandler = new Handler();
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic4Write;
//    private BluetoothGattCharacteristic characteristic4Read;

    private BluetoothDevice bluetoothDevice;
    private BluetoothBleResultAdapter mBluetoothBleResultAdapter;
    private List<BluetoothEPRResultAdapter.BluetoothBean> mLists;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_ble_client);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        txtSearch = (TextView) findViewById(R.id.txtSearch);
        txtSendMsg = (TextView) findViewById(R.id.txtSendMsg);
        txtTip = (TextView) findViewById(R.id.txtTip);
        listView = (ListView) findViewById(R.id.listView);

        versionCheck();
        bluetoothIsOpen();
        bleCheck();
        init();
        requestPermission();
    }

    private void init() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBluetoothDevice();
                mLists = new ArrayList<>();
                mBluetoothBleResultAdapter = new BluetoothBleResultAdapter(mLists);
                mBluetoothBleResultAdapter.notifyDataSetChanged();
            }
        });

        txtSendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothGatt == null) {
                    showTip("别点了，先连接指定的蓝牙先吧");
                    return;
                }
                sendMessage(bluetoothGatt, characteristic4Write, "this's cool!");
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectDeviceWithGatt(mBluetoothBleResultAdapter.getItem(position).getBluetoothDevice());
            }
        });

        mLists = new ArrayList<>();
        mBluetoothBleResultAdapter = new BluetoothBleResultAdapter(mLists);
        listView.setAdapter(mBluetoothBleResultAdapter);
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
            showTip("当前版本SDK" + Build.VERSION.SDK_INT + "< Android5.0不支持BLE蓝牙");
            txtTip.setText("当前版本" + Build.VERSION.SDK_INT + "< Android5.0不支持BLE蓝牙");
            return;
        }
    }

    //判断是否支持蓝牙BLE
    private void bleCheck() {
        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showTip("不支持蓝牙BLE");
            txtTip.setText("不支持蓝牙BLE");
            return;
        }
    }

    //判断蓝牙开启
    private void bluetoothIsOpen() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showTip("此设备不支持蓝牙功能");
            Log.d("dddd", "此设备不支持蓝牙功能");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {//判断蓝牙是不是已经开启
            showTip("请先开启蓝牙功能");
            Log.d("dddd", "请先开启蓝牙功能");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }
    }

    //搜索周边蓝牙设备 10s
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void searchBluetoothDevice() {
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSearch();
            }
        }, 10 * 1000);
        bluetoothLeScanner.startScan(mLeScanCallback);//新
    }

    //停止搜索
    private void stopSearch() {
        if (bluetoothLeScanner != null)
            bluetoothLeScanner.stopScan(mLeScanCallback);
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result == null || result.getDevice() == null
                    || TextUtils.isEmpty(result.getDevice().getName())) {
                setMsg(txtTip, "没有搜索到蓝牙设备");
//                return;
            }
            BluetoothDevice device = result.getDevice();
            Log.d("onScanResult", "Device name: " + device.getName());
            Log.d("onScanResult", "Device address: " + device.getAddress());
            Log.d("onScanResult", "Device rssi: " + result.getRssi());
            Log.d("onScanResult", "Device UUIDs: " + device.getUuids());
            BluetoothEPRResultAdapter.BluetoothBean bb = new BluetoothEPRResultAdapter.BluetoothBean(device, result.getRssi(),result.getScanRecord());
            if (!isContain(mLists, bb)) {
                mLists.add(bb);
            }
            mBluetoothBleResultAdapter = new BluetoothBleResultAdapter(mLists);
            listView.setAdapter(mBluetoothBleResultAdapter);
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

    private boolean isContain(List<BluetoothEPRResultAdapter.BluetoothBean> lists, BluetoothEPRResultAdapter.BluetoothBean bean) {
        for (BluetoothEPRResultAdapter.BluetoothBean b : lists) {
            if (b.getBluetoothDevice().getAddress().equals(bean.getBluetoothDevice().getAddress())) {
                return true;
            }
        }
        return false;
    }

    //gatt连接
    private synchronized void connectDeviceWithGatt(BluetoothDevice device) {
//        if (bluetoothGatt == null) {
        Log.e("ddddd", "准备与" + device.getName() + ": " + device.getAddress() + "建立GATT连接");
        bluetoothGatt = BleHelper.connectDeviceWithGATT(device, BluetoothBLEClientActivity.this, true, bluetoothGattCallback);
//        }else {
//            Log.e("ddddd",bluetoothGatt.getDevice().getName() + "  ff");
//        }
    }

    //UUID需要外围设备（蓝牙硬件树莓派）定好告知中央设备（手机）
    private static UUID uuidServer = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static UUID uuidCharRead = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static UUID uuidCharWrite = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private static UUID uuidDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    //初始化bluetoothGattCharacteristic
    private void initCharacteristic(BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt == null)
            return;
        BluetoothGattService service = bluetoothGatt.getService(uuidServer);
//        characteristic4Read = service.getCharacteristic(uuidCharRead);
        if (service != null)
            characteristic4Write = service.getCharacteristic(uuidCharWrite);
//        bluetoothGatt.readCharacteristic(characteristic4Read);
    }

    //发送消息
    private void sendMessage(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, String msgs) {
        initCharacteristic(bluetoothGatt);
        if (bluetoothGatt == null || bluetoothGattCharacteristic == null)
            return;
            bluetoothGattCharacteristic.setValue(msgs);
//            bluetoothGattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);//传不完继续传且不需要对面回应
            bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e("ddddd", "onConnectionStateChange ");
//            if (status == BluetoothGatt.GATT_SUCCESS) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.e("dddddt", "已连接到 " + gatt.getDevice().getName());
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_CONNECTING)
                Log.e("dddddt", "准备连接到 " + gatt.getDevice().getName());
            else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                Log.e("dddddt", "准备断开 " + gatt.getDevice().getName());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED)
                Log.e("dddddt", "与 " + gatt.getDevice().getName() + "断开连接");
//            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            for (BluetoothGattService service : gatt.getServices()) {
                Log.e("ddddd", "service  " + service.getUuid().toString());
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : service.getCharacteristics())
                    Log.e("ddddd", "characteristic  " + bluetoothGattCharacteristic.getUuid().toString());
            }

            Log.e("ddddd", "onServicesDiscovered " + gatt.executeReliableWrite() + "  " + gatt.beginReliableWrite());
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e("ddddd", "onCharacteristicRead " + gatt.executeReliableWrite());
//            Log.e("dddddt", characteristic4Read.getDescriptor(uuidDescriptor).toString());
//            Log.e("dddddt", "as " + characteristic4Read.getDescriptor(uuidDescriptor).getValue());
            Log.e("dddddt", characteristic.getDescriptor(uuidDescriptor).toString());
            Log.e("dddddt", "as " + bluetoothGatt.readDescriptor(characteristic.getDescriptor(uuidDescriptor)));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e("ddddd", "onCharacteristicWrite "+ status);
        }

        /**
         * 收到消息
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e("ddddd", "onCharacteristicChanged");
            Log.e("dddddt", "接收数据 " + characteristic.getValue());
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
        }

        int i = 0;

        @Override
        public void onReliableWriteCompleted(final BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.e("ddddd", "onReliableWriteCompleted" + gatt.readRemoteRssi());
                    if (i > 0) {
                        i--;
                        mHandler.postDelayed(this, 1000);
                    }
                }
            });

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
    };

    private void showTip(String s) {
        Toast.makeText(BluetoothBLEClientActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    private void setMsg(final TextView textView, final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                textView.setText(msg);
            }
        });
    }
}
