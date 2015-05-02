package com.zhengfood.pointtoanything;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
/**
 * Created by robert on 4/14/15.
 */
public class GPSTracker extends Service implements LocationListener{

    private final Context myContext;

    boolean isGPSEnabled = false;

    boolean isNetworkEnabled = false;

    boolean canGetLocation = false;

    Location location = null;

    double latitude;
    double longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATE = 10;

    private static final long MIN_TIME_BETWEEN_UPDATE = 1000*60*1; // 1 minute

    protected LocationManager locationManager;

    public GPSTracker(Context context){
        this.myContext = context;
        getLocation();
    }

    public Location getLocation(){
        try{
            locationManager = (LocationManager)
                    myContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager.
                    isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager.
                    isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled){
                // Network and GPS are both not available

            }
            else{
                // One of them is available
                this.canGetLocation = true;
                if(isNetworkEnabled){
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATE,
                            MIN_DISTANCE_CHANGE_FOR_UPDATE, this);

                    if(locationManager != null){
                        location = locationManager.
                                getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null){
                            latitude=location.getLatitude();
                            longitude=location.getLongitude();
                        }
                    }
                }


                if(isGPSEnabled){
                    if(location == null){
                        locationManager.requestLocationUpdates(
                                locationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATE,
                                MIN_DISTANCE_CHANGE_FOR_UPDATE, this);

                        Log.d("Network", "Network disabled");
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager !=null){
                            location = locationManager.
                                    getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null){
                                latitude=location.getLatitude();
                                longitude=location.getLongitude();
                            }
                        }

                    }
                }


            }

        } catch (Exception e){
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public void onLocationChanged(Location location) {

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
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean canGetLocation (){
        return canGetLocation;
    }

    public double getLatitude(){
        return location.getLatitude();
    }

    public double getLongitude(){
        return location.getLongitude();

    }
}