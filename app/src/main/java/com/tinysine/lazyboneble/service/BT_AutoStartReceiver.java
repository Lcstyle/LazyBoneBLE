package com.tinysine.lazyboneble.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.preference.PreferenceManager;
import android.util.Log;
import com.tinysine.lazyboneble.R;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.tinysine.lazyboneble.SettingsActivity.AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY;


public class BT_AutoStartReceiver extends BroadcastReceiver
    {
        private static final String TAG = "BT_AutoStartReceiver";

        private void createNotificationChannel(Context context) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("LazyBoneBLE-AutoStart", "LazyBoneBLE AutoStart", importance);
            channel.setDescription("LazyBoneBLE Registered Notification Channel");
            NotificationManager notificationManager = getSystemService(context, NotificationManager.class);
            if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
        }

        public void onReceive(Context context, Intent intent)
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String AUTO_CONNECT_BT_TRIGGER_DEV_NAME = prefs.getString(AUTO_CONNECT_BT_TRIGGER_DEV_NAME_KEY, "");
                createNotificationChannel(context);
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String device_name = device.getName();
                switch (action)
                    {
                        case BluetoothDevice.ACTION_ACL_CONNECTED:
                            Log.v(TAG, "Device Connected: " + device_name);
                            if (device_name.contains(AUTO_CONNECT_BT_TRIGGER_DEV_NAME))
                                    ActivateSwitch(context);
                            break;

                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                            Log.v(TAG, "Device Disconnected: " + device_name);
                            break;
                    }
            }

        public void ActivateSwitch(Context context)
        {
            PackageManager pm = context.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage("com.tinysine.lazyboneble");
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LazyBoneBLE-AutoStart")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("LazyBoneBLE")
                    .setContentText("AutoLaunch")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setTimeoutAfter(60000)
                    .setFullScreenIntent(pendingIntent, true)
                    .setAutoCancel(true);

            Notification blueToothConnected = builder.build();
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1, blueToothConnected);
        }
    }