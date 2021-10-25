package com.tinysine.lazyboneble.geolocation;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.tinysine.lazyboneble.Bluemd;
import com.tinysine.lazyboneble.R;


import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import static com.tinysine.lazyboneble.Bluemd.PREFS_NAME;
import static com.tinysine.lazyboneble.geolocation.SelectMapsHomeLocation.HOME_LATLNG_KEY;


public class BackgroundGeoFenceLocationService extends Service
    {
        Notification bgLocationNotification;
        Intent ServiceCancelIntent;
        private LocationRequest mLocationRequest;
        private LocationCallback locationCallback;
        private static final String PACKAGE_NAME = "com.tinysine.lazyboneble.geolocation.BackgroundGeoFenceLocationService";
        private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME + ".started_from_notification";
        private static final long INTERVAL = 1000 * 10;
        private static final long FASTEST_INTERVAL = 1000 * 5;
        private FusedLocationProviderClient fusedLocationClient;
        private static final String TAG = "BG_Location_Service";
        public SharedPreferences preferences;
        public LatLng mHomeLatLng;
        public String mHomeLatLngStr;
        private GeofencingClient mGeofencingClient;
        public GeofenceHelper geofenceHelper;
        private final float GEOFENCE_RADIUS = 200;
        private Handler mServiceHandler;
        HandlerThread handlerThread;
        public static String VANITY_NAME_KEY = "vanity_name";

        @Override
        public void onDestroy()
            {
                super.onDestroy();
                fusedLocationClient.removeLocationUpdates(locationCallback);
                mServiceHandler.removeCallbacksAndMessages(null);
            }

        @Override
        public void onRebind(Intent intent)
            {
                // Called when a client (MainActivity in case of this sample) returns to the foreground
                // and binds once again with this service. The service should cease to be a foreground
                // service when that happens.
                Log.i(TAG, "in onRebind()");
                stopForeground(true);
                super.onRebind(intent);
            }


        @Override
        public boolean onUnbind(Intent intent)
            {
                Log.i(TAG, "Last client unbound from service");
                // Called when the last client (MainActivity in case of this sample) unbinds from this
                // service. If this method is called due to a configuration change in MainActivity, we
                // do nothing. Otherwise, we make this service a foreground service.
                Log.i(TAG, "Starting foreground service");
                startForeground(1000, bgLocationNotification);

                return true; // Ensures onRebind() is called when a client re-binds.
            }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId)
            {
                Log.i(TAG, "Service started");
                boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION, false);

                // We got here because the user decided to remove location updates from the notification.
                if (startedFromNotification)
                    {
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                        stopSelf();
                    }
                ServiceCancelIntent = new Intent(this, BackgroundGeoFenceLocationService.class);
                Intent ApplicationLaunchIntent = new Intent(this, Bluemd.class);

                ServiceCancelIntent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

                PendingIntent ServiceIntent = PendingIntent.getService(this, 1001, ServiceCancelIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                PendingIntent ApplicationIntent = PendingIntent.getActivity(this, 1001, ApplicationLaunchIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, 0);
                String VANITY_NAME = preferences.getString(VANITY_NAME_KEY, "");


                bgLocationNotification = new NotificationCompat.Builder(this, "bgLocRcvr")
                        .addAction(R.drawable.im_disconnect, "Stop", ServiceIntent)
                        .setDeleteIntent(ServiceIntent)
                        .setContentTitle(getText(R.string.bg_loc_notification_title))
                        .setContentText(getText(R.string.bg_loc_notification_message) + " " + VANITY_NAME)  // add vanity name printout (no space required for msg+vanityname concatenation)
                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(ApplicationIntent)
                        .build();


                startForeground(1000, bgLocationNotification);
                return Service.START_NOT_STICKY;
            }

        @Override
        public void onTaskRemoved(Intent rootIntent)
            {
                super.onTaskRemoved(rootIntent);
                mServiceHandler.removeCallbacksAndMessages(null);
            }

        @Override
        public void onCreate()
            {
                super.onCreate();
                preferences = this.getSharedPreferences(PREFS_NAME, 0);
                mHomeLatLngStr = preferences.getString(HOME_LATLNG_KEY, "");
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                mGeofencingClient = LocationServices.getGeofencingClient(this);
                geofenceHelper = new GeofenceHelper(this);
                handlerThread = new HandlerThread(TAG);
                handlerThread.start();
                mServiceHandler = new Handler(handlerThread.getLooper());

                locationCallback = new LocationCallback()
                    {
                        @Override
                        public void onLocationResult(@NonNull LocationResult locationResult)
                            {
                                for (Location location : locationResult.getLocations())
                                    {
                                        Log.d(TAG, "onLocationResult: ");
                                        super.onLocationResult(locationResult);
                                        //                                        onNewLocation(locationResult.getLastLocation());
                                    }
                            }
                    };

                if (!(mHomeLatLngStr.equals("")) && mHomeLatLng == null)
                    {
                        setHomeLatLng();
                        String returnMsg = addGeofence(mHomeLatLng, GEOFENCE_RADIUS);
                        Toast.makeText(this, returnMsg, Toast.LENGTH_SHORT).show();
                    }

                createNotificationChannel();
                startLocationUpdates();
            }

        //        private void onNewLocation(Location location) {
        //            Log.i(TAG, "New location: " + location);
        //
        //            mLocation = location;
        //
        //            // Notify anyone listening for broadcasts about the new location.
        //            Intent intent = new Intent(ACTION_BROADCAST);
        //            intent.putExtra(EXTRA_LOCATION, location);
        //            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        //
        //            // Update notification content if running as a foreground service.
        //            if (serviceIsRunningInForeground(this)) {
        //                mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        //            }
        //        }

        public String addGeofence(LatLng latLng, float radius)
            {
                String GEOFENCE_ID = "HOME_GEOFENCE";
                Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
                GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
                PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
                AtomicReference<String> returnMsg = new AtomicReference<>("GeoFence Added");

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
//                        return TODO;
                    }
                mGeofencingClient.addGeofences(geofencingRequest, pendingIntent).addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Home Location Geofence Successfully registered"))
                        .addOnFailureListener(e -> {
                            String errorMessage = geofenceHelper.getErrorString(e);
                            Log.d(TAG, "onFailure: " + errorMessage);
                            returnMsg.set(errorMessage);
                        });
                return returnMsg.get();
            }

        private void setHomeLatLng()
            {
                String[] latLngArr = mHomeLatLngStr.split(",");
                String latStr = latLngArr[0].split("\\(")[1];
                String lngStr = latLngArr[1].split("\\)")[0];
                double lat = Double.parseDouble(latStr);
                double lng = Double.parseDouble(lngStr);
                mHomeLatLng = new LatLng(lat, lng);
            }

        private void createNotificationChannel()
        {
            NotificationChannel channel = new NotificationChannel("bgLocRcvr", "bgLocRcvr", NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.createNotificationChannel(channel);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent)
            {
                return null;
            }


        protected void createLocationRequest()
            {
                mLocationRequest = LocationRequest.create();
                mLocationRequest.setInterval(INTERVAL);
                mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }

        private void requestLocationUpdates()
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, mServiceHandler.getLooper());

            }

        protected void startLocationUpdates()
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                Log.d(TAG, "Location update started ..............: ");


                this.createLocationRequest();
                this.requestLocationUpdates();
            }
    }
