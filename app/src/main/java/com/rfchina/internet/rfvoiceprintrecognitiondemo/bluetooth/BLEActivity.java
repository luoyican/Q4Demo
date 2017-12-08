package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanRecord;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;
import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by luoyican on 2017/12/6.
 */

public class BLEActivity extends Activity {
    //UUID需要外围设备（蓝牙硬件树莓派）定好告知中央设备（手机）
    private static UUID uuidServer = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    private static UUID uuidCharRead = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private static UUID uuidCharWrite = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private static UUID uuidDescriptor = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private List<BluetoothLeDevice> mBluetoothDevices;
    private HashMap<String, DeviceMirror> connectedDevices;
    private TextView txtResult;
    private Handler mHandler = new Handler();
    private ScanCallback mCallback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_ble_for_client);

        txtResult = (TextView) findViewById(R.id.txtResult);

        initBle();
        scan();
        mBluetoothDevices = new ArrayList<>();
        connectedDevices = new HashMap<>();

        ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initBle() {
        //蓝牙相关配置修改
        ViseBle.config()
                .setScanTimeout(-1)//扫描超时时间，这里设置为永久扫描
                .setConnectTimeout(10 * 1000)//连接超时时间
                .setOperateTimeout(5 * 1000)//设置数据操作超时时间
                .setConnectRetryCount(3)//设置连接失败重试次数
                .setConnectRetryInterval(1000)//设置连接失败重试间隔时间
                .setOperateRetryCount(3)//设置数据操作失败重试次数
                .setOperateRetryInterval(1000)//设置数据操作失败重试间隔时间
                .setMaxConnectCount(100);//设置最大连接设备数量
//蓝牙信息初始化，全局唯一，必须在应用初始化时调用
        ViseBle.getInstance().init(this);
    }

    private void scan() {
        mCallback = new ScanCallback(new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
                BluetoothDevice device = bluetoothLeDevice.getDevice();
//                if (bluetoothLeDevice.getRssi() > -60)
//                    Log.e("eeeee", "device:" + device.getName() + "       " + device.getAddress() + "   rssi  " + bluetoothLeDevice.getRssi());
                ScanRecord scanRecord = parseFromBytes(bluetoothLeDevice.getScanRecord());
                //搜索满足条件的设备，全部添加到准备连接的设备列表（去除重复）
                if (scanRecord != null && scanRecord.getServiceUuids() != null && scanRecord.getServiceUuids().size() > 0) {
                    List<ParcelUuid> uuids = scanRecord.getServiceUuids();
                    if (isContaind(uuids, uuidServer.toString())) {//&& scanRecord.getServiceData(uuid) != null&&!TextUtils.isEmpty(new String(scanRecord.getServiceData(uuid)))&&serverName.equals(new String(scanRecord.getServiceData(uuid)))
                        DeviceMirror deviceMirror = getDeviceMirror(connectedDevices, bluetoothLeDevice);
                        if (deviceMirror != null && bluetoothLeDevice.getRssi() > -60) {
                            Log.e("ddddd", "write " + bluetoothLeDevice.getName());
                            write(deviceMirror);
                        }

                        if ( !isContain(mBluetoothDevices, bluetoothLeDevice)) {
                            Log.e("ddddd", "connect " + bluetoothLeDevice.getName());
                            mBluetoothDevices.add(bluetoothLeDevice);
                            connect(bluetoothLeDevice);
                        }
                    }

                }
            }

            @Override
            public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

            }

            @Override
            public void onScanTimeout() {

            }
        });
        ViseBle.getInstance().startScan(mCallback);
    }

    public void connect(final BluetoothLeDevice bluetoothLeDevice) {
        ViseBle.getInstance().connect(bluetoothLeDevice, new IConnectCallback() {
            @Override
            public void onConnectSuccess(DeviceMirror deviceMirror) {
                Log.e("ddddd", "connect to " + deviceMirror.getBluetoothLeDevice().getName());
                connectedDevices.put(bluetoothLeDevice.getAddress(), deviceMirror);
            }

            @Override
            public void onConnectFailure(BleException exception) {
                Log.e("dddd", "" + exception.toString());
                connectedDevices.remove(bluetoothLeDevice);
                mBluetoothDevices.remove(bluetoothLeDevice);
            }

            @Override
            public void onDisconnect(boolean isActive) {
                Log.e("dddd", "onDisconnect");
                connectedDevices.remove(bluetoothLeDevice);
                mBluetoothDevices.remove(bluetoothLeDevice);
            }
        });
    }

    private void write(DeviceMirror deviceMirror) {
        sendMsg(deviceMirror, "" + deviceMirror.getBluetoothLeDevice().getDevice().getName());
        recivedMsg(deviceMirror);
    }

    private void sendMsg(DeviceMirror deviceMirror, String msg) {
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_WRITE)
                .setServiceUUID(uuidServer)
                .setCharacteristicUUID(uuidCharWrite)
                .setDescriptorUUID(uuidDescriptor)
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.e("ddddd", "sendMsg");
            }

            @Override
            public void onFailure(BleException exception) {
                Log.e("ddddd", "sendMsg  onFailure " + exception.toString());
            }
        }, bluetoothGattChannel);
        deviceMirror.writeData(msg.getBytes());
    }

    private void recivedMsg(DeviceMirror deviceMirror) {
        BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                .setPropertyType(PropertyType.PROPERTY_INDICATE)
                .setServiceUUID(uuidServer)
                .setCharacteristicUUID(uuidCharRead)
                .setDescriptorUUID(uuidDescriptor)
                .builder();
        deviceMirror.bindChannel(new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                Log.e("ddddd", "recivedMsg: " + new String(data));
            }

            @Override
            public void onFailure(BleException exception) {
                Log.e("ddddd", exception.toString());
            }
        }, bluetoothGattChannel);
        deviceMirror.registerNotify(true);

        deviceMirror.setNotifyListener(bluetoothGattChannel.getGattInfoKey(), new IBleCallback() {
            @Override
            public void onSuccess(byte[] data, BluetoothGattChannel bluetoothGattChannel, BluetoothLeDevice bluetoothLeDevice) {
                String msg = new String(data);
                Log.e("eeeee2", "recivedMsg: " + msg);
                setTxtResult(msg.contains("yes"));
            }

            @Override
            public void onFailure(BleException exception) {
                Log.e("eeeee2", exception.toString());
            }
        });
    }

    //字节数组转ScanRecord
    private ScanRecord parseFromBytes(byte[] scanRecord) {
        try {
            Method method = ScanRecord.class.getMethod("parseFromBytes", byte[].class);
            return (ScanRecord) method.invoke(null, scanRecord);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCallback != null)
            ViseBle.getInstance().stopScan(mCallback);
        ViseBle.getInstance().disconnect();
        ViseBle.getInstance().clear();
    }

    private boolean isContain(List<BluetoothLeDevice> lists, BluetoothLeDevice bean) {
        for (BluetoothLeDevice b : lists) {
            if (b.getAddress().equals(bean.getAddress())) {
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

    private DeviceMirror getDeviceMirror(HashMap<String, DeviceMirror> hashMap, BluetoothLeDevice bean) {
        DeviceMirror deviceMirror = null;
        for (String sb : hashMap.keySet()) {
            if (sb.equals(bean.getAddress())) {
                deviceMirror = hashMap.get(sb);
                Log.e("ddddd", "deviceMirror   " + deviceMirror);
                break;
            }
        }
        return deviceMirror;
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

}
