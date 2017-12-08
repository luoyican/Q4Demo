package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luoyican on 2017/12/1.
 */

public class BluetoothBleForClientAdapter extends BaseAdapter {
    private List<BluetoothBleBean> datas;

    public BluetoothBleForClientAdapter(List<BluetoothBleBean> datas) {
        this.datas = datas;
    }

    public List<BluetoothBleBean> addData(BluetoothBleBean data) {
        if (datas == null) datas = new ArrayList<>();
        datas.add(data);
        return datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public BluetoothBleBean getItem(int position) {
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
            convertView = View.inflate(parent.getContext(), R.layout.item_ble_for_client, null);
            viewHolder = new ViewHolder();
            viewHolder.txtTip = (TextView) convertView.findViewById(R.id.txtTip);
            viewHolder.txtState = (TextView) convertView.findViewById(R.id.txtState);
            viewHolder.txtDeliverMsg = (TextView) convertView.findViewById(R.id.txtDeliverMsg);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothBleBean bean = getItem(position);
        DecimalFormat decimalFormat =new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        viewHolder.txtTip.setText("name:" + bean.getBluetoothDevice().getName() + "   distance:" +
                decimalFormat.format(RssiUtil.getDistance((int) bean.getDistance())) +
                "\nMac address:" + bean.getBluetoothDevice().getAddress());
        viewHolder.txtState.setText(bean.getGattState());
        viewHolder.txtDeliverMsg.setText(bean.getDeliverMsg());
        return convertView;
    }

    class ViewHolder {
        private TextView txtTip, txtState, txtDeliverMsg;
    }

    public static class BluetoothBleBean {
        private BluetoothDevice bluetoothDevice;
        private double distance;
        private ScanRecord scanRecord;
        private String gattState;
        private String deliverMsg;

        public BluetoothBleBean(BluetoothDevice bluetoothDevice, double distance, ScanRecord scanRecord) {
            this.bluetoothDevice = bluetoothDevice;
            this.distance = distance;
            this.scanRecord = scanRecord;//ble
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

        public ScanRecord getScanRecord() {
            return scanRecord;
        }

        public void setScanRecord(ScanRecord scanRecord) {
            this.scanRecord = scanRecord;
        }

        public String getGattState() {
            return gattState;
        }

        public void setGattState(String gattState) {
            this.gattState = gattState;
        }

        public String getDeliverMsg() {
            return deliverMsg;
        }

        public void setDeliverMsg(String deliverMsg) {
            this.deliverMsg = deliverMsg;
        }
    }
}
