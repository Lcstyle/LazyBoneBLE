package com.tinysine.lazyboneble.service;

import java.util.List;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.tinysine.lazyboneble.util.LogUtil;
import com.tinysine.lazyboneble.util.Util;

public class BluetoothLeService extends Service {
	private BluetoothManager mBluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt;
	// GATT - > GENERIC ATTRIBUTE PROFILE OR ATT - ATTRIBUTE PROTOCOL
	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

	public final static UUID UUID_NOTIFY = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	public final static UUID UUID_SERVICE = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

	public BluetoothGattCharacteristic mNotifyCharacteristic;

	public void WriteString(String strValue) {
		byte[] datas = Util.hexStr2Bytes(strValue);
		if (mNotifyCharacteristic != null) {
			mNotifyCharacteristic.setValue(datas);
			mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
		}
	}

	public void WriteBytes(byte[] datas) {
		if (mNotifyCharacteristic != null && datas != null) {
			mNotifyCharacteristic.setValue(datas);
			if (mBluetoothGatt != null) {
				mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
			}
		}
	}

	public void ReadValue() {
		mBluetoothGatt.readCharacteristic(mNotifyCharacteristic);
	}

	public void findService(List<BluetoothGattService> gattServices) {
		for (BluetoothGattService gattService : gattServices) {
			LogUtil.e(gattService.getUuid().toString());
			LogUtil.e(UUID_SERVICE.toString());
			if (gattService.getUuid().toString()
					.equalsIgnoreCase(UUID_SERVICE.toString())) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService
						.getCharacteristics();
				LogUtil.e("Count is:" + gattCharacteristics.size());
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					if (gattCharacteristic.getUuid().toString()
							.equalsIgnoreCase(UUID_NOTIFY.toString())) {
						mNotifyCharacteristic = gattCharacteristic;
						setCharacteristicNotification(gattCharacteristic, true);
						broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
						return;
					}
				}
			}
		}
	}

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			String intentAction;
			LogUtil.e("oldStatus=" + status + " NewStates=" + newState);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					intentAction = ACTION_GATT_CONNECTED;
					broadcastUpdate(intentAction);
					LogUtil.e("Connected to GATT server.");
					LogUtil.e("Attempting to start service discovery:" + mBluetoothGatt.discoverServices());
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					intentAction = ACTION_GATT_DISCONNECTED;
					mBluetoothGatt.close();
					mBluetoothGatt = null;
					LogUtil.e("Disconnected from GATT server.");
					broadcastUpdate(intentAction);
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				LogUtil.e("onServicesDiscovered received: " + status);
				findService(gatt.getServices());
			} else if (mBluetoothGatt.getDevice().getUuids() == null)
					LogUtil.e("onServicesDiscovered received: " + status);
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			LogUtil.e("onCharacteristicRead: " + status);
			if (status == BluetoothGatt.GATT_SUCCESS)
				broadcastUpdate(characteristic);
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			broadcastUpdate(characteristic);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
		}

		@Override
		public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
			LogUtil.e("onDescriptorRead");
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
			LogUtil.e("onDescriptorWrite");
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b) {
			LogUtil.e("onReadRemoteRssi");
		}

		@Override
		public void onReliableWriteCompleted(BluetoothGatt gatt, int a) {
			LogUtil.e("onReliableWriteCompleted");
		}

	};

	private void broadcastUpdate(final String action) {
		final Intent intent = new Intent(action);
		sendBroadcast(intent);
	}

	private void broadcastUpdate(final BluetoothGattCharacteristic characteristic) {
		final Intent intent = new Intent(BluetoothLeService.ACTION_DATA_AVAILABLE);

		final byte[] data = characteristic.getValue();
		if (data != null && data.length > 0)
			intent.putExtra(EXTRA_DATA, data);

		sendBroadcast(intent);
	}

	public class LocalBinder extends Binder {
		public BluetoothLeService getService() {
			return BluetoothLeService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		close();
		return super.onUnbind(intent);
	}

	private final IBinder mBinder = new LocalBinder();

	public boolean initialize() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null) {
				LogUtil.e("Unable to initialize BluetoothManager.");
				return false;
			}
		}
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if (mBluetoothAdapter == null) {
			LogUtil.e("Unable to obtain a BluetoothAdapter.");
			return false;
		}
		return true;
	}

	public boolean connect(final String address) {
		if (mBluetoothAdapter == null || address == null) {
			LogUtil.e("BluetoothAdapter not initialized or unspecified address.");
			return false;
		}
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		if (device == null) {
			LogUtil.e("Device not found.  Unable to connect.");
			return false;
		}
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
		}
		mBluetoothGatt = device.connectGatt(this, true, mGattCallback);
		LogUtil.e("Trying to create a new connection.");
		return true;
	}

	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LogUtil.e("BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}

	public void close() {
		if (mBluetoothGatt == null) {
			return;
		}
		mBluetoothGatt.close();
		mBluetoothGatt = null;
	}

	public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LogUtil.e("BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.readCharacteristic(characteristic);
	}

	public void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			LogUtil.e("BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
	}

	public List<BluetoothGattService> getSupportedGattServices() {
		if (mBluetoothGatt == null)
			return null;

		return mBluetoothGatt.getServices();
	}
}
