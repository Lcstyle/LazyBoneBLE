package com.tinysine.lazyboneble;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends Activity
    {
        private TextView tv_scanTime;
        private TextView tv_autoConnectTime;
        private EditText et_vanity_name;
        private EditText et_trigger_dev_name;
        private static long AUTO_CONNECT_SCAN_PERIOD;
        private static long GENERAL_SCAN_PERIOD;
        private static String VANITY_NAME;
        public static String AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY = "bt_connect_trigger_dev";
        public static String AUTO_CONNECT_BT_TRIGGER_DEV_NAME;
        public static String AUTO_CONNECT_SCAN_PERIOD_KEY = "auto_scan_period";
        public static String GENERAL_SCAN_PERIOD_KEY = "general_scan_period";
        public static String VANITY_NAME_KEY = "vanity_name";

        private SharedPreferences.Editor prefsEditor;

        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.general_settings);
                init();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefsEditor = prefs.edit();

                AUTO_CONNECT_SCAN_PERIOD = prefs.getLong(AUTO_CONNECT_SCAN_PERIOD_KEY, 15000);
                GENERAL_SCAN_PERIOD = prefs.getLong(GENERAL_SCAN_PERIOD_KEY, 30000);

                AUTO_CONNECT_BT_TRIGGER_DEV_NAME = prefs.getString(AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY, "");
                et_trigger_dev_name.setText(AUTO_CONNECT_BT_TRIGGER_DEV_NAME);

                VANITY_NAME = prefs.getString(VANITY_NAME_KEY, "");
                et_vanity_name.setText(VANITY_NAME);
            }

        @Override
        protected void onDestroy()
            {
                super.onDestroy();
                update_vanity_name();
                update_trigger_dev_name();

            }

        private void update_trigger_dev_name()
            {
                String updated_trigger_dev_name = et_trigger_dev_name.getText().toString();
                if (!(AUTO_CONNECT_BT_TRIGGER_DEV_NAME.equals(updated_trigger_dev_name)))
                    {
                        prefsEditor.putString(AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY, updated_trigger_dev_name);
                        prefsEditor.apply();
                    }
            }

        private void update_vanity_name()
            {
                String updated_vanity_name = et_vanity_name.getText().toString();
                if (!(VANITY_NAME.equals(updated_vanity_name)))
                    {
                        prefsEditor.putString(VANITY_NAME_KEY, updated_vanity_name);
                        prefsEditor.apply();
                    }
            }

        private void updateScanTime()
        {
            String seconds = Long.toString(TimeUnit.MILLISECONDS.toSeconds(GENERAL_SCAN_PERIOD));
            tv_scanTime.setText(String.format("%sS", seconds));
            prefsEditor.putLong(GENERAL_SCAN_PERIOD_KEY, GENERAL_SCAN_PERIOD);
            prefsEditor.apply();
        }

        private void updateAutoConnectTime()
        {
            String seconds = Long.toString(TimeUnit.MILLISECONDS.toSeconds(AUTO_CONNECT_SCAN_PERIOD));
            tv_autoConnectTime.setText(String.format("%sS", seconds));
            prefsEditor.putLong(AUTO_CONNECT_SCAN_PERIOD_KEY, AUTO_CONNECT_SCAN_PERIOD);
            prefsEditor.apply();
        }

        private void init() {
            tv_autoConnectTime = findViewById(R.id.tv_autoConnectTime);
            tv_scanTime = findViewById(R.id.tv_scanPeriod);
            et_vanity_name = findViewById(R.id.vanity_name);
            et_trigger_dev_name = findViewById(R.id.et_trigger_dev_name);
        }

        public void onBack(View v) {
            finish();
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
    }