package com.tinysine.lazyboneble.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

import android.preference.PreferenceManager;
import android.util.Log;

import com.tinysine.lazyboneble.R;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.tinysine.lazyboneble.SettingsActivity.AUTO_CONNECT_WIFI_TRIGGER_DEV_NAME_KEY;


public class wiFi_AutoStartReceiver extends BroadcastReceiver
    {
        private void createNotificationChannel(Context context)
            {
                // Create the NotificationChannel, but only on API 26+ because
                // the NotificationChannel class is new and not in the support library
                //                CharSequence name = getString("");
                //                String description = getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("LazyBoneBLE-WiFi-AutoStart", "LazyBoneBLE WiFi AutoStart", importance);
                channel.setDescription("LazyBoneBLE Registered Notification Channel");
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = getSystemService(context, NotificationManager.class);
                if (notificationManager != null)
                    notificationManager.createNotificationChannel(channel);
            }

        public void onReceive(Context context, Intent intent)
            {
                //                ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
                //                cm.get
                //                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                ////                WifiInfo info = wifiManager.getConnectionInfo();
                ////                String ssid  = info.getSSID();
                //


                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String AUTO_CONNECT_WIFI_TRIGGER_DEV_NAME = prefs.getString(AUTO_CONNECT_WIFI_TRIGGER_DEV_NAME_KEY, "");
                createNotificationChannel(context);
                String action = intent.getAction();
            }
    }
//                String device_name = wifiNetwork.getName();
//
//                switch (action)
//                    {
//                        case BluetoothDevice.ACTION_ACL_CONNECTED:
//                            Log.v("BTRECEIVER", "Device Connected: " + device_name);
//                            if (device_name.contains(AUTO_CONNECT_WIFI_TRIGGER_DEV_NAME))
//                                ActivateSwitch(context);
//                            break;
//
//                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
//                            Log.v("BTRECEIVER", "Device Disconnected: " + device_name);
//                            break;
//                    }
//            }

//        public void ActivateSwitch(Context context)
//            {
//                PackageManager pm = context.getPackageManager();
//                Intent launchIntent = pm.getLaunchIntentForPackage("com.tinysine.lazyboneble");
//                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//                // .setContentIntent(pendingIntent)
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "LazyBoneBLE-AutoStart")
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle("LazyBoneBLE")
//                        .setContentText("AutoLaunch")
//                        .setPriority(NotificationCompat.PRIORITY_HIGH)
//                        .setCategory(NotificationCompat.CATEGORY_CALL)
//                        .setTimeoutAfter(60000)
//                        .setFullScreenIntent(pendingIntent, true)
//                        .setAutoCancel(true);
//
//                Notification blueToothConnected = builder.build();
//                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//                notificationManager.notify(1, blueToothConnected);
//            }
//    }