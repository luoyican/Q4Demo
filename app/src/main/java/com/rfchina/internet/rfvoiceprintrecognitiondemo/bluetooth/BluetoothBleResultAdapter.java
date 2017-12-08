package com.rfchina.internet.rfvoiceprintrecognitiondemo.bluetooth;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rfchina.internet.rfvoiceprintrecognitiondemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luoyican on 2017/11/28.
 */

public class BluetoothBleResultAdapter extends BaseAdapter {
    private List<BluetoothEPRResultAdapter.BluetoothBean> datas;

    public BluetoothBleResultAdapter(List<BluetoothEPRResultAdapter.BluetoothBean> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public BluetoothEPRResultAdapter.BluetoothBean getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_bluetooth_ble, null);
            viewHolder = new ViewHolder();
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.txtAddress = (TextView) convertView.findViewById(R.id.txtAddress);
            viewHolder.txtRssi = (TextView) convertView.findViewById(R.id.txtRssi);
            viewHolder.txtUuids = (TextView) convertView.findViewById(R.id.txtUuids);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothEPRResultAdapter.BluetoothBean bb = getItem(position);
        viewHolder.txtName.setText(TextUtils.isEmpty(bb.getBluetoothDevice().getName()) ? "unknow device" : bb.getBluetoothDevice().getName());
        viewHolder.txtAddress.setText("address:" + bb.getBluetoothDevice().getAddress());
        viewHolder.txtRssi.setText("rssi:" + bb.getDistance());
        String uuids = "";
        if (bb.getScanRecord() != null && bb.getScanRecord().getServiceUuids() != null && bb.getScanRecord().getServiceUuids().size() > 0)
            for (ParcelUuid uuid : bb.getScanRecord().getServiceUuids()) {
                uuids += uuid + " "+(bb.getScanRecord().getServiceData(uuid) == null || bb.getScanRecord().getServiceData(uuid).length == 0 ? "" : new String(bb.getScanRecord().getServiceData(uuid))) + "\n";
                Log.e("DDDD",uuids);
            }
        viewHolder.txtUuids.setText("service UUIDs:" + uuids);
        return convertView;
    }

    class ViewHolder {
        private TextView txtName;
        private TextView txtAddress;
        private TextView txtRssi;
        private TextView txtUuids;

        public ViewHolder() {
        }
    }
}
