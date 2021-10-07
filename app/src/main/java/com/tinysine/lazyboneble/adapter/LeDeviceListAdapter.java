package com.tinysine.lazyboneble.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tinysine.lazyboneble.R;
import com.tinysine.lazyboneble.util.BLEDevice;

public class LeDeviceListAdapter extends BaseAdapter {

	private ArrayList<BLEDevice> mLeDevices;
	private LayoutInflater mInflator;

	public LeDeviceListAdapter(Context mContext) {
		super();
		mLeDevices = new ArrayList<BLEDevice>();
		mInflator = LayoutInflater.from(mContext);
	}

	public void addDevice(BLEDevice device) {
		if (!mLeDevices.contains(device)) {
			mLeDevices.add(device);

		notifyDataSetChanged();
		}
	}

	public BLEDevice getDevice(int position) {
		return mLeDevices.get(position);
	}

	public void clear() {
		mLeDevices.clear();
	}

	@Override
	public int getCount() {
		return mLeDevices.size();
	}

	@Override
	public Object getItem(int i) {
		return mLeDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		if (view == null) {
			view = mInflator.inflate(R.layout.listitem_device, null);
			viewHolder = new ViewHolder();
			viewHolder.deviceAddress = view
					.findViewById(R.id.device_address);
			viewHolder.deviceName = view
					.findViewById(R.id.device_name);
			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}
		BLEDevice device = mLeDevices.get(i);
		final String deviceName = device.getName();
		if (deviceName != null && deviceName.length() > 0) {
			viewHolder.deviceName.setText(deviceName);
		} else {
			viewHolder.deviceName.setText(R.string.unknown_device);
		}
		viewHolder.deviceAddress.setText(device.getAddress());
		return view;
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}

}
