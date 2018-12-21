package com.trevorpc.restaurantmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int MY_PERMISSION_REQUEST_LOCATION =  100;
    private GoogleMap map;
    private static final long UPDATE_INTERNAL =5000;
    private static final long FASTEST_INTERVAL = 5000;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        runtimePermissions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLastKnownLocation();
        startLocationUpdate();
        Log.d("TAG", "onCreate: ");
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng starbucks = new LatLng(11,11);
        LatLng panera = new LatLng(11,11);
        LatLng dunkin = new LatLng(11,11);
        LatLng fiveguys = new LatLng(11,11);
        LatLng pizzahut = new LatLng(11,11);
        LatLng atlanta = new LatLng(33.9526,-84.5499);

        map.addMarker(new MarkerOptions().position(atlanta).title("Marker in Marietta"));
        map.addMarker(new MarkerOptions().position(starbucks).title("Marker for Starbucks"));
        map.addMarker(new MarkerOptions().position(dunkin).title("Marker for Dunkin Donuts"));
        map.addMarker(new MarkerOptions().position(fiveguys).title("Marker for Five Guys"));
        map.addMarker(new MarkerOptions().position(pizzahut).title("Marker for Pizza Hut"));
        map.addMarker(new MarkerOptions().position(panera).title("Marker for Panera Bread"));

        map.moveCamera((CameraUpdateFactory.newLatLng(atlanta)));


    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null) {
            LatLng lastLocationLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.addMarker(new MarkerOptions().position(lastLocationLatLng).title("You Are Here"));
            map.moveCamera(CameraUpdateFactory.newLatLng(lastLocationLatLng));
            map.setMinZoomPreference(10);

            Geocoder geocoder = new Geocoder(getApplicationContext());
            try {
                List<Address> geocodeFromLocation = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(),1);
                String address = "1895 The Exchange SE Atlanta GA";
                Log.d("TAG", "onSuccess: " + geocodeFromLocation.get(0).getAddressLine(0));
                List<Address> reverseGeoCode = geocoder.getFromLocationName(address,1);
                String latLng = reverseGeoCode.get(0).getLatitude() + " ! " + reverseGeoCode.get(0).getLongitude();
                Log.d("TAG", "onLocationChanged: " + latLng);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void runtimePermissions() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d("PERMISSION_REQUESTED", "runtimePermissions: Runtime Permission being requested");
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSION_REQUEST_LOCATION);
            }
        }


        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_LOCATION: {
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        getLastKnownLocation();
                        startLocationUpdate();
                    }
                }
            }
            return;
    }
}

    public void getLastKnownLocation() {
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                Log.d("TAG", "onSuccess:" + location.toString());
                                LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                                Log.d("TAG", "onSuccess: " + location.getLatitude());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS Location: ");
                            e.printStackTrace();
                        }
                    });
        }



    }

    protected void startLocationUpdate() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERNAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    // do work here
                    onLocationChanged(locationResult.getLastLocation());
                }
            }, Looper.myLooper());
        }

    }
}