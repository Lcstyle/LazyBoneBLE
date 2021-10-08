/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tinysine.lazyboneble;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.tinysine.lazyboneble.adapter.LeDeviceListAdapter;
import com.tinysine.lazyboneble.util.BLEDevice;

public class DeviceListActivity extends Activity {

	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private boolean mRegisteredScanning;

	private static long AUTO_CONNECT_SCAN_PERIOD;
	private static long GENERAL_SCAN_PERIOD;
	public static String AUTO_CONNECT_SCAN_PERIOD_KEY = "auto_scan_period";
	public static String GENERAL_SCAN_PERIOD_KEY = "general_scan_period";
	public static String DEVICE_ADDRESS_KEY = "device_address";
	public static String DEVICE_NAME_KEY = "device_name";
	public static String MATCH_NAME = "Lazy";
	public static String registered_device_name_key = "REG_DEV_NAME";
	public static String registered_device_addr_key = "REG_DEV_ADDR";
	public static String registered_device_password_key = "REG_DEV_PASSWORD";
	public static String reg_dev_name;
	public static String reg_dev_addr;
	public static String reg_dev_password;

	private LeDeviceListAdapter mPairedDevicesArrayAdapter;
	private LeDeviceListAdapter mNewDevicesArrayAdapter;

	private SharedPreferences preferences;
	private Set<BluetoothDevice> pairedDevices;
	private Editor editor;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTitle(R.string.scanning_reg_dev);
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
			{
				Toast.makeText(this, "Bluetooth not supported.", Toast.LENGTH_SHORT).show();
				finish();
			}

		boolean scan_override = getIntent().getBooleanExtra("scan_override", false);
		preferences = getSharedPreferences(Bluemd.PREFS_NAME, 0);
		editor = preferences.edit();
		AUTO_CONNECT_SCAN_PERIOD = preferences.getLong(AUTO_CONNECT_SCAN_PERIOD_KEY, 15000);
		GENERAL_SCAN_PERIOD = preferences.getLong(GENERAL_SCAN_PERIOD_KEY, 30000);
		setResult(Activity.RESULT_CANCELED);
		reg_dev_name = preferences.getString(registered_device_name_key, "");
		mNewDevicesArrayAdapter = new LeDeviceListAdapter(this);
		mPairedDevicesArrayAdapter = new LeDeviceListAdapter(this);

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		mBluetoothAdapter.stopLeScan(mLeScanCallback);

		if (mBluetoothAdapter == null)
			{
				Toast.makeText(this, "Bluetooth not supported.", Toast.LENGTH_SHORT).show();
				finish();
				return;
			}

		if (scan_override) reg_dev_name = "";

		if (!(reg_dev_name.equals("")))
			{
				reg_dev_addr = preferences.getString(registered_device_addr_key, "");
				reg_dev_password = preferences.getString(registered_device_password_key, "");
				doRegisteredDeviceDiscovery();
			}
		else
			showDeviceActivityUI();
	}

	private void showDeviceActivityUI()
		{
			Toast.makeText(this, "Registered Device Not Detected", Toast.LENGTH_SHORT).show();
			setContentView(R.layout.device_list);
			setTitle(R.string.scanning);

			Button scanButton = findViewById(R.id.button_scan);
			scanButton.setOnClickListener(v -> {
				stopRegisteredFind();
				doDiscovery();
				v.setVisibility(View.GONE);
			});
			pairedDevices = mBluetoothAdapter.getBondedDevices();

			ListView pairedListView = findViewById(R.id.paired_devices);
			pairedListView.setAdapter(mPairedDevicesArrayAdapter);
			pairedListView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				BLEDevice de = mPairedDevicesArrayAdapter.getDevice(arg2);
				String address = de.getAddress();
				if (address == null || address.equals(""))
						return;

				Intent intent = new Intent();
				intent.putExtra(DEVICE_NAME_KEY, de.getName());
				intent.putExtra(DEVICE_ADDRESS_KEY, address);
				setResult(RESULT_OK, intent);
				finish();
			});

			ListView newDevicesListView = findViewById(R.id.new_devices);
			newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
			newDevicesListView.setOnItemClickListener((arg0, arg1, arg2, arg3) -> {
				mBluetoothAdapter.stopLeScan(mLeScanCallback);
				BLEDevice de = mNewDevicesArrayAdapter.getDevice(arg2);
				String address = de.getAddress();
				if (address == null || address.equals(""))
					return;

				Intent intent = new Intent();
				intent.putExtra(DEVICE_NAME_KEY, de.getName());
				intent.putExtra(DEVICE_ADDRESS_KEY, address);
				setResult(RESULT_OK, intent);
				finish();
			});

			if (pairedDevices.size() > 0)
				{
					findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
					for (BluetoothDevice device : pairedDevices)
						{
							String nameString = device.getName();
							mBluetoothAdapter.stopLeScan(mLeScanCallback);
							String address = device.getAddress();
							nameString = preferences.getString(address, nameString);
							editor.putString(address, nameString);
							editor.apply();
							BLEDevice de = new BLEDevice();
							de.setName(nameString);
							de.setAddress(address);
							mPairedDevicesArrayAdapter.addDevice(de);
						}
				} else
				{
					String noDevices = getResources().getText(R.string.none_paired).toString();
					BLEDevice de = new BLEDevice(noDevices, "");
					mPairedDevicesArrayAdapter.addDevice(de);
				}
		}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		stopFind();
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBluetoothAdapter.isDiscovering())
			mBluetoothAdapter.cancelDiscovery();

		scanLeDevice();
	}

	private void doRegisteredDeviceDiscovery() {
		// Indicate scanning in the title
		setProgressBarIndeterminateVisibility(true);

		// If we're already discovering, stop it
		if (mBluetoothAdapter.isDiscovering())
			mBluetoothAdapter.cancelDiscovery();

		Toast.makeText(this, "Scanning for nearby Registered Device", Toast.LENGTH_SHORT).show();
		findRegisteredLeDevice();
	}

	private final Handler mHandler = new Handler();

	private void scanLeDevice() {
		mHandler.postDelayed(() -> {
			if (mScanning)
				stopFind();
		}, GENERAL_SCAN_PERIOD);

		mScanning = true;
		mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	private void findRegisteredLeDevice() {
		mHandler.postDelayed(() -> {
			if (mRegisteredScanning) {
				stopRegisteredFind();
				setProgressBarIndeterminateVisibility(false);
				showDeviceActivityUI();
			}
		}, AUTO_CONNECT_SCAN_PERIOD);

		mRegisteredScanning = true;
		mBluetoothAdapter.startLeScan(uniqueLeScanCallback);
	}

	private void stopFind() {
		mScanning = false;
		mBluetoothAdapter.stopLeScan(mLeScanCallback);
		setProgressBarIndeterminateVisibility(false);
		setTitle(R.string.select_device);
		if (mNewDevicesArrayAdapter.getCount() == 0) {
			String noDevices = getResources().getText(R.string.none_found).toString();
			BLEDevice de = new BLEDevice(noDevices, "");
			mNewDevicesArrayAdapter.addDevice(de);
		}
	}

	private void stopRegisteredFind() {
		mRegisteredScanning = false;
		mBluetoothAdapter.stopLeScan(uniqueLeScanCallback);
	}

	private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
		{
			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
				{
					runOnUiThread(() -> {
						if (device.getBondState() != BluetoothDevice.BOND_BONDED)
							{
								String devNameStr = null;
								String prefNameStr;
								String address = device.getAddress();

								prefNameStr = preferences.getString(address, "");

								if (prefNameStr == null || prefNameStr.equals(""))
									devNameStr = device.getName();
								if (prefNameStr != null && devNameStr != null)
									{
										if (!(prefNameStr.equals(devNameStr)))
											{
												editor.putString(address, devNameStr);
												editor.apply();
											}
									}

								if (devNameStr == null && address.equals(reg_dev_addr))
									devNameStr = reg_dev_name;

								if (!(devNameStr == null || devNameStr.equals("") || address.equals("")))
									{
										editor.putString(address, devNameStr);
										editor.apply();
										BLEDevice de = new BLEDevice();
										de.setName(devNameStr);
										de.setAddress(address);
										mPairedDevicesArrayAdapter.addDevice(de);
									}
							}
					});
				}
		};

	private final BluetoothAdapter.LeScanCallback uniqueLeScanCallback = new BluetoothAdapter.LeScanCallback()
		{
			@Override
			public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord)
				{
					DeviceListActivity.this.runOnUiThread(() -> {
						if (device.getBondState() != BluetoothDevice.BOND_BONDED)
							{
								String address = device.getAddress();

								if (address.equals(reg_dev_addr))
									{
										Toast.makeText(DeviceListActivity.this, "Registered Device Found, AutoConnecting", Toast.LENGTH_SHORT).show();
										DeviceListActivity.this.stopRegisteredFind();
										Intent intent = new Intent();
										intent.putExtra(DEVICE_NAME_KEY, reg_dev_name);
										intent.putExtra(DEVICE_ADDRESS_KEY, reg_dev_addr);
										DeviceListActivity.this.setResult(RESULT_OK, intent);
										DeviceListActivity.this.finish();
									}
							}
					});
				}
		};
}
