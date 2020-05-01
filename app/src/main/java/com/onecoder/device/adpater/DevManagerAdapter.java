package com.onecoder.device.adpater;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.onecoder.device.R;
import com.onecoder.device.base.BleDev;
import com.onecoder.devicelib.base.control.entity.BleDevice;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class DevManagerAdapter extends BaseAdapter {

    private Context context;
    private List<BleDev> baseDeviceList;

    public DevManagerAdapter(Context context) {
        this.context = context;
    }

    public List<BleDev> getBaseDeviceList() {
        return baseDeviceList;
    }

    public void setBaseDeviceList(List<BleDev> baseDeviceList) {
        this.baseDeviceList = baseDeviceList;
        notifyDataSetChanged();
    }

    public int getCount() {
        return baseDeviceList == null ? 0 : baseDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return baseDeviceList != null && baseDeviceList.size() > position ? baseDeviceList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (context == null) {
            return convertView;
        }

        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_dev_manager_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        setDateToView(position, viewHolder);
        return convertView;
    }

    private void setDateToView(int position, ViewHolder viewHolder) {
        Object object = getItem(position);
        if (!(object instanceof BleDev)) {
            return;
        }
        BleDev bleDev = (BleDev) object;
        viewHolder.devNameTxt.setText("" + bleDev.getName());
        viewHolder.connectState.setText(
                context.getString(bleDev.getConnectState() >= BleDevice.STATE_CONNECTED
                        ? R.string.connected : R.string.unconnected));

    }

    static class ViewHolder {
        @BindView(R.id.dev_name_txt)
        TextView devNameTxt;
        @BindView(R.id.connect_state)
        TextView connectState;


        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
