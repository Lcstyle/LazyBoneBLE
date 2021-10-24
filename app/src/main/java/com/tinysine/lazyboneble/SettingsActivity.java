package com.tinysine.lazyboneble;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static com.tinysine.lazyboneble.Bluemd.PREFS_NAME;

public class SettingsActivity extends Activity
    {
        private TextView tv_scanTime;
        private TextView tv_autoConnectTime;
        private TextView tv_geoFenceDwellDelay;
        private EditText et_vanity_name;
        private EditText et_trigger_dev_name;
        private static long AUTO_CONNECT_SCAN_PERIOD;
        private static long GENERAL_SCAN_PERIOD;
        private static int GEOFENCE_DWELL_DELAY;


        private static String VANITY_NAME;
        public static String VANITY_NAME_KEY = "vanity_name";
        public static String AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY = "bt_connect_trigger_dev";
        public static String AUTO_CONNECT_BT_TRIGGER_DEV_NAME;
        public static String AUTO_CONNECT_SCAN_PERIOD_KEY = "auto_scan_period";
        public static String GENERAL_SCAN_PERIOD_KEY = "general_scan_period";
        public static String GEOFENCE_DWELL_TIME_KEY = "geofence_dwell_delay";

        private SharedPreferences.Editor sharedPrefsEditor;
        private SharedPreferences.Editor prefsEditor;

        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.general_settings);
                init();
                // DEFAULT PREFS
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                sharedPrefsEditor = sharedPreferences.edit();
                // USER PREFS
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
                prefsEditor = preferences.edit();

                // DEFAULT PREFS
                AUTO_CONNECT_SCAN_PERIOD = sharedPreferences.getLong(AUTO_CONNECT_SCAN_PERIOD_KEY, 15000);
                tv_autoConnectTime.setText(String.format("%sS", TimeUnit.MILLISECONDS.toSeconds(AUTO_CONNECT_SCAN_PERIOD)));

                GENERAL_SCAN_PERIOD = sharedPreferences.getLong(GENERAL_SCAN_PERIOD_KEY, 30000);
                tv_scanTime.setText(String.format("%sS", TimeUnit.MILLISECONDS.toSeconds(GENERAL_SCAN_PERIOD)));

                // USER PREFS
                GEOFENCE_DWELL_DELAY = preferences.getInt(GEOFENCE_DWELL_TIME_KEY, 60);
                tv_geoFenceDwellDelay.setText(String.format("%sS", GEOFENCE_DWELL_DELAY));

                AUTO_CONNECT_BT_TRIGGER_DEV_NAME = preferences.getString(AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY, "");
                et_trigger_dev_name.setText(AUTO_CONNECT_BT_TRIGGER_DEV_NAME);

                VANITY_NAME = preferences.getString(VANITY_NAME_KEY, "");
                et_vanity_name.setText(VANITY_NAME);
            }

        @Override
        protected void onDestroy()
            {
                super.onDestroy();
                update_vanity_name();
                update_trigger_dev_name();
                sharedPrefsEditor.apply();
                prefsEditor.apply();
                Toast.makeText(this, "Preferences Updated", Toast.LENGTH_SHORT).show();
            }

        private void update_trigger_dev_name()
            {
                String updated_trigger_dev_name = et_trigger_dev_name.getText().toString();
                if (!(AUTO_CONNECT_BT_TRIGGER_DEV_NAME.equals(updated_trigger_dev_name)))
                    {
                        prefsEditor.putString(AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY, updated_trigger_dev_name);
                        Toast.makeText(this, "Triggering Device Name Updated", Toast.LENGTH_SHORT).show();
                    }
            }

        private void update_vanity_name()
            {
                String updated_vanity_name = et_vanity_name.getText().toString();
                if (!(VANITY_NAME.equals(updated_vanity_name)))
                    {
                        prefsEditor.putString(VANITY_NAME_KEY, updated_vanity_name);
                        Toast.makeText(this, "Vanity Name Updated", Toast.LENGTH_SHORT).show();
                    }
            }

        public void onBack(View v) {
            this.finish();
        }

        private void updateScanTime()
        {
            String seconds = Long.toString(TimeUnit.MILLISECONDS.toSeconds(GENERAL_SCAN_PERIOD));
            tv_scanTime.setText(String.format("%sS", seconds));
            sharedPrefsEditor.putLong(GENERAL_SCAN_PERIOD_KEY, GENERAL_SCAN_PERIOD);
            Toast.makeText(this, "Scan Time Settings Updated", Toast.LENGTH_SHORT).show();
        }

        private void updateAutoConnectTime()
        {
            String seconds = Long.toString(TimeUnit.MILLISECONDS.toSeconds(AUTO_CONNECT_SCAN_PERIOD));
            tv_autoConnectTime.setText(String.format("%sS", seconds));
            sharedPrefsEditor.putLong(AUTO_CONNECT_SCAN_PERIOD_KEY, AUTO_CONNECT_SCAN_PERIOD);
            Toast.makeText(this, "AutoConnect Time Settings Updated", Toast.LENGTH_SHORT).show();
        }

        private void updateGeoFenceDwellTime()
            {
                String seconds = Integer.toString(GEOFENCE_DWELL_DELAY);
                tv_geoFenceDwellDelay.setText(String.format("%sS", seconds));
                prefsEditor.putInt(GEOFENCE_DWELL_TIME_KEY, GEOFENCE_DWELL_DELAY);
                Toast.makeText(this, "GeoFence Dwell Time Settings Updated", Toast.LENGTH_SHORT).show();
            }

        @SuppressLint("ClickableViewAccessibility")
        private void init() {
            tv_autoConnectTime = findViewById(R.id.tv_autoConnectTime);
            tv_scanTime = findViewById(R.id.tv_scanPeriod);
            tv_geoFenceDwellDelay = findViewById(R.id.tv_geoFenceDwellDelay);
            et_vanity_name = findViewById(R.id.vanity_name);
            et_trigger_dev_name = findViewById(R.id.et_trigger_dev_name);

            Button bt_geo_add_time = findViewById(R.id.bt_geo_add_time);
            Button bt_geo_minus_time = findViewById(R.id.bt_geo_minus_time);

            bt_geo_minus_time.setOnTouchListener(new View.OnTouchListener() {
                private Handler mHandler;

                @Override
                public boolean onTouch(View v, MotionEvent mEvent)
                 {
                     switch (mEvent.getAction())
                         {
                             case MotionEvent.ACTION_DOWN:
                                 if (mHandler != null) return true;
                                 mHandler = new Handler();
                                 mHandler.postDelayed(minus_dwell_time, 500);
                                 break;
                             case MotionEvent.ACTION_UP:
                                 if (mHandler == null) return true;
                                 mHandler.removeCallbacks(minus_dwell_time);
                                 updateGeoFenceDwellTime();
                                 mHandler = null;
                                 break;
                         }
                     return false;
                 }

                final Runnable minus_dwell_time = new Runnable() {
                    @Override public void run() {
                        if (GEOFENCE_DWELL_DELAY > 1)
                                GEOFENCE_DWELL_DELAY -= 1;
                        String seconds = Integer.toString(GEOFENCE_DWELL_DELAY);
                        tv_geoFenceDwellDelay.setText(String.format("%sS", seconds));
                        mHandler.postDelayed(this, 50);
                    }
                };
            });

            bt_geo_add_time.setOnTouchListener(new View.OnTouchListener() {
                private Handler mHandler;

                @Override
                public boolean onTouch(View v, MotionEvent mEvent)
                    {
                        switch (mEvent.getAction())
                            {
                                case MotionEvent.ACTION_DOWN:
                                    if (mHandler != null) return true;
                                    mHandler = new Handler();
                                    mHandler.postDelayed(add_dwell_time, 500);
                                    break;
                                case MotionEvent.ACTION_UP:
                                    if (mHandler == null) return true;
                                    mHandler.removeCallbacks(add_dwell_time);
                                    updateGeoFenceDwellTime();
                                    mHandler = null;
                                    break;
                            }
                        return false;
                    }
                final Runnable add_dwell_time = new Runnable() {
                    @Override public void run() {
                        if (GEOFENCE_DWELL_DELAY < 3600)
                            GEOFENCE_DWELL_DELAY += 1;
                        String seconds = Integer.toString(GEOFENCE_DWELL_DELAY);
                        tv_geoFenceDwellDelay.setText(String.format("%sS", seconds));
                        mHandler.postDelayed(this, 50);
                    }
                };
            });
        }

        public void onAddAutoConnectTime(View v) {
            if (AUTO_CONNECT_SCAN_PERIOD < 60000) {
                AUTO_CONNECT_SCAN_PERIOD += 1000;
                updateAutoConnectTime();
            }
        }

        public void onMinusAutoConnectTime(View v) {
            if (AUTO_CONNECT_SCAN_PERIOD > 1000) {
                AUTO_CONNECT_SCAN_PERIOD -= 1000;
                updateAutoConnectTime();
            }
        }

        public void onPlusScanTime(View v) {
            if (GENERAL_SCAN_PERIOD < 60000) {
                GENERAL_SCAN_PERIOD += 1000;
                updateScanTime();
            }
        }

        public void onMinusScanTime(View v) {
            if (GENERAL_SCAN_PERIOD > 1000) {
                GENERAL_SCAN_PERIOD -= 1000;
                updateScanTime();
            }
        }
        public void onAddGeoDwellTime(View v) {
            if (GEOFENCE_DWELL_DELAY < 3600) {
                GEOFENCE_DWELL_DELAY += 1;
                updateGeoFenceDwellTime();
            }
        }

        public void onMinusGeoDwellTime(View v) {
            if (GEOFENCE_DWELL_DELAY > 1) {
                GEOFENCE_DWELL_DELAY -= 1;
                updateGeoFenceDwellTime();
            }
        }
    }