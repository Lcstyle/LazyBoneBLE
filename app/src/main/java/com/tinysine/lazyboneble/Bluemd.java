package com.tinysine.lazyboneble;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Application;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tinysine.lazyboneble.geolocation.BackgroundGeoFenceLocationService;
import com.tinysine.lazyboneble.geolocation.GeofenceNotificationHelper;
import com.tinysine.lazyboneble.geolocation.SelectMapsHomeLocation;
import com.tinysine.lazyboneble.service.BluetoothLeService;
import com.tinysine.lazyboneble.service.BluetoothLeService.LocalBinder;
import com.tinysine.lazyboneble.util.LogUtil;
import com.tinysine.lazyboneble.util.Util;
import com.tinysine.lazyboneble.util.SharedPreferencesUtil;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import static com.tinysine.lazyboneble.DeviceListActivity.reg_dev_name;
import static com.tinysine.lazyboneble.DeviceListActivity.reg_dev_password;
import static com.tinysine.lazyboneble.DeviceListActivity.registered_device_name_key;
import static com.tinysine.lazyboneble.SettingsActivity.VANITY_NAME_KEY;

@SuppressLint("InflateParams")
public class Bluemd extends Activity {


	private ImageView iv_connect_status;
	private Button btn_connect_name;
	private Button btn_status;

	public boolean isOn = false;

	private boolean ok = false;
	private boolean isModeConnectSuccess = false;

	public static final String GEO_REQUEST_DISCONNECT_DEVICE = "REQUEST_DISCONNECT_DEVICE";
	public static final String GEO_REQUEST_CONNECT_DEVICE = "REQUEST_CONNECT_DEVICE";

	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;

	private int status = Util.BT_DATA_PASSWORD;

	private static final int BAIDU_READ_PHONE_STATE = 100;//Location permission request
	private static final int PRIVATE_CODE = 1315;//Turn on GPS permissions

	private static String VANITY_NAME;

	private static final String ADDRESS = "address";	//Local cache address identification
	private static final String ADDRESS_NAME = "addressName"; // Local cache address identification

	private String address; // device address
	private String mConnectedDeviceName; // device name

	private ModeThread modeThread = null;

	private BluetoothLeService mBluetoothLeService;
	private BroadcastReceiver locationTransitionReceiver;
	private BluetoothListenerReceiver receiver; //Bluetooth open and close monitoring service
	private SelectMapsHomeLocation dla_instance;

	private Intent bg_loc_service_intent;

	static final String[] LOCATION_GPS = new String[] {
			Manifest.permission.ACCESS_COARSE_LOCATION,
			Manifest.permission.ACCESS_FINE_LOCATION,
			Manifest.permission.READ_PHONE_STATE };


	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder service) {
			mBluetoothLeService = ((LocalBinder) service).getService();
			if (!mBluetoothLeService.initialize()) {
				LogUtil.e("Unable to initialize Bluetooth");
				finish();
			}
			LogUtil.e("mBluetoothLeService is okay");

			if (!TextUtils.isEmpty(address)) checkIsConnect();
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private boolean isNeedPassword = false;
	private boolean isVerify = false;

	/**
	 * Restore default settings
	 */
	private void resetDefault() {
		isConnected = false;
		isOn = false;
		isNeedPassword = false;
		isModeConnectSuccess = false;
		iv_connect_status.setImageResource(R.drawable.im_disconnect);
		btn_status.setBackgroundResource(R.drawable.btn_normal);
		btn_connect_name.setText("");
		if (progressDialog != null) progressDialog.dismiss();
		if (pDialog != null) pDialog.cancel();
		if (updateThread != null) updateThread.stopThread();
		if (modeThread != null) {
			modeThread.modeStop();
			modeThread = null;
		}
		if (firstTimeThread != null) firstTimeThread.StopThread();
		if (isConnected && mBluetoothLeService != null) {
			isConnected = false;
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();
		}
		if (mBluetoothLeService != null)
				mBluetoothLeService.disconnect();
	}

	/**
	 * Set button state
	 */
	private void setButtonStatus() {
		if (isOn)
			{
				btn_status.setBackgroundResource(R.drawable.btn_selected);
				dla_instance.setHomeGeoFence(this);
			}
		else btn_status.setBackgroundResource(R.drawable.btn_normal);
	}


	public class BluetoothListenerReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction()))
				{
					int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
					switch (blueState)
						{
							case BluetoothAdapter.STATE_TURNING_ON:
								Log.e("onReceive:", "onReceive: Bluetooth is turning on");
								break;
							case BluetoothAdapter.STATE_ON:
								Log.e("onReceive", "onReceive: Bluetooth is turned on");
								if (!TextUtils.isEmpty(address))checkIsConnect();
								break;
							case BluetoothAdapter.STATE_TURNING_OFF:
								Log.e("onReceive", "onReceive: Bluetooth is turning off");
								break;
							case BluetoothAdapter.STATE_OFF:
								Log.e("onReceive", "onReceive: Bluetooth is off");
								break;
						}
				}
		}
	}


	public class locationTransitionReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent)
			{
				final String action = intent.getAction();
				if (Bluemd.GEO_REQUEST_DISCONNECT_DEVICE.equals(action))
					{
						finishAndRemoveTask();

						if (isOn)
							{
								liveData("6f");
								Toast.makeText(context, VANITY_NAME + " ShutDown", Toast.LENGTH_SHORT).show();
								Log.e("LocTransRcvr:", "LazyBone GeoFence " + VANITY_NAME + " Shutting Down");
								finishAndRemoveTask();
							}
					}
				else if (Bluemd.GEO_REQUEST_CONNECT_DEVICE.equals(action))
					{
						if (!(isOn))
							{
								String msg = VANITY_NAME + " AutoStart";
								GeofenceNotificationHelper notificationHelper = new GeofenceNotificationHelper(context);
								notificationHelper.sendHighPriorityNotification("LazyBone GeoFence", msg, Bluemd.class);
								liveData("65");
								Toast.makeText(context, VANITY_NAME + " AutoStart", Toast.LENGTH_SHORT).show();
								Log.e("LocTransRcvr:", "LazyBone GeoFence " + VANITY_NAME + " Starting");
							}
					}
				}
		}

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
				{
					final String action = intent.getAction();
					if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
						{
							iv_connect_status.setImageResource(R.drawable.im_connecting);
							isConnected = true;
							SharedPreferencesUtil.putString(Bluemd.this, ADDRESS, address);
							SharedPreferencesUtil.putString(Bluemd.this, ADDRESS_NAME, mConnectedDeviceName);
							LogUtil.e("1111111111111111111111" + address);
							LogUtil.e("1111111111111111111111" + mConnectedDeviceName);

						} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
							resetDefault();
						else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
						{
							isConnected = true;
							if (progressDialog != null)
								progressDialog.dismiss();
							iv_connect_status.setImageResource(R.drawable.im_connected);
							setConnectName();
							askMode();
						} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
						{
							byte[] datas = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
							if (status == Util.BT_DATA_MODE)
								{
									if (datas != null && datas.length == 7)
										{
											askMode();
											return;
										}
									if (datas == null || datas.length != 1)
										{
											askMode();
											return;
										}
									isModeConnectSuccess = true;
									int value = datas[0] & 0xff;
									if (value == 17)
										{
											isNeedPassword = true;
											if (!(reg_dev_name.equals(""))) sendRegisteredPassword();
											else popEditPassword();
										}
									else
										{
											isNeedPassword = false;
											sendStatus();
										}
								}
							if (status == Util.BT_DATA_STATUS)
								{
									if (datas == null || datas.length != 1)
											return;
									if (progressDialog != null)
											progressDialog.dismiss();
									int value = datas[0] & 0xff;
									isOn = value == 1;
									setButtonStatus();
								} else if (status == Util.BT_DATA_PASSWORD)
								{
									if (datas == null || datas.length != 1)
											return;
									int password = datas[0] & 0xff;
									if (pDialog != null)
											pDialog.cancel();
									if (password == 1)
										{
											isVerify = true;
											sendStatus();
											Toast.makeText(Bluemd.this, "Verify successful!", Toast.LENGTH_SHORT).show();
											progressDialog = ProgressDialog.show(Bluemd.this,"Please wait...", "Updating status, please wait...", true, true);
											liveData("65");
											setButtonStatus();
										}
									else if (password != 17)
										{
											Toast.makeText(Bluemd.this,"Verify failed, password incorrect! Please reset your board!", Toast.LENGTH_SHORT).show();
											isVerify = false;
											popEditPassword();
										}
								}
						}
				}
		};

	/**
	 * Query working mode momentary or latching
	 */
	private void askMode() {
		if (!isModeConnectSuccess) {
			status = Util.BT_DATA_MODE;
			liveData("3C");
			if (modeThread != null) {
				modeThread.modeStop();
				modeThread = null;
			}
			modeThread = new ModeThread();
			modeThread.start();
		}
	}

	public String convertStringToHex(String str) {
		char[] chars = str.toCharArray();
		StringBuilder hex = new StringBuilder();
		for (char aChar : chars)
				hex.append(Integer.toHexString(aChar));
		return hex.toString();
	}

	private ProgressDialog progressDialog;
	private FirstTimeThread firstTimeThread;
	private boolean isConnected = false;
	public static final String PREFS_NAME = "MyPrefsFile";

	public SharedPreferences preferences;
	private SharedPreferences defaultPreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.ac_main);

		defaultPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences = getSharedPreferences(PREFS_NAME, 0);

		receiver = new BluetoothListenerReceiver();
		registerReceiver(receiver, makeFilter());

		locationTransitionReceiver = new locationTransitionReceiver();
		registerReceiver(locationTransitionReceiver, makeLocationTransitionFilter() );

		iv_connect_status = findViewById(R.id.iv_connect_status);
		btn_connect_name = findViewById(R.id.btn_connect_name);
		btn_status = findViewById(R.id.btn_status);
		VANITY_NAME = preferences.getString(VANITY_NAME_KEY, "");

		// logo Control
		ImageView iv_logo1 = findViewById(R.id.logo);
		iv_logo1.setOnClickListener(v -> {
			LogUtil.e("1111111111111111111111111111111111111111");
			if (!isConnected && !TextUtils.isEmpty(address))
				checkIsConnect();
		});

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null)
			{
				finishDialogNoBluetooth();
				return;
			}

		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null)
			{
				finishDialogNoBluetooth();
				return;
			}


		btn_status.setOnClickListener(v -> {
			resetAutoDisconnect();

			boolean isMomentMode = defaultPreferences.getBoolean(ModeSettingActivity.KEY_MOMENT_MODE, false);
			if (isMomentMode)
				{
					if (isOn)
						liveData("6f");
					else
						{
							int value = defaultPreferences.getInt(ModeSettingActivity.KEY_FULSE_TIME, 100) / 100;
							String hex = Integer.toHexString(value);
							hex = hex.length() == 1 ? "0" + hex : hex;
							liveData("63" + hex);
						}
				} else
				{
					if (isOn)
						liveData("6f");
					else
						liveData("65");
				}
		});

		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Verifying...");
		pDialog.setCancelable(false);

		LinearLayout ll_bgLayout = findViewById(R.id.ll_bg);
		ll_bgLayout.setOnClickListener(arg0 -> openOptionsMenu());

		// Fetch locally cached data
		address = SharedPreferencesUtil.getString(Bluemd.this, ADDRESS, "");
		mConnectedDeviceName = SharedPreferencesUtil.getString(Bluemd.this, ADDRESS_NAME, "");

		// AUTO START CONNECT DIALOG - For Registered Device Automatic Connection
		Intent serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
	}

	private void startLocationUpdates()
		{
			int backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
				enableUserLocation();

			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				enableUserLocation();


			Context appContext = getApplicationContext();
			bg_loc_service_intent = new Intent(appContext, BackgroundGeoFenceLocationService.class);
			appContext.startForegroundService(bg_loc_service_intent);
			Toast.makeText(appContext, "Located Updates Enabled...", Toast.LENGTH_SHORT).show();
		}

	private void enableUserLocation()
		{
			int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
			int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);

			if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
		}

	private IntentFilter makeFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		return filter;
	}

	// Check whether positioning and gps are turned on
	private void checkIsConnect() {
		LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
		ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); // 是否定位
		if (!ok) { // Targeting is not turned on
			Toast.makeText(Bluemd.this, "It appears that the GPS location service is not turned on, please turn it on", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, PRIVATE_CODE);
		} else {
			if (!mBluetoothAdapter.isEnabled())
				mBluetoothAdapter.enable();
			else
				live(address);
		}
	}

	/**
	 * Check whether GPS and location permissions are turned on
	 */
	private void showGPSContacts() {
		LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (ok) {//Location service
			//Determine whether it is the android6.0 system version, if it is, you need to dynamically add permissions
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PERMISSION_GRANTED)
				ActivityCompat.requestPermissions(this, LOCATION_GPS, BAIDU_READ_PHONE_STATE);
		} else {
			Toast.makeText(this, "This app need location permission, please enable it!", Toast.LENGTH_LONG).show();
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivityForResult(intent, PRIVATE_CODE);
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothDevice.ACTION_UUID);
		return intentFilter;
	}

	private static IntentFilter makeLocationTransitionFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Bluemd.GEO_REQUEST_DISCONNECT_DEVICE);
		intentFilter.addAction(Bluemd.GEO_REQUEST_CONNECT_DEVICE);
		return intentFilter;
	}

	private void sendStatus() {
		status = Util.BT_DATA_STATUS;
		if (firstTimeThread != null)
			firstTimeThread.StopThread();

		firstTimeThread = new FirstTimeThread();
		firstTimeThread.start();
	}

	private class FirstTimeThread extends Thread {
		boolean flag = true;

		@Override
		public void run() {
			while (flag) {
				if (isConnected) {
					if (mBluetoothLeService != null) {
						byte[] datas = { 0x5B };
						mBluetoothLeService.WriteBytes(datas);
					}
				}
				try {
					sleep(500);
				} catch (InterruptedException ignored) {
				}
			}
		}

		public void StopThread() {
			flag = false;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mEnablingBT = false;
		showGPSContacts();
	}

	private BluetoothAdapter mBluetoothAdapter = null;
	private boolean mEnablingBT = false;


	@Override
	public synchronized void onResume() {
		super.onResume();
		if (!TextUtils.isEmpty(address))
			checkIsConnect(); // Check if it is connected

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		if (!mEnablingBT) {
			if ((mBluetoothAdapter != null) && (!mBluetoothAdapter.isEnabled())) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.alert_dialog_turn_on_bt)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.alert_dialog_warning_title)
						.setCancelable(false)
						.setPositiveButton(R.string.alert_dialog_yes, (dialog, id) -> {
									mEnablingBT = true;
									Intent enableIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE );
									startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
								})
						.setNegativeButton(R.string.alert_dialog_no, (dialog, id) -> finishDialogNoBluetooth());
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}

	private void finishDialogNoBluetooth() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.alert_dialog_no_bt)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.app_name)
				.setCancelable(false)
				.setPositiveButton(R.string.alert_dialog_ok, (dialog, id) -> finish());
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getMenuInflater();
		if (isNeedPassword)
			inflater.inflate(R.menu.option_menu, menu);
		else
			inflater.inflate(R.menu.menu_nopass, menu);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.connect: {
				resetDefault();
				Intent serverIntent = new Intent(this, DeviceListActivity.class);
				serverIntent.putExtra("scan_override", true);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
				return true;
			case R.id.exit: {
				Bluemd.this.finish();
			}
				return true;
			case R.id.geofence_settings: {
					Intent intent = new Intent(Bluemd.this, SelectMapsHomeLocation.class);
					startActivity(intent);
				}
				return true;
			case R.id.mode: {
				if (!isConnected) {
					showText("Please connect the device first!");
					break;
				}
				Intent intent = new Intent(Bluemd.this, ModeSettingActivity.class);
				startActivity(intent);
				}
				return true;
			case R.id.general_settings: {
				Intent intent = new Intent(Bluemd.this, SettingsActivity.class);
				startActivity(intent);
			}
				return true;
			case R.id.changepassword: {
				if (!isConnected) {
					showText("Please connect the device first!");
					break;
				}

				if (!isVerify) {
					showText("Please verify the password first!");
					break;
				}
				LayoutInflater layoutInflater = LayoutInflater.from(this);
				final View myLoginView = layoutInflater.inflate( R.layout.ac_password, null );
				AlertDialog.Builder dlgChgPsd = new AlertDialog.Builder(this);
				AlertDialog dialog = dlgChgPsd
						.setTitle("Change Password")
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(myLoginView)
						.setCancelable(false)
						.setPositiveButton("OK", (arg0, arg1) -> {
							isVerify = true;
							String strOrgPsd = ((EditText) (myLoginView.findViewById(R.id.orgpsd))).getText().toString();
							String strNewPsd = ((EditText) (myLoginView.findViewById(R.id.newpsd))).getText().toString();
							String strNewPsd2 = ((EditText) (myLoginView.findViewById(R.id.newpsd2))).getText().toString();
							if (!strOrgPsd.equals(strInputPsd)) showText("The original password is not correct!");
							else if (strNewPsd.equals("")) showText("The password cannot be empty!");
							else if (strNewPsd.length() != 6) showText("The password must be 6 digits!");
							else if (!strNewPsd.equals(strNewPsd2)) showText("The passwords are not same!");
							else {
									saveRegisteredDevicePassword(strNewPsd);
									resetPassword("40" + int2Byte(strNewPsd));
									showText("The password has been changed successfully!");
								}
							})
						.setNegativeButton("Quit", (arg0, arg1) -> isVerify = true).create();
				dialog.show();
				return true;
				}
			default:
				return super.onOptionsItemSelected(item);
		}
		return false;
	}

	private void showText(String message) {
		AlertDialog.Builder builder = new Builder(Bluemd.this);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("Information");
		builder.setMessage(message);
		builder.setPositiveButton("OK", null);
		builder.show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE:
				if (resultCode == RESULT_OK) {
					mConnectedDeviceAddress = data.getStringExtra(DeviceListActivity.DEVICE_ADDRESS_KEY);
					live(mConnectedDeviceAddress);
					startLocationUpdates();
					}
				break;
			case REQUEST_ENABLE_BT:
				if ((mBluetoothAdapter != null) && (!mBluetoothAdapter.isEnabled())) {
					finishDialogNoBluetooth();
				}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		// requestCode is the declared permission acquisition code, which is passed in when checkSelfPermission
		if (requestCode == BAIDU_READ_PHONE_STATE)
			{
				if (grantResults[0] == PERMISSION_GRANTED) //Have permission
					{ // Obtain the permission and deal with it accordingly
						if (!TextUtils.isEmpty(address))
								live(address); // auto connect
					} else //If the user cancels, permissions may be null.
						showGPSContacts();
			}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (firstTimeThread != null)
			firstTimeThread.StopThread();

		unbindService(mServiceConnection);
		mBluetoothLeService = null;
		unregisterReceiver(receiver);
		unregisterReceiver(locationTransitionReceiver);
		stopService(bg_loc_service_intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	private String mConnectedDeviceAddress = null;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you wish to quit?")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.app_name)
					.setCancelable(false)
					.setPositiveButton("Yes",(dialog, id) -> {
								android.os.Process.killProcess(android.os.Process.myPid());
								Bluemd.this.finish();
							})
					.setNegativeButton("No", (dialog, which) -> { });
			AlertDialog alert = builder.create();
			alert.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void sendData(String hexValue) {
		if (!isConnected)
			return;
		if (mBluetoothLeService != null) {
			mBluetoothLeService.WriteString(hexValue);
		}
	}

	public void liveData(String hexValue) {
		if (!isConnected) {
			resetDefault();
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		} else {
			sendData(hexValue);
		}
	}

	private void live(String address) {
		mBluetoothLeService.connect(address);
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Status");
		progressDialog.setMessage("Connecting...");
		progressDialog.setCancelable(true);
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
			mBluetoothLeService.disconnect();
			progressDialog.dismiss();
		});
		progressDialog.show();
	}

	private void sendPassword(String password) {
		status = Util.BT_DATA_PASSWORD;
		sendData(password);
	}

	private void saveRegisteredDevicePassword(String password)
		{
			if (!(reg_dev_name.equals("")))
				{
					Editor editor = preferences.edit();
					editor.putString(DeviceListActivity.registered_device_password_key, password);
					editor.apply();
				}
		}

	private void resetPassword(String password) {
		sendData(password);
	}

	private ProgressDialog pDialog;
	public static String strInputPsd = "";

	private void sendRegisteredPassword()
		{
			String paString = "3F" + int2Byte(reg_dev_password);
			sendPassword(paString);
		}

	private void popEditPassword() {
		final EditText eText = new EditText(this);
		InputFilter[] filters = { new InputFilter.LengthFilter(6) };
		eText.setFilters(filters);
		eText.setInputType(InputType.TYPE_CLASS_NUMBER);
		eText.setTransformationMethod(PasswordTransformationMethod.getInstance());

		new AlertDialog.Builder(this)
				.setTitle("Please input the password!")
				.setIcon(android.R.drawable.ic_dialog_info)
				.setView(eText)
				.setCancelable(false)
				.setPositiveButton("OK", (arg0, arg1) -> {
					String pa = eText.getText().toString();
					if (!pa.equals("") && pa.length() == 6) {
						pDialog.show();
						strInputPsd = pa;
						saveRegisteredDevicePassword(strInputPsd);
						String paString = "3F" + int2Byte(pa);
						sendPassword(paString);
					} else {
						Toast.makeText(Bluemd.this,"Password is 6 digits!", Toast.LENGTH_SHORT).show();
						popEditPassword();
					}
				})
				.setNegativeButton("Quit", (arg0, arg1) -> resetDefault()).create().show();
	}


	private String int2Byte(String value) {
		int va = Integer.parseInt(value);
		String hex = Integer.toHexString(va);
		int len = hex.length();
		String data = "000000";
		data = data.substring(0, 6 - len) + hex;
		return String.format("%s%s%s", data.substring(4, 6), data.substring(2, 4), data.substring(0, 2));
	}

	private void setConnectName() {
		String connectName = preferences.getString(mConnectedDeviceAddress, "");
		btn_connect_name.setText(connectName);
	}


	private void resetAutoDisconnect() {
		boolean isAutoMode = defaultPreferences.getBoolean(ModeSettingActivity.KEY_AUTO_MODE, false);
		if (isAutoMode) {
			if (updateThread == null) {
				updateThread = new UpdateThread();
				updateThread.start();
			}
			updateThread.reset();
		}
	}

	private UpdateThread updateThread = null;

	private final Handler mHandler = new Handler();

	private class UpdateThread extends Thread {
		boolean flag = true;
		int count = 0;

		@Override
		public void run() {
			super.run();
			while (flag) {
				int value = defaultPreferences.getInt( ModeSettingActivity.KEY_AUTO_TIME, 1);

				if (count == value) {
					mHandler.post(() -> {
						boolean isAutoMode = defaultPreferences.getBoolean(ModeSettingActivity.KEY_AUTO_MODE, false);
						if (isAutoMode) { resetDefault(); }
					});
				}
				count++;
				try {
					sleep(1000);
				} catch (InterruptedException ignored) {
				}
			}
		}

		public void reset() {
			count = 0;
		}

		public void stopThread() {
			flag = false;
			count = 0;
		}
	}

	public void onModifyName(View v) {
		final EditText eText = new EditText(this);
		new AlertDialog.Builder(this).setTitle("Please input new name")
				.setIcon(android.R.drawable.ic_dialog_info).setView(eText)
				.setCancelable(false)
				.setPositiveButton("OK", (arg0, arg1) -> {
					String newName = eText.getText().toString();
					if (!newName.equals("")) {
						Editor editor = preferences.edit();
						editor.putString(mConnectedDeviceAddress, newName);
						editor.putString(registered_device_name_key, newName);
						editor.putString(DeviceListActivity.registered_device_addr_key, mConnectedDeviceAddress);
						editor.apply();
						setConnectName();
						DeviceListActivity.MATCH_NAME = newName;
						Toast.makeText(Bluemd.this, "Device Renamed and Registered for AutoConnect!", Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton("Cancel", null).create().show();
	}

	private class ModeThread extends Thread {

		private boolean isStop = false;

		@Override
		public void run() {
			super.run();
			try {
				sleep(3000);
				if (!isStop && !isModeConnectSuccess)
					sendStatus();
			} catch (Exception ignored) {
			}
		}
		public void modeStop() {
			isStop = true;
		}
	}
}