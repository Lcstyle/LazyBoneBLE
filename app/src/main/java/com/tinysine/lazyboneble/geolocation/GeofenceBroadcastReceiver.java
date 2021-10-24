package com.tinysine.lazyboneble.geolocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.tinysine.lazyboneble.Bluemd;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "LAZYBLEGeoBroadcastRcvr";

    @Override
    public void onReceive(Context context, Intent intent) {

        GeofenceNotificationHelper notificationHelper = new GeofenceNotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();

        for (Geofence geofence : geofenceList)
            {
                Log.d(TAG, "onReceive: " + geofence.getRequestId());
            }
        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType)
            {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    Toast.makeText(context, "Home GeoFence Entered", Toast.LENGTH_SHORT).show();
                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    String msg = "Home Location Auto Shutdown Activated";
                    notificationHelper.sendHighPriorityNotification("LazyBone GeoFence", msg, Bluemd.class);
                    send_disconnect_request(context);
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    Toast.makeText(context, "Home GeoFence Exit", Toast.LENGTH_LONG).show();
                    send_connect_request(context);
                    break;
            }
    }

    private void send_disconnect_request(Context context)
        {
            Intent transition_broadcast = new Intent();
            transition_broadcast.setAction(Bluemd.GEO_REQUEST_DISCONNECT_DEVICE);
            context.sendBroadcast(transition_broadcast);
        }

    private void send_connect_request(Context context)
        {
            Intent transition_broadcast = new Intent();
            transition_broadcast.setAction(Bluemd.GEO_REQUEST_CONNECT_DEVICE);
            context.sendBroadcast(transition_broadcast);
        }
}