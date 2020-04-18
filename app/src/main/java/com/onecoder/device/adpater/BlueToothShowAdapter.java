package com.onecoder.device.adpater;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.onecoder.device.R;
import com.onecoder.devicelib.base.control.entity.BluetoothBean;

import java.util.List;


public class BlueToothShowAdapter extends BaseAdapter {

	private Context context;

	List<BluetoothBean> bluetoothList;

	private OnItemClickListner itemClickListner ;

	/**
	 * 点击Item的回调
	 */
	public interface OnItemClickListner{
		void  onItemClick(BluetoothBean device);
	}

	public BlueToothShowAdapter(Context context) {
		this.context = context;
	}

	public List<BluetoothBean> getBluetoothList() {
		return bluetoothList;
	}

	public void setBluetoothList(List<BluetoothBean> bluetoothList) {
		this.bluetoothList = bluetoothList;
		notifyDataSetChanged();
	}

	public int getCount() {
		return bluetoothList == null ? 0 : bluetoothList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return bluetoothList != null ? bluetoothList.get(arg0) : null;
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder =null;
		if (convertView==null){
			convertView = LayoutInflater.from(context).inflate(R.layout.layout_bluetoothitem, null);
			viewHolder=new ViewHolder();
			viewHolder.rssi = (TextView) convertView.findViewById(R.id.text_rssi);
			viewHolder.text_mac=(TextView)convertView.findViewById(R.id.text_mac);
			viewHolder.xinhao=(ImageView)convertView.findViewById(R.id.xinhao);
			viewHolder.item= (LinearLayout) convertView.findViewById(R.id.item_device);
			convertView.setTag(viewHolder);
		}else {
			viewHolder= (ViewHolder) convertView.getTag();
		}
		setDateToView(position,viewHolder);
		setItemClickEvent(position,viewHolder);
		return convertView;
	}

	private void setItemClickEvent(int position, ViewHolder viewHolder) {
        if (bluetoothList == null || bluetoothList.size() <= position
                || bluetoothList.get(position) == null) {
            return;
        }
		final BluetoothBean blueTooth = bluetoothList.get(position);
//		final BluetoothDevice device = blueTooth.getDevice();
		viewHolder.item.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (itemClickListner!=null){
					itemClickListner.onItemClick(blueTooth);
				}
			}
		});
	}


	private void setDateToView(int position, ViewHolder viewHolder) {
        if (bluetoothList == null || bluetoothList.size() <= position
                || bluetoothList.get(position) == null) {
            return;
        }
        int status = bluetoothList.get(position).getBleDevice().getBondState();
        int sige = bluetoothList.get(position).getRssi();
        if (status==12)
        {
			viewHolder.xinhao.setImageResource(R.mipmap.img_bluetooth_isconnect);

        }
		if (-120 < sige &&sige< -100){
			viewHolder.xinhao.setImageResource(R.mipmap.xinhaotiao00);

		}
		if (-100 < sige &&sige< -80){
			viewHolder.xinhao.setImageResource(R.mipmap.xinhaotiao01);

		}
		if (-80 < sige &&sige< -60){
			viewHolder.xinhao.setImageResource(R.mipmap.xinhaotiao02);

		}
		if (-60 < sige &&sige< -40){
			viewHolder.xinhao.setImageResource(R.mipmap.xinhaotiao03);

		}
		if (-40 < sige &&sige< -20){
			viewHolder.xinhao.setImageResource(R.mipmap.xinhaotiao04);
		}

		viewHolder.rssi.setText("RSSI"+bluetoothList.get(position).getRssi()+ "\t\t"+ bluetoothList.get(position).getBleDevice().getName()+"");
		viewHolder.text_mac.setText(bluetoothList.get(position).getBleDevice().getAddress()+"");

	}


	class  ViewHolder {
		public TextView rssi=null;
		public TextView text_mac=null;
		public ImageView xinhao=null;
		public LinearLayout item=null;
	}


	public OnItemClickListner getItemClickListner() {
		return itemClickListner;
	}

	public void setItemClickListner(OnItemClickListner itemClickListner) {
		this.itemClickListner = itemClickListner;
	}
}
