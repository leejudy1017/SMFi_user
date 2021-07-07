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
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

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

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;

public class MainActivity  extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private Marker currentMarker = null;
    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 3000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 100; // 0.5초


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    Location mCurrentLocatiion;
    LatLng currentPosition;
    int tracking ;
    ListView listView;


    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    GoogleMap mMap;

    //marker 모음 {"place",[경도,위도]
    HashMap<String, double[]> markerMap = new HashMap<>();
    HashMap<String, double[]> markerDistance = new HashMap<>();

    List<String> nearMarker = new ArrayList<String>();
    myAdapter adapter;
    Double nearlat=0.0;
    Double nearlon=0.0;

    String selectedMarker ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLayout = findViewById(R.id.layout_main);
        listView =findViewById(R.id.listView);
        tracking=0;

        //임의의 안테나값 (서버연동 예정)
        for (int i=1; i<1000;i++){
            markerMap.put("O2I CN "+i, new double[]{Math.random()+37, Math.random()-123});
        }

        //구글 맵 호출
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this); //getMapAsync must be called on the main thread.

    }

    public void onClick(View v) throws IOException {

        switch (v.getId()) {

            case R.id.hotspot:
                adapter = new myAdapter();
                AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater2 = getLayoutInflater();
                View view2 = inflater2.inflate(R.layout.dialog_contect_list, null);
                builder2.setView(view2);
                final ListView listView = view2.findViewById(R.id.listView);
                listView.setAdapter(adapter);

                final Button contactBtn = (Button) view2.findViewById(R.id.contactBtn);
                final Button cancelBtn = (Button) view2.findViewById(R.id.cancelBtn);

                final AlertDialog dialog2 = builder2.create();

                contactBtn.setOnClickListener(v14 -> {
                    if(! (nearlat==0.0 && nearlon==0.0)){
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(nearlat,nearlon));
                        mMap.moveCamera(cameraUpdate);
                    }
                    dialog2.dismiss();
                });

                cancelBtn.setOnClickListener(v16 -> dialog2.dismiss());
                dialog2.show();
                break;

        }
    }

    class myAdapter extends BaseAdapter{


        @Override
        public int getCount() {
            return nearMarker.size();
        }

        @Override
        public Object getItem(int position) {
            return nearMarker.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Button view =new Button(getApplicationContext());
            view.setText(nearMarker.get(position));
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nearlat = markerMap.get(nearMarker.get(position))[0];
                    nearlon = markerMap.get(nearMarker.get(position))[1];
                    Log.i(nearMarker.get(position),"경도: "+markerMap.get(nearMarker.get(position))[0]+" 위도: "+markerMap.get(nearMarker.get(position))[1]);
                }
            });
            return view;
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
        // 구글 맵 객체를 불러온다.
        mMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();
        mMap.setOnMarkerClickListener(this);


        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) { startLocationUpdates(); }
        else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", view -> ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)).show(); }
            else {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE); }
        }


        //정보창 클릭 이벤트 (selected)
        googleMap.setOnInfoWindowClickListener(marker -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = getLayoutInflater();


            if (distance(markerMap.get(marker.getTitle())[0],markerMap.get(marker.getTitle())[1], location.getLatitude(), location.getLongitude(), "kilometer") <= 10) {

                if(!selectedMarker.equals("")&& !selectedMarker.equals(marker.getTitle())){
                    Toast.makeText(MainActivity.this, selectedMarker +"와 연결을 해제 해주세요.", Toast.LENGTH_SHORT).show();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(markerMap.get(selectedMarker)[0],markerMap.get(selectedMarker)[1]));
                    mMap.moveCamera(cameraUpdate);
                }
                else{
                    if (selectedMarker.equals(marker.getTitle())){
                        View view = inflater.inflate(R.layout.dialog_discontect_antenna, null);
                        builder.setView(view);
                        final Button discontactBtn = (Button) view.findViewById(R.id.discontactBtn);
                        final Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
                        final AlertDialog dialog = builder.create();

                        cancelBtn.setOnClickListener(v -> {
                            dialog.dismiss();
                        });

                        discontactBtn.setOnClickListener(v -> {
                            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.abled_antenna);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,180,160,false);
                            selectedMarker = marker.getTitle();
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            selectedMarker="";
                            dialog.dismiss();
                        });
                        dialog.show();
                    }
                    else{
                        View view = inflater.inflate(R.layout.dialog_contect_antenna, null);
                        builder.setView(view);
                        final Button contactBtn = (Button) view.findViewById(R.id.contactBtn);
                        final Button cancelBtn = (Button) view.findViewById(R.id.cancelBtn);
                        final AlertDialog dialog = builder.create();

                        cancelBtn.setOnClickListener(v -> {
                            dialog.dismiss();
                        });

                        contactBtn.setOnClickListener(v -> {
                            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.contect_antenna);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b,180,160,false);
                            selectedMarker = marker.getTitle();
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            dialog.dismiss();
                        });
                        dialog.show();
                    }
                }
            }
            else {
                Toast.makeText(MainActivity.this, "거리가 멀어 접속이 불가능합니다.", Toast.LENGTH_SHORT).show();
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

                String markingPoint = "위도:" + location.getLatitude() + " 경도:" + location.getLongitude();
                Log.d(TAG, "onMarkingResult : " + markingPoint);


                if(tracking==0){
                    //현재 위치에 마커 생성하고 이동
                    setCurrentLocation(location);
                    mCurrentLocatiion = location;


                    for (Map.Entry<String, double[]> elem : markerMap.entrySet()) {
                        LatLng point = new LatLng(elem.getValue()[0], elem.getValue()[1]);
                        String markerSnippet = " 위도: " + elem.getValue()[0] + " 경도: " + elem.getValue()[1];
                        Log.i("markerMap", "key: " + elem.getKey() + markerSnippet);

                        BitmapDrawable bitmapdraw;
                        Bitmap b;
                        Bitmap smallMarker;
                        MarkerOptions markerOptions = new MarkerOptions();
                        if (distance(elem.getValue()[0], elem.getValue()[1], location.getLatitude(), location.getLongitude(), "kilometer") <= 10) {
                            nearMarker.add(elem.getKey());
                            bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.abled_antenna);
                            b = bitmapdraw.getBitmap();
                            smallMarker = Bitmap.createScaledBitmap(b, 180, 160, false);
                        } else {
                            bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.disabled_antenna);
                            b = bitmapdraw.getBitmap();
                            smallMarker = Bitmap.createScaledBitmap(b, 100, 130, false);
                        }

                        markerOptions
                                .position(point)
                                .title(elem.getKey())
                                .snippet(markerSnippet)
                                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                        mMap.addMarker(markerOptions);
                    }

                    tracking=1;
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
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            //내 위치 (파란색 표시)
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

        //현재 위치 마킹 안함
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


        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";


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

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
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

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            } else {

                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();

                } else {

                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }

        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", (dialog, id) -> {
            Intent callGPSSettingIntent
                    = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
        });
        builder.setNegativeButton("취소", (dialog, id) -> dialog.cancel());
        builder.create().show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
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

