package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by luoyican on 2017/11/6.
 */

public class BluetoothResultAdapter extends BaseAdapter {
    private List<BluetoothBean> datas;

    public BluetoothResultAdapter(List<BluetoothBean> datas) {
        Collections.sort(datas, new Comparator<BluetoothBean>() {
            @Override
            public int compare(BluetoothBean o1, BluetoothBean o2) {
                if (o1.distance - o2.distance > 0) return 1;
                else return -1;
            }
        });
        this.datas = datas;
    }

    public void add(BluetoothBean object) {
        if (datas != null) {
            datas.add(object);
        } else {
            datas = new ArrayList<>();
            datas.add(object);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public BluetoothBean getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_bluetooth_adapter, null);
            viewHolder = new ViewHolder();
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
            viewHolder.txtRSSI = (TextView) convertView.findViewById(R.id.txtRSSI);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.txtName.setText("名称：" + (TextUtils.isEmpty(getItem(position).getBluetoothDevice().getName()) ? "未知设备名" : getItem(position).getBluetoothDevice().getName()));
        viewHolder.txtAddress.setText("地址：" + getItem(position).getBluetoothDevice().getAddress());
        DecimalFormat df = new DecimalFormat("#0.00");
        viewHolder.txtRSSI.setText("距离:" + df.format(getItem(position).getDistance()) + "米");

        return convertView;
    }


    private class ViewHolder {
        private TextView txtName;
        private TextView txtAddress;
        private TextView txtRSSI;
    }


    public static class BluetoothBean {
        private BluetoothDevice bluetoothDevice;
        private double distance;

        public BluetoothBean(BluetoothDevice bluetoothDevice, double distance) {
            this.bluetoothDevice = bluetoothDevice;
            this.distance = distance;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public BluetoothDevice getBluetoothDevice() {
            return bluetoothDevice;
        }

        public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
        }
    }

}
