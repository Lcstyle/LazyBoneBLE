package com.tinysine.lazyboneble;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class ModeSettingActivity extends Activity {

	private CheckBox cb_fulsetime;
	private CheckBox cb_autoTime;
	private TextView tv_fulseTime;
	private TextView tv_autoTime;

	public static final String KEY_FULSE_TIME = "fulse_time";
	public static final String KEY_MOMENT_MODE = "moment_mode";
	public static final String KEY_AUTO_TIME = "auto_time";
	public static final String KEY_AUTO_MODE = "auto_mode";
	private int current_fulseTime = 100;
	private int current_autoTime = 10;
	private SharedPreferences pre;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_mode);
		init();

		pre = PreferenceManager.getDefaultSharedPreferences(this);

		current_fulseTime = pre.getInt(KEY_FULSE_TIME, 100);
		cb_fulsetime.setChecked(pre.getBoolean(KEY_MOMENT_MODE, false));
		cb_fulsetime.setOnCheckedChangeListener((buttonView, isChecked) -> updateFulseTime());
		updateFulseTime();

		current_autoTime = pre.getInt(KEY_AUTO_TIME, 10);
		cb_autoTime.setChecked(pre.getBoolean(KEY_AUTO_MODE, false));
		cb_autoTime.setOnCheckedChangeListener((buttonView, isChecked) -> updateAutoTime());
		updateAutoTime();

	}

	private void updateFulseTime() {
		tv_fulseTime.setText(String.format("%dMS", current_fulseTime));
		Editor editor = pre.edit();
		editor.putBoolean(KEY_MOMENT_MODE, cb_fulsetime.isChecked());
		editor.putInt(KEY_FULSE_TIME, current_fulseTime);
		editor.apply();
	}

	private void updateAutoTime() {
		tv_autoTime.setText(current_autoTime + "S");
		Editor editor = pre.edit();
		editor.putBoolean(KEY_AUTO_MODE, cb_autoTime.isChecked());
		editor.putInt(KEY_AUTO_TIME, current_autoTime);
		editor.apply();
	}

	private void init() {
		cb_fulsetime = findViewById(R.id.cb_fulsetime);
		cb_autoTime = findViewById(R.id.cb_autoTime);
		tv_fulseTime = findViewById(R.id.tv_fulseTime);
		tv_autoTime = findViewById(R.id.tv_autoTime);
	}

	public void onBack(View v) {
		finish();
	}

	public void onPlusAutoTime(View v) {
		if (current_autoTime < 300) {
			current_autoTime += 10;
			updateAutoTime();
		}
	}

	public void onMinusAutoTime(View v) {
		if (current_autoTime > 10) {
			current_autoTime -= 10;
			updateAutoTime();
		}
	}

	public void onPlusFulseTime(View v) {
		if (current_fulseTime < 20000) {
			current_fulseTime += 100;
			updateFulseTime();
		}
	}

	public void onMinusFulseTime(View v) {
		if (current_fulseTime > 100) {
			current_fulseTime -= 100;
			updateFulseTime();
		}
	}
}
