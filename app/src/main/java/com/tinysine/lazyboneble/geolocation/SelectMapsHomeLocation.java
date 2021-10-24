package com.tinysine.lazyboneble.geolocation;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.tinysine.lazyboneble.R;
import com.tinysine.lazyboneble.databinding.ActivitySelectMapsHomeLocationBinding;

import java.util.concurrent.atomic.AtomicReference;

import static com.tinysine.lazyboneble.Bluemd.PREFS_NAME;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SelectMapsHomeLocation extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener
    {
        private static final int DEFAULT_ZOOM = 15;

        private final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
        private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

        public static final String HOME_LATLNG_KEY = "home_location";
        private static final String TAG = "LazyBoneMapsActivity";

        private final float GEOFENCE_RADIUS = 200;


        public SharedPreferences preferences;

        public GeofenceHelper geofenceHelper;

        public LatLng mHomeLatLng;
        public String mHomeLatLngStr;

        private GoogleMap mMap;
        private GeofencingClient mGeofencingClient;
        private FusedLocationProviderClient fusedLocationClient;

        @Override
        protected void onCreate(Bundle savedInstanceState)
            {
                super.onCreate(savedInstanceState);
                com.tinysine.lazyboneble.databinding.ActivitySelectMapsHomeLocationBinding binding = ActivitySelectMapsHomeLocationBinding.inflate(getLayoutInflater());
                setContentView(binding.getRoot());
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null)
                    mapFragment.getMapAsync(this);
                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                setHomeGeoFence(this);
            }

        public void setHomeGeoFence(Activity caller)
            {
                preferences = caller.getSharedPreferences(PREFS_NAME, 0);
                mHomeLatLngStr = preferences.getString(HOME_LATLNG_KEY, "");
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(caller);
                mGeofencingClient = LocationServices.getGeofencingClient(caller);
                geofenceHelper = new GeofenceHelper(caller);


                if (!(mHomeLatLngStr.equals("")) && mHomeLatLng == null)
                    {
                        setHomeLatLng();
                        String returnMsg = addGeofence(caller, mHomeLatLng, GEOFENCE_RADIUS);
                        Toast.makeText(caller, returnMsg, Toast.LENGTH_SHORT).show();
                    }
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



        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera. In this case,
         * we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to install
         * it inside the SupportMapFragment. This method will only be triggered once the user has
         * installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap)
            {
                mMap = googleMap;
                enableUserLocation();

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        enableUserLocation();
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                // If registered Device and preference not empty
                if (!(mHomeLatLngStr.equals("") || mHomeLatLng == null))
                    {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(mHomeLatLng).title("Home"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mHomeLatLng, DEFAULT_ZOOM));
                        addMarker(mHomeLatLng);
                        addCircle(mHomeLatLng, GEOFENCE_RADIUS);
                    }
                else
                {
                    fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null)
                            {
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.addMarker(new MarkerOptions().position(latLng).title("Home"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                            }
                    });
                }
                mMap.setOnMapLongClickListener(this);
            }

        private void enableUserLocation()
            {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    mMap.setMyLocationEnabled(true);
                else //Ask for permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
            {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE)
                    {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            {
                                enableUserLocation();
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                        mMap.setMyLocationEnabled(true);
                    }

                if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE)
                    {
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                            //We have the permission
                            Toast.makeText(this, "Required Permissions Granted - You can now add GeoFences...", Toast.LENGTH_SHORT).show();
                        else
                            //We do not have the permission..
                            Toast.makeText(this, "Background location access is necessary for GeoFences to trigger...", Toast.LENGTH_SHORT).show();
                    }
            }

        @Override
        public void onMapLongClick(@NonNull LatLng latLng)
            {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    handleMapLongClick(latLng);
                else
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            }

        private void handleMapLongClick(LatLng latLng)
            {
                mMap.clear();
                addMarker(latLng);
                addCircle(latLng, GEOFENCE_RADIUS);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(HOME_LATLNG_KEY, latLng.toString());
                editor.apply();
                String returnMsg = addGeofence(this, latLng, GEOFENCE_RADIUS);
                Toast.makeText(this, returnMsg, Toast.LENGTH_SHORT).show();
            }

        public String addGeofence(Activity caller, LatLng latLng, float radius)
            {
                String GEOFENCE_ID = "HOME_GEOFENCE";
                Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
                GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
                PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
                AtomicReference<String> returnMsg = new AtomicReference<>("GeoFence Added");
                if (ActivityCompat.checkSelfPermission(caller, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        enableUserLocation();
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return returnMsg.get();
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

        private void addMarker(LatLng latLng) {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            mMap.addMarker(markerOptions);
        }

        private void addCircle(LatLng latLng, float radius) {


            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.radius(radius);
            circleOptions.strokeColor(Color.argb(255, 255, 0,0));
            circleOptions.fillColor(Color.argb(64, 255, 0,0));
            circleOptions.strokeWidth(4);
            mMap.addCircle(circleOptions);
        }
    }
