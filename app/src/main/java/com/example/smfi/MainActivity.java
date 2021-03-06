package com.example.smfi;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private Marker currentMarker = null;
    private View mLayout;  // Snackbar ???????????? ???????????? View??? ???????????????.
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 3000;  // 1???
    private static final int FASTEST_UPDATE_INTERVAL_MS = 100; // 0.5???

    // ?????? ????????? marker
    private Marker connectMarker = null;

    // onRequestPermissionsResult?????? ????????? ???????????? ActivityCompat.requestPermissions??? ????????? ????????? ????????? ???????????? ?????? ???????????????.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // ?????? ???????????? ?????? ????????? ???????????? ???????????????.
    String[] REQUIRED_PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // ?????? ?????????


    Location mCurrentLocatiion;
    LatLng currentPosition;
    int tracking ;
    ArrayList<String> arrayList;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    GoogleMap mMap;
    Button hotspot;
    Button link;

    //marker ?????? {??????, Marker}
    HashMap<String, Marker> markerMap = new HashMap<>();
    HashMap<String, Circle> circleMap = new HashMap<>();

    private Context mContext;
    String text;

    LatLng[] antenna = new LatLng[900];

    SeekBar seekBar;
    TextView seekBar_count;
    int seek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        seekBar =findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if(progress%50==0){
                    seekBar_count.setText(progress+ " m");
                }
                else {
                    seekBar.setProgress((progress/50)*50);
                    seekBar_count.setText((progress/50)*50 + " m");
                    seek = (progress/50)*50;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar_count = findViewById(R.id.seekbar_count);

        int x = 0;
        //????????? ???????????? (???????????? ??????)
        //?????? ?????? ???????????????
        for (int i=0; i<30;i++){
            for(int j=0;j<30;j++){

                //korea
                antenna[x] = new LatLng(37.413294+(0.0100613*i), 126.734086+(0.01784083*j));

                //google
                //antenna[x] = new LatLng(Math.random()+37, Math.random()-123);
                x++;
            }
        }


        text = PreferenceManager.getString(mContext,"rebuild");

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLayout = findViewById(R.id.layout_main);
        tracking=0;
        arrayList = new ArrayList<>();
        hotspot = findViewById(R.id.hotspot);
        hotspot.setEnabled(false);
        link = findViewById(R.id.link);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connectMarker!=null){
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(connectMarker.getPosition());
                    mMap.moveCamera(cameraUpdate);
                }
            }
        });

        //?????? ??? ??????
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //getMapAsync must be called on the main thread.

    }

    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.logout:
                Intent intent = new Intent(getApplication(),LoginActivity.class);
                startActivity(intent);
                finish();
                break;

            case R.id.hotspot:

                if(arrayList.size()!=0){
                    for(int i =0;i<arrayList.size();i++){
                        markerMap.get(arrayList.get(i)).remove();
                        circleMap.get(arrayList.get(i)).remove();
                    }
                    arrayList.clear();
                }

                for(int x=0;x<antenna.length;x++){
                    double temp_distance = distance(antenna[x].latitude,antenna[x].longitude,location.getLatitude(),location.getLongitude(),"meter");
                    if(temp_distance<seek){
                        if(connectMarker!=null && connectMarker.getTitle().equals("ANTENNA "+x)){

                            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker4);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);
                            arrayList.add("ANTENNA "+x);
                            markerMap.put("ANTENNA "+x,mMap.addMarker(new MarkerOptions()
                                    .title("ANTENNA "+x)
                                    .snippet("?????????????????????.")
                                    .position(new LatLng(antenna[x].latitude, antenna[x].longitude))
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));

                            CircleOptions circleOptions =  new CircleOptions().center(markerMap.get("ANTENNA "+x).getPosition())
                                    .radius(500)
                                    .strokeWidth(0f)
                                    .fillColor(Color.parseColor("#880000ff"));

                            circleMap.put("ANTENNA "+x,mMap.addCircle(circleOptions));
                            Toast.makeText(MainActivity.this, "ANTENNA "+x+" ??? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                        }
                        else{
                        BitmapDrawable bitmap = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                        Bitmap b = bitmap.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b,40,40,false);
                        arrayList.add("ANTENNA "+x);
                        markerMap.put("ANTENNA "+x,mMap.addMarker(new MarkerOptions()
                        .title("ANTENNA "+x)
                        .snippet("??????: "+String.format("%.2f",temp_distance)+" m")
                        .position(new LatLng(antenna[x].latitude, antenna[x].longitude))
                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));

                        CircleOptions circleOptions =  new CircleOptions().center(markerMap.get("ANTENNA "+x).getPosition())
                                .radius(500)
                                .strokeWidth(0f)
                                .fillColor(Color.parseColor("#880000ff"));

                        circleMap.put("ANTENNA "+x,mMap.addCircle(circleOptions));
                    }
                    }
                }

                Log.i("?????? ????????? ????????? ??? : ", String.valueOf(arrayList.size()));
                if(arrayList.size()==0){
                    Toast.makeText(MainActivity.this, "????????? ??? ?????? ???????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                }

                break;

        }
    }



    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit == "kilometer") {
            dist = dist * 1.609344;
        } else if (unit == "meter") {
            dist = dist * 1609.344;
        }

        return (dist);
    }

    // This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    // This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        // ?????? ??? ????????? ????????????.
        mMap = googleMap;

        //????????? ????????? ?????? ??????????????? GPS ?????? ?????? ???????????? ???????????????
        //????????? ??????????????? ????????? ??????
        setDefaultLocation();
        mMap.setOnMarkerClickListener(this);


        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { startLocationUpdates(); }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.",
                        Snackbar.LENGTH_INDEFINITE).setAction("??????", view -> ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)).show(); }
            else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE); }
        }


        //????????? ?????? ????????? (selected)
        googleMap.setOnInfoWindowClickListener(marker -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();


            if (distance(markerMap.get(marker.getTitle()).getPosition().latitude,markerMap.get(marker.getTitle()).getPosition().longitude, location.getLatitude(), location.getLongitude(), "meter") <= seek) {

                if(connectMarker!=null && connectMarker.getTitle().equals(marker.getTitle())){

                    //????????????????????????
                    View view = inflater.inflate(R.layout.dialog_discontect_antenna, null);
                    builder.setView(view);
                    final Button discontactBtn = (Button) view.findViewById(R.id.discontactBtn);
                    final Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
                    final AlertDialog dialog = builder.create();

                    cancelBtn.setOnClickListener(v -> {
                        dialog.dismiss();
                    });

                    discontactBtn.setOnClickListener(v -> {
                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker3);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b,40,40,false);
                        String markingPoint = "??????:" + String.format("%.4f",marker.getPosition().latitude)+ " ??????:" + String.format("%.4f",marker.getPosition().longitude);
                        marker.setSnippet(markingPoint);
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                        connectMarker=null;
                        dialog.dismiss();
                    });
                    dialog.show();
                }
                else{

                    //????????????????????????
                    if(connectMarker==null){
                        View view = inflater.inflate(R.layout.dialog_contect_antenna, null);
                        builder.setView(view);
                        final Button contactBtn = (Button) view.findViewById(R.id.contactBtn);
                        final Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
                        final AlertDialog dialog = builder.create();

                        cancelBtn.setOnClickListener(v -> {
                            dialog.dismiss();
                        });

                        contactBtn.setOnClickListener(v -> {

                            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker4);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);

                            marker.setSnippet("?????????????????????.");
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                            connectMarker = marker;

                            Toast.makeText(MainActivity.this, marker.getTitle()+" ??? ?????????????????????.", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
                        dialog.show();
                    }

                    //?????? ??? ??????
                    else{

                        View view = inflater.inflate(R.layout.dialog_discontect, null);
                        builder.setView(view);
                        final TextView text = view.findViewById(R.id.text);
                        text.setText(connectMarker.getTitle()+" ?????? ????????? ????????????????????? ?");

                        final Button discontactBtn = (Button) view.findViewById(R.id.discontactBtn);
                        final Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
                        final AlertDialog dialog = builder.create();

                        cancelBtn.setOnClickListener(v -> {
                            dialog.dismiss();
                        });

                        discontactBtn.setOnClickListener(v -> {

                            //?????? connection ??????
                            markerMap.get(connectMarker.getTitle()).remove();

                            double temp_distance = distance(connectMarker.getPosition().latitude, connectMarker.getPosition().longitude,location.getLatitude(),location.getLongitude(),"meter");

                            if(temp_distance<seek) {
                                //?????? connection ??????
                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker3);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 40, 40, false);
                                String markingPoint = "??????:" + String.format("%.4f", connectMarker.getPosition().latitude) + " ??????:" + String.format("%.4f", connectMarker.getPosition().longitude);

                                markerMap.put(connectMarker.getTitle(), mMap.addMarker(new MarkerOptions()
                                        .title(connectMarker.getTitle())
                                        .snippet("??????: " + String.format("%.2f", temp_distance) + " m")
                                        .position(new LatLng(connectMarker.getPosition().latitude, connectMarker.getPosition().longitude))
                                        .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));

                                Toast.makeText(MainActivity.this, connectMarker.getTitle()+" ??? ????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                            }
                            //????????? connection ??????
                            connectMarker = marker;
                            markerMap.get(marker.getTitle()).remove();

                            BitmapDrawable bitmapdraw2 = (BitmapDrawable)getResources().getDrawable(R.drawable.marker4);
                            Bitmap b2 = bitmapdraw2.getBitmap();
                            Bitmap smallMarker2 = Bitmap.createScaledBitmap(b2,90,150,false);

                            markerMap.put(connectMarker.getTitle(), mMap.addMarker(new MarkerOptions()
                                    .title(connectMarker.getTitle())
                                    .snippet("?????????????????????.")
                                    .position(new LatLng(connectMarker.getPosition().latitude, connectMarker.getPosition().longitude))
                                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker2))));

                            dialog.dismiss();
                        });
                        dialog.show();

                    }

                }
            }
            else {
                Toast.makeText(MainActivity.this, "????????? ?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markingPoint = "??????:" + String.format("%.4f",location.getLatitude()) + " ??????:" + String.format("%.4f",location.getLongitude());
                Log.d(TAG, "onMarkingResult : " + markingPoint);

                if(connectMarker!=null){
                    PreferenceManager.setString(mContext,"rebuild",connectMarker.getTitle());
                    //????????? ???????????? ????????????
                    link.setText("????????? SMART HOT SPOT : "+connectMarker.getTitle());

                    if(distance(connectMarker.getPosition().latitude,connectMarker.getPosition().longitude,location.getLatitude(),location.getLongitude(),"meter")>seek){
                        Toast.makeText(MainActivity.this, connectMarker.getTitle()+" ??? ????????? ???????????? \n????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                        PreferenceManager.setString(mContext,"rebuild","");

                        //????????? connection ??????
                        BitmapDrawable bitmapdraw2 = (BitmapDrawable)getResources().getDrawable(R.drawable.marker3);
                        Bitmap b2 = bitmapdraw2.getBitmap();
                        Bitmap smallMarker2 = Bitmap.createScaledBitmap(b2,40,40,false);
                        String markingPoint2 = "??????:" + String.format("%.4f",connectMarker.getPosition().latitude) + " ??????:" + String.format("%.4f",connectMarker.getPosition().longitude);

                        markerMap.get(connectMarker.getTitle()).remove();

                        markerMap.put(connectMarker.getTitle(), mMap.addMarker(new MarkerOptions()
                                .title(connectMarker.getTitle())
                                .snippet(markingPoint2)
                                .position(new LatLng(connectMarker.getPosition().latitude, connectMarker.getPosition().longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker2))));

                        connectMarker=null;
                        link.setText("");
                    }
                }
                else{
                    link.setText("");
                }


                if(tracking==0){
                    //?????? ????????? ?????? ???????????? ??????
                    setCurrentLocation(location);
                    mCurrentLocatiion = location;
                    tracking=1;
                    hotspot.setEnabled(true);

                    if(!text.equals("")){

                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker4);
                        Bitmap b = bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b,90,150,false);
                        arrayList.add(text);
                        markerMap.put(text,mMap.addMarker(new MarkerOptions()
                                .title(text)
                                .snippet("?????????????????????.")
                                .position(new LatLng(antenna[(int) Double.parseDouble(text.split(" ")[1])].latitude, antenna[(int) Double.parseDouble(text.split(" ")[1])].longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))));

                        CircleOptions circleOptions =  new CircleOptions().center(markerMap.get(text).getPosition())
                                .radius(500)
                                .strokeWidth(0f)
                                .fillColor(Color.parseColor("#880000ff"));

                        circleMap.put(text,mMap.addCircle(circleOptions));
                        //Toast.makeText(MainActivity.this, text+" ??? ???????????? ????????????", Toast.LENGTH_SHORT).show();

                        connectMarker = markerMap.get(text);
                        Log.i("pre DATA: ",text);
                        Log.i("pre DATA: ", connectMarker.toString());
                    }
                    else{
                        Log.i("pre DATA: ","????????? ????????? ???????????? ????????????.");
                    }
                }
            }

        }

    };


    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        } else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED || hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocationUpdates : ????????? ???????????? ??????");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            //??? ?????? (????????? ??????)
            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }
    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location) {
        if (currentMarker != null) currentMarker.remove();
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //?????? ?????? ?????? ??????
        //MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(currentLatLng);
        //markerOptions.title(markerTitle);
        //markerOptions.snippet(markerSnippet);
        //markerOptions.draggable(true);
        //currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
    }


    public void setDefaultLocation() {


        //????????? ??????, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "???????????? ????????? ??? ??????";
        String markerSnippet = "?????? ???????????? GPS ?????? ?????? ???????????????";


        if (currentMarker != null) currentMarker.remove();

        //MarkerOptions markerOptions = new MarkerOptions();
        //markerOptions.position(DEFAULT_LOCATION);
        //markerOptions.title(markerTitle);
        //markerOptions.snippet(markerSnippet);
        //markerOptions.draggable(true);
        //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        //currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }

    //??????????????? ????????? ????????? ????????? ?????? ????????????
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        return false;

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // ?????? ????????? PERMISSIONS_REQUEST_CODE ??????, ????????? ????????? ???????????? ??????????????????
            boolean check_result = true;

            // ?????? ???????????? ??????????????? ???????????????.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {
                // ???????????? ??????????????? ?????? ??????????????? ???????????????.
                startLocationUpdates();
            } else {

                // ????????? ???????????? ????????? ?????? ????????? ??? ?????? ????????? ??????????????? ?????? ???????????????.2 ?????? ????????? ????????????.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // ???????????? ????????? ????????? ???????????? ?????? ?????? ???????????? ????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {

                    // "?????? ?????? ??????"??? ???????????? ???????????? ????????? ????????? ???????????? ??????(??? ??????)?????? ???????????? ???????????? ?????? ????????? ??? ????????????.
                    Snackbar.make(mLayout, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("??????", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    //??????????????? GPS ???????????? ?????? ????????????
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", (dialog, id) -> {
            Intent callGPSSettingIntent
                    = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
        });
        builder.setNegativeButton("??????", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS ????????? ?????????");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

}

