package com.example.deepak.fusedmap;
import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MyService extends Service {

    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null );
    }

    public void getLastLocationFun() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location loc = task.getResult();
                    Toast.makeText(MyService.this, "LastLocation: " + loc.getLatitude() + "\n" + loc.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        getLastLocationFun();
        startLocationUpdates();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MyService.this);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult == null){
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Toast.makeText(MyService.this, "Current Location"+location.getLatitude()+"\n"+location.getLongitude(), Toast.LENGTH_SHORT).show();
                }
            }
        };
        createLocationRequest();
    }

//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Intent i = new Intent(getApplicationContext(),this.getClass());
//        i.setPackage(getPackageName());
//        startService(i);
//        super.onTaskRemoved(rootIntent);
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }
}
