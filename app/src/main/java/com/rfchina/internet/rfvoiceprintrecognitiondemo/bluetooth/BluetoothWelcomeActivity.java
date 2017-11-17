package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;
import com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth.BluetoothResultAdapter.BluetoothBean;
import com.xloong.library.bluesocket.BlueSocketBaseThread;
import com.xloong.library.bluesocket.BluetoothSocketHelper;
import com.xloong.library.bluesocket.message.IMessage;
import com.xloong.library.bluesocket.message.ImageMessage;
import com.xloong.library.bluesocket.message.StringMessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by luoyican on 2017/10/12.
 */

public class BluetoothWelcomeActivity extends Activity {
    private ImageView ivBack;
    private LinearLayout viewSearch;
    private TextView txtMsg;
    private TextView txtSerach;
    private TextView txtTip;
    private ListView mListView;
    private LinearLayout viewResult;
    private TextView txtLocation;
    private TextView txtConnet;
    private TextView txtId1;
    private TextView txtId2;
    private TextView txtId3;
    private TextView txtResult;

    private final int PERMISSION_REQUEST_COARSE_LOCATION = 1001;
    private final int REQUEST_BLUTETOOTH = 1002;
    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothBean> resultListDevices;
    private BluetoothResultAdapter mBluetoothResultAdapter;
    private BluetoothSocket socket;
    private BluetoothDevice connetDevice;//最后配对的设备

    private BluetoothSocketHelper mHelper;
    private String msg = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_welcome);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        viewSearch = (LinearLayout) findViewById(R.id.viewSearch);
        txtMsg = (TextView) findViewById(R.id.txtMsg);
        txtSerach = (TextView) findViewById(R.id.txtSerach);
        txtTip = (TextView) findViewById(R.id.txtTip);
        mListView = (ListView) findViewById(R.id.list_item);
        viewResult = (LinearLayout) findViewById(R.id.viewResult);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        txtConnet = (TextView) findViewById(R.id.txtConnet);
        txtId1 = (TextView) findViewById(R.id.txtId1);
        txtId2 = (TextView) findViewById(R.id.txtId2);
        txtId3 = (TextView) findViewById(R.id.txtId3);
        txtResult = (TextView) findViewById(R.id.txtResult);

        init();
    }

    private void init() {
        viewSearch.setVisibility(View.VISIBLE);
        viewResult.setVisibility(View.GONE);

        resultListDevices = new ArrayList<>();
        mBluetoothResultAdapter = new BluetoothResultAdapter(resultListDevices);
        mListView.setAdapter(mBluetoothResultAdapter);


        mHelper = new BluetoothSocketHelper();
        mHelper.setBlueSocketListener(new BluetoothSocketHelper.BlueSocketListener() {
            @Override
            public void onBlueSocketStatusChange(BlueSocketBaseThread.BlueSocketStatus status, BluetoothDevice remoteDevice) {

            }

            @Override
            public void onBlueSocketMessageReceiver(IMessage message) {
                if (message instanceof StringMessage) {
                    Toast.makeText(BluetoothWelcomeActivity.this, ((StringMessage) message).getContent(), Toast.LENGTH_SHORT).show();
                } else if (message instanceof ImageMessage) {
                    Toast.makeText(BluetoothWelcomeActivity.this, ((ImageMessage) message).getContent().getAbsolutePath(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        requestPermission();
        //允许被发现时间
        enableBeDiscovery();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toPairDevice(mBluetoothResultAdapter.getItem(position).getBluetoothDevice());
            }
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtSerach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBluetoothDevice();
            }
        });

        txtId1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connetDevice != null) {
//                    BluetoothHelper.getBluetoothHelper().sendMessages(connetDevice, "yes");
                    msg = "11111111111111";
                }
            }
        });
        txtId2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connetDevice != null) {
//                    BluetoothHelper.getBluetoothHelper().sendMessages(connetDevice, "yes");
                    msg = "22222222222";
                }
            }
        });
        txtId3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connetDevice != null) {
                    msg = "333333333";
//                    BluetoothHelper.getBluetoothHelper().sendMessages(connetDevice, "no");
                }
            }
        });
    }

    private void refreshUI(final TextView txtMsg, final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtMsg.setText(msg);
            }
        });
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
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

    private void initBluetoothDevice() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showTip("此设备不支持蓝牙功能");
            Log.d("dddd", "此设备不支持蓝牙功能");
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {//判断蓝牙是不是已经开启
            showTip("请先开启蓝牙功能");
            Log.d("dddd", "请先开启蓝牙功能 "+bluetoothAdapter.enable());
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BLUTETOOTH);
        }
    }

    private void searchBluetoothDevice() {
        initBluetoothDevice();
//        removePairDevice();
        resultListDevices = new ArrayList<>();
        mBluetoothResultAdapter = new BluetoothResultAdapter(resultListDevices);
        mListView.setAdapter(mBluetoothResultAdapter);
        //启动蓝牙搜索
        bluetoothAdapter.startDiscovery();
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

    //配对
    private void toPairDevice(BluetoothDevice device) {
//        暂停搜索设备
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
//                showTip("远程设备发送蓝牙配对请求");
                ClsUtils.createBond(device.getClass(), device);
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//                showTip("设备已经连接");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //连接
    private void connetDevice(BluetoothDevice device) {
        mHelper.connect(device);
    }

    private Handler mHandler = new Handler();

    private void print(final BluetoothDevice device) {
        final BluetoothGatt bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
                Log.e("DDDDDDrss", "" + rssi);
                Log.e("DDDDDDrss", "" + RssiUtil.getDistance(rssi));
                if (rssi + 10 > 0) {
                    StringMessage message = new StringMessage();
                    message.setContent("我是Client内容" + msg, "扩展信息");
                    mHelper.write(message);
                }
            }
        });
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                Log.e("DDDDDDrss", "" + bluetoothGatt.readRemoteRssi());
                bluetoothGatt.readRemoteRssi();
                mHandler.postDelayed(this, 4000);
            }
        });

    }

    //得到配对的设备列表，清除已配对的设备
    public void removePairDevice() {
        if (bluetoothAdapter != null) {
            //mBluetoothAdapter初始化方式 mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //这个就是获取已配对蓝牙列表的方法
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter
                    .getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                //这里可以通过device.getName() device.getAddress()来判断是否是自己需要断开的设备
                Log.d("DDDDDDDr", device.getName() + "   " + device.getAddress());
                unpairDevice(device);
            }
        }
    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            ClsUtils.removeBond(device.getClass(), device);
        } catch (Exception e) {
            Log.e("mate", e.getMessage());
        }
    }

    //广播接收发现的蓝牙设备
    private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //获取本地蓝牙名称
            String nameL = bluetoothAdapter.getName();
            //获取本地蓝牙地址
            String addressL = getBluetoothAddress();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                showTip("开始扫描...");
                refreshUI(txtTip, "开始扫描..." + " name:" + nameL + " address:" + addressL);
                Log.d("dddd", "开始扫描");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    //信号强度
                    int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    // 添加到结果
                    Log.d("ddddddd", "设备名:" + device.getName() + " 设备地址:" + device.getAddress());
                    Log.d("ddddddds", RssiUtil.getDistance(rssi) + " m");
                    BluetoothBean bluetoothBean = new BluetoothBean(device, RssiUtil.getDistance(rssi));
                    //方法一
                    if (!resultListDevices.contains(bluetoothBean))
                        resultListDevices.add(bluetoothBean);
                    mBluetoothResultAdapter = new BluetoothResultAdapter(resultListDevices);
                    mListView.setAdapter(mBluetoothResultAdapter);
                    //方法二
//                    mBluetoothResultAdapter.add(bluetoothBean);
//                    mBluetoothResultAdapter.notifyDataSetChanged();
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                showTip("扫描结束.");
                refreshUI(txtTip, "扫描结束" + " name:" + nameL + " address:" + addressL);
                Log.d("dddd", "扫描结束");
            } else if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                Log.e("DDDDDDt", "ACTION_PAIRING_REQUEST");
                BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mBluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                    try {
//                    abortBroadcast();//不弹出配对码框
                        ClsUtils.setPin(mBluetoothDevice.getClass(), mBluetoothDevice, "0000"); // 手机和蓝牙采集器配对
                        // ClsUtils.cancelPairingUserInput(device.getClass(),device); //一般调用不成功，前言里面讲解过了
                        Log.d("DDDDD", "配对信息" + mBluetoothDevice.getName());
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Log.d("DDDDD", "请求连接错误...");
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.e("DDDDDDt", "ACTION_BOND_STATE_CHANGED");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_NONE:
                        Log.e("DDDDDDt", "取消配对");
//                        showTip("配对已取消");
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.e("DDDDDDt", "配对中");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.e("DDDDDDt", "配对成功");
                        showTip("配对成功");
                        refreshUI(txtLocation, "本地蓝牙信息：\n    名字:" + nameL + " 地址：" + addressL);
                        refreshUI(txtConnet, "已配对蓝牙信息：\n    名字:" + device.getName() + " 地址：" + device.getAddress());
                        viewSearch.setVisibility(View.GONE);
                        viewResult.setVisibility(View.VISIBLE);
                        connetDevice = device;
                        connetDevice(device);
                        print(device);
                        break;
                }
            }
        }
    };

    /**
     * 获取本地蓝牙地址
     *
     * @return
     */
    public static String getBluetoothAddress() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Field field = bluetoothAdapter.getClass().getDeclaredField("mService");
            // 参数值为true，禁用访问控制检查
            field.setAccessible(true);
            Object bluetoothManagerService = field.get(bluetoothAdapter);
            if (bluetoothManagerService == null) {
                return null;
            }
            Method method = bluetoothManagerService.getClass().getMethod("getAddress");
            Object address = method.invoke(bluetoothManagerService);
            if (address != null && address instanceof String) {

                return (String) address;
            } else {
                return null;
            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
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
        mHelper.stop();
    }

    private void showTip(String s) {
        Toast.makeText(BluetoothWelcomeActivity.this, s, Toast.LENGTH_SHORT).show();
    }

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
                } else {
                    showTip("蓝牙打开失败");
                }
                break;
        }
    }
}

