package com.ziegler.hereiam;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Gabriel on 27/08/2016.
 */
public class LocationManager extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // Google client to interact with Google API
    private static GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    LocationSettingReceiver locationSettingReceiver;
    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 20000; // 10 sec
    private static int FATEST_INTERVAL = 10000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = true;
    private static String TAG = "LOCATIONMANAGER";

    public LocationManager() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationSettingReceiver = new LocationSettingReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.ziegler.utracker.CHANGE_SETTINGS");
        registerReceiver(locationSettingReceiver, filter);
        createLocationRequest();
        buildGoogleApiClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        Log.d(TAG, "Service Started");
        // Let it continue running until it is stopped.
        Toast.makeText(this, "Location Started", Toast.LENGTH_LONG).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        unregisterReceiver(locationSettingReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

        FirebaseUtil.postLocation(getLastLocation());
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location: " + location.toString());
        FirebaseUtil.postLocation(location);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        Log.d(TAG, "Creating Local Request");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT); // 10 meters
    }

    protected void startLocationUpdates() {
        Log.d(TAG, "Requesting Location Updates");
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, e.getMessage());
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    public static Location getLastLocation() {
        try {
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return mLastLocation;
        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        }
        return null;
    }

    class LocationSettingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("KEY_ONLINE")) {
                mRequestingLocationUpdates = intent.getBooleanExtra("KEY_ONLINE", false);
                if (mRequestingLocationUpdates)
                    startLocationUpdates();
                else
                    stopLocationUpdates();
            }
        }
    }
}