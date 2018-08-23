package com.example.deepak.fusedmap;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ProcessLifecycleOwner;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,TouchableWrapper.UpdateMapAfterUserInterection,SensorEventListener {

    private GoogleMap mMap;
    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    int mHeight;
    int mWidth;
    Location userlocation;
    float bearing =0;
    GeomagneticField geoField;
    float mDeclination;
    private SensorManager mSensorManager;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private Sensor mRotVectSensor;
    private float[] mRotationMatrix = new float[16];
    private ImageView image;
    private double angle;
    boolean flag=false;
    private LatLng oldLocation;
    private static final float ALPHA = 0.5f;


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
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {

                if (location != null) {
                    userlocation = location;
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
        image = (ImageView) findViewById(R.id.imageViewCompass);

        TouchableWrapper touchableMap = findViewById(R.id.touchableMap);
        touchableMap.setListener(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotVectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

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
//                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
//                            .target(currentLocation)
//                            .tilt(67.5f)
//                            .zoom(20)
//                            .bearing(bearing)
//                            .build()
//                    ));   
                    animateMarkerNew(oldLocation,currentLocation);                    
                    geoField = new GeomagneticField(
                            Double.valueOf(userlocation.getLatitude()).floatValue(),
                            Double.valueOf(userlocation.getLongitude()).floatValue(),
                            Double.valueOf(userlocation.getAltitude()).floatValue(),
                            System.currentTimeMillis()
                    );
                    mDeclination = geoField.getDeclination();
                    Log.i("logcheck", String.valueOf(mDeclination));

                    oldLocation=currentLocation;
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
        mWidth= this.getResources().getDisplayMetrics().widthPixels;
        Log.i("Dimensions", String.valueOf(mHeight)+" Height");
        Log.i("Dimensions", String.valueOf(mWidth)+" Width");
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
    // mMap.addMarker(new MarkerOptions().position(newLocation).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
    // Projection projection = mMap.getProjection();
    //center = projection.toScreenLocation(newLocation);
    Point centerOfMap = new Point(mWidth/2,(2*mHeight)/3);
    final float angle = angleBetweenLines(centerOfMap, touchpoint, newTouchpoint);
    if (Math.abs(angle) < 5) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // move the camera (NOT animateCamera() ) to new position with "bearing" updated
                flag=false;
                bearing = mMap.getCameraPosition().bearing - angle;
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                        .target(newLocation)
                        .tilt(67.5f)
                        .zoom(20)
                        .bearing(bearing)
                        .build()
                ));
                image.setRotation(-bearing);
            }
        });
    }
}
catch (NullPointerException n){ if(!checkP()){
    reqP();
    Log.i("logcheck","NullPointer while rotating");
}}
    }


    public void imageClick(View view)
    {
        if(flag==true) {
            bearing=0;
            CameraPosition oldPos = mMap.getCameraPosition();
            CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
            image.setRotation(0);
        }
        flag=!flag;
    }

    private float[] applyLowPassFilter(float[] input, float[] output) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            mRotationMatrix = applyLowPassFilter(event.values, mRotationMatrix);
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            if (Math.abs(Math.toDegrees(orientation[0]) - angle) > 0.8)
            {
                float sensorBearing = (float) Math.toDegrees(orientation[0]) + mDeclination;
                if(flag == true) {
                    updateCamera(sensorBearing);
                }
            }
            angle = Math.toDegrees(orientation[0]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void updateCamera(float bearing2) {
        final LatLng newLocation = new LatLng(userlocation.getLatitude(), userlocation.getLongitude());
        bearing=bearing2;
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                .target(newLocation)
                .tilt(67.5f)
                .zoom(20)
                .bearing(bearing)
                .build()
        ));
        image.setRotation(-bearing);
        Log.i("bearing2", String.valueOf(mMap.getCameraPosition().bearing));
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


    private void animateMarkerNew(final LatLng startPosition, final LatLng destination) {

            final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);
            final LatLngInterpolatorNew latLngInterpolator = new LatLngInterpolatorNew.LinearFixed();

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(2000); // duration 3 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    try {
                        float v = animation.getAnimatedFraction();
                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                .target(newPosition)
                                .tilt(67.5f)
                                .zoom(20)
                                .bearing(bearing)
                                .build()
                        ));
                    } catch (Exception ex) {
                        //I don't care atm..
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                }
            });
            valueAnimator.start();

    }



    private interface LatLngInterpolatorNew {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolatorNew {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this,
                mRotVectSensor,
                SensorManager.SENSOR_STATUS_ACCURACY_LOW);
    }


    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        mSensorManager.unregisterListener(this);
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
