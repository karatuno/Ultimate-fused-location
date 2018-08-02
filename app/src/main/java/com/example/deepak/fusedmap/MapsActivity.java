package com.example.deepak.fusedmap;

import android.Manifest;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,TouchableWrapper.UpdateMapAfterUserInterection {

    private GoogleMap mMap;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    Point center;
    int mHeight;
    Location userlocation;
    float bearing =0;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private void startLocationUpdates() {

        Log.i("logcheck","startLocationUpdates");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},99);

            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null );
    }

    public void getLastLocationFun() {

        Log.i("logcheck","getLastLocationFun");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Not permitted", Toast.LENGTH_SHORT).show();
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    userlocation = task.getResult();
                    mMap.clear();
                    LatLng currentLocation = new LatLng(userlocation.getLatitude(), userlocation.getLongitude());
               //     mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location"));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                            .target(currentLocation)
                            .tilt(67.5f)
                            .zoom(20)
                            .bearing(bearing)
                            .build()
                    ));
                }
            }
        });

    }

    public void reqP(){

        Log.i("logcheck","reqP");
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                99);

    }

    public boolean checkP(){
        Log.i("logcheck","checkP");
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void stopLocationUpdates() {
        Log.i("logcheck","stopLocationUpdates");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void createLocationRequest() {
        Log.i("logcheck","createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("logcheck","onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createLocationRequest();


        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if(locationResult == null){
                    Log.i("logcheck","onLocationResult null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {

                    // Add a marker on current location and move the camera
                    Log.i("logcheck","LocationUpdates");
                    mMap.clear();
                    userlocation=location;
                    LatLng currentLocation = new LatLng(userlocation.getLatitude(), userlocation.getLongitude());
      //              mMap.addMarker(new MarkerOptions().position(currentLocation).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                            .target(currentLocation)
                            .tilt(67.5f)
                            .zoom(20)
                            .bearing(bearing)
                            .build()
                    ));
                }
            }
        };

        if (Build.VERSION.SDK_INT < 23)
        {
            OKBuilder();
        }
        else {
            if (!checkP()) {
                reqP();
            }

            if (checkP()) {
                OKBuilder();
            }

        }
        startLocationUpdates();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());
        mHeight= this.getResources().getDisplayMetrics().heightPixels;
    }

    private void OKBuilder()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                getLastLocationFun();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
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
    public void onMapReady(GoogleMap googleMap) {
        Log.i("logcheck","onMapReady");
        mMap = googleMap;
        mMap.setPadding(0,mHeight/3,0,0);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setBuildingsEnabled(false);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.json));

            if (!success) {
                Log.e("", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("", "Can't find style. Error: ", e);
        }

    }

    @Override
    protected void onDestroy() {
        Log.i("logcheck","onDestroy");
        super.onDestroy();
        stopLocationUpdates();
    }


    @Override
    public void onUpdateMapAfterUserInterection(Point touchpoint, Point newTouchpoint) {
        Log.i("logcheck","onUpdateMapAfterUserInterection");
try {
    Log.i("logcheck","trying to rotate");
    final LatLng newLocation = new LatLng(userlocation.getLatitude(), userlocation.getLongitude());
  //  mMap.addMarker(new MarkerOptions().position(newLocation).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
    Projection projection = mMap.getProjection();
    center = projection.toScreenLocation(newLocation);
    Point centerOfMap = center;
    final float angle = angleBetweenLines(centerOfMap, touchpoint, newTouchpoint);


    if (Math.abs(angle) < 5) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // move the camera (NOT animateCamera() ) to new position with "bearing" updated
                bearing = mMap.getCameraPosition().bearing - angle;
                Log.i("bearing", String.valueOf(bearing));
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                        .target(newLocation)
                        .tilt(67.5f)
                        .zoom(20)
                        .bearing(bearing)
                        .build()
                ));

                Log.i("bearing", String.valueOf(mMap.getCameraPosition().bearing));
            }
        });
    }
}
catch (NullPointerException n){ if(!checkP()){
    reqP();
    Log.i("logcheck","NullPointer while rotating");
}}
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("LogCheck","onRequestPermissionsResult");

        if (requestCode != 0) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }

                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback,
                        null);
                OKBuilder();
            }
        }
    }

    public float angleBetweenLines(Point center,Point endLine1,Point endLine2){
        float a = endLine1.x - center.x;
        float b = endLine1.y - center.y;
        float c = endLine2.x - center.x;
        float d = endLine2.y - center.y;

        float atan1 = (float) Math.atan2(a,b);
        float atan2 = (float) Math.atan2(c,d);

        return (float) ((atan1 - atan2) * 180 / Math.PI);
    }

    public class AppLifecycleListener implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onMoveToForeground() {
            // app moved to foreground
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onMoveToBackground() {
            Toast.makeText(MapsActivity.this, "LifeCycleEvent", Toast.LENGTH_SHORT).show();
            startService(new Intent(MapsActivity.this, MyService.class));
        }
    }
}
