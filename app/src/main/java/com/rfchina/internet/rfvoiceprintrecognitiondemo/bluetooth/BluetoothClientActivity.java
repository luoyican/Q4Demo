package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;
import com.xloong.library.bluesocket.BluetoothSocketHelper;
import com.xloong.library.bluesocket.message.StringMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luoyican on 2017/11/16.
 */

public class BluetoothClientActivity extends Activity {
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 1001;
    private final int REQUEST_BLUTETOOTH = 1002;

    private ImageView ivBack;
    private TextView txtId1;
    private TextView txtId2;
    private TextView txtId3;

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothSocketHelper> mHelpers = new ArrayList<>();  //蓝牙设备间传输socket
    private String sendMsg = "";    //传输的信息
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_client);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        txtId1 = (TextView) findViewById(R.id.txtId1);
        txtId2 = (TextView) findViewById(R.id.txtId2);
        txtId3 = (TextView) findViewById(R.id.txtId3);

        init();
    }

    private void init() {

        //运行时权限申请
        requestPermission();
        //允许被发现时间
        enableBeDiscovery();

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txtId1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg = "YES1";
            }
        });
        txtId2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg = "YES2";
            }
        });
        txtId3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg = "NO1";
            }
        });
    }

    private BluetoothSocketHelper initBluetoothSocketHelper() {
        BluetoothSocketHelper mHelper = new BluetoothSocketHelper();
        mHelpers.add(mHelper);
//        mHelper.setBlueSocketListener(new BluetoothSocketHelper.BlueSocketListener() {
//            @Override
//            public void onBlueSocketStatusChange(BlueSocketBaseThread.BlueSocketStatus status, BluetoothDevice remoteDevice) {
//
//            }
//
//            @Override
//            public void onBlueSocketMessageReceiver(IMessage message) {
//                if (message instanceof StringMessage) {
//                    Toast.makeText(BluetoothClientActivity.this, ((StringMessage) message).getContent(), Toast.LENGTH_SHORT).show();
//                } else if (message instanceof ImageMessage) {
//                    Toast.makeText(BluetoothClientActivity.this, ((ImageMessage) message).getContent().getAbsolutePath(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        return mHelper;
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    private void enableBeDiscovery() {
        //开启蓝牙功能
        initBluetoothDevice();

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

    private void searchBluetoothDevice() {
        //启动蓝牙搜索
        bluetoothAdapter.startDiscovery();
    }

    private void initBluetoothDevice() {
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
            startActivityForResult(enableBtIntent, REQUEST_BLUTETOOTH);
        } else {
            searchBluetoothDevice();
        }
    }

    //配对
    private void toPairDevice(BluetoothDevice device) {
        //暂停搜索设备
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                ClsUtils.createBond(device.getClass(), device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                showTip("配对成功,准备进行连接");
                BluetoothSocketHelper mHelper = initBluetoothSocketHelper();
                connetDevice(device, mHelper);
                Log.d("dddd", "已经配对");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //连接
    private void connetDevice(BluetoothDevice device, BluetoothSocketHelper mHelper) {
        if (device != null) {
            mHelper.connect(device);
            readRemoteRssiAndSendMessage(device, mHelper);
        }
    }

    private void readRemoteRssiAndSendMessage(final BluetoothDevice device, final BluetoothSocketHelper mHelper) {
        final BluetoothGatt bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                Log.e("DDDDDDrss", "" + rssi);
                Log.e("DDDDDDrss", "" + RssiUtil.getDistance(rssi));
                if (rssi + 10 > 0) {
                    StringMessage message = new StringMessage();
                    message.setContent(getInfo() + "\n" + sendMsg, "扩展信息");
                    mHelper.write(message);
                }
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                Log.e("DDDDDDrss", "" + bluetoothGatt.readRemoteRssi());
                bluetoothGatt.readRemoteRssi();
                mHandler.postDelayed(this, 500);
            }
        });

    }

    private String getInfo() {
        TelephonyManager mTm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String imei = mTm.getDeviceId();
        String imsi = mTm.getSubscriberId();
        String mtype = android.os.Build.MODEL; // 手机型号

        return "imei:" + imei + "\nimsi:" + imsi + "\nmtype:" + mtype;
    }


    private void initBlutoothReceiver() {
        // 注册广播接收器。接收蓝牙发现
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, intentFilter);
    }

    //广播接收发现的蓝牙设备
    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d("dddd", "开始扫描");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    //信号强度
//                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    String name = device.getName();
                    // 添加到结果
                    Log.d("ddddddd", "设备名:" + device.getName() + " 设备地址:" + device.getAddress());
                    if (!TextUtils.isEmpty(name) && (name.contains("BLUTOOTHTEST"))) {//  |
                        toPairDevice(device);
                    }
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d("dddd", "扫描结束");
//                searchBluetoothDevice();
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mBluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    try {
//                    abortBroadcast();//不弹出配对码框
                        ClsUtils.setPin(mBluetoothDevice.getClass(), mBluetoothDevice, "0000"); // 手机和蓝牙采集器配对
                        Log.d("DDDDD", "配对信息" + mBluetoothDevice.getName());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Log.d("DDDDD", "请求连接错误...");
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        Log.e("DDDDDDt", "取消配对");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.e("DDDDDDt", "配对中");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.e("DDDDDDt", "配对成功");
                        showTip("配对成功,准备进行连接");
                        BluetoothSocketHelper mHelper = initBluetoothSocketHelper();
                        connetDevice(device, mHelper);
                        break;
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    showTip("定位权限没有允许，功能无法使用");
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_BLUTETOOTH:
                if (resultCode == RESULT_OK) {
                    showTip("蓝牙功能已经开启");
                    searchBluetoothDevice();
                } else {
                    showTip("蓝牙打开失败");
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBlutoothReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //为了防止内存溢出应该把注册与注销放到可视周期onResume&onPause更合理
        if (bluetoothReceiver != null)
            unregisterReceiver(bluetoothReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (BluetoothSocketHelper mHelper : mHelpers)
            mHelper.stop();
    }

    private void showTip(String s) {
        Toast.makeText(BluetoothClientActivity.this, s, Toast.LENGTH_SHORT).show();
    }
}
