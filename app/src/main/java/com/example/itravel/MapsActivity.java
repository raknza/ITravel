package com.example.itravel;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;


import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapsActivity extends Fragment  {

    MapView mMapView;
    private GoogleMap mMap;

    public static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;
    // 預設地點
    private LatLng Taiwan = new LatLng( 23.973872, 120.982022);
    // 搜尋範圍圓形
    private Circle root_circle;
    // Loading 視窗
    LoadDialog loading_window ;
    View rootView;
    GooglePlaceSearch google_search;
    //  搜索點標記
    private MarkerOptions Markers[];
    // 數量
    int count;
    // 清單選取
    List<Integer> select_list = new ArrayList<Integer>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.activity_maps, container, false);
        // Google Map init
        mMapView = rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        loading_window = new LoadDialog(getActivity());
        loading_window.start();
        GoogleMapInit();
        ButtonsInit();
        Markers = new MarkerOptions[1000];
        count = 0;
        return rootView;
    }

    private void GoogleMapInit(){
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap Map) {
                mMap = Map;
                enableMyLocationIfPermitted();
                //  建立鏡頭 設定縮放及傾斜角度等
                CameraPosition cameraPosition = new CameraPosition.Builder().target(Taiwan).zoom(8).tilt(20).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(true);
                InitAddMarkerDialog();
                InitMapClick(mMap);
                google_search = new GooglePlaceSearch(mMap, getActivity(),loading_window,rootView);
            }
        });
    }

    private void ButtonsInit(){
        Button search_btn = rootView.findViewById(R.id.search_button);
        Button switch_btn = rootView.findViewById(R.id.switch_button);
        final EditText search_place = rootView.findViewById(R.id.PlaceInput);
        // 地區搜尋定位
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() { // 子執行緒
                    @Override
                    public void run() {
                        // 關閉虛擬鍵盤
                        hideKeyboardFrom(getContext(),getView());
                        // Loading視窗
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loading_window.ReSet();
                            }
                        });
                        google_search.SearchPlace(search_place.getText().toString());


                    }
                }).start();
            }
        });
        // 切換成清單
        switch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                select_list = new ArrayList<Integer>();
                switch_dialog.show();
                List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
                for(int i=0;i<count;i++) {
                    HashMap<String, String> hm = new HashMap<String, String>();
                    int length = google_search.GetPlaceLength(i);
                    hm.put("item_name",Markers[i].getTitle() + " - " + Markers[i].getSnippet() );
                    hm.put("data_length",length+ "筆資料");
                    aList.add(hm);
                }
                String[] from = {"item_name","data_length"};
                int[] to = { R.id.item_name,R.id.data_length};
                SimpleAdapter adapter = new SimpleAdapter(getActivity().getApplicationContext(), aList, R.layout.list_view_radio, from, to);
                listView.setAdapter(adapter);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    // 探索搜尋視窗
    Dialog dialog;
    EditText title,radius;
    Button add,cancel;
    Spinner spinner;

    // 切換列表視窗
    Dialog switch_dialog;
    Button switch_decide,switch_cancel;
    ListView listView;

    private void InitAddMarkerDialog(){
        dialog = new Dialog(getContext());
        dialog.setTitle("Add marker!");
        dialog.setContentView(R.layout.dialog_add);
        title = (EditText)dialog.findViewById(R.id.title);
        add = (Button)dialog.findViewById(R.id.btn_add);
        cancel = (Button)dialog.findViewById(R.id.btn_cancel);
        spinner = dialog.findViewById(R.id.spinner);
        radius = dialog.findViewById(R.id.radius);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        InitSwitchToList();
    }

    private void InitSwitchToList(){
        switch_dialog = new Dialog(getContext());
        switch_dialog.setTitle("Switch to List");
        switch_dialog.setContentView(R.layout.search_point_select);
        listView = (ListView) switch_dialog.findViewById(R.id.list_view);
        switch_decide = (Button)switch_dialog.findViewById(R.id.decide);
        switch_cancel = (Button)switch_dialog.findViewById(R.id.cancel);
        switch_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 按下確定（切換）
        switch_decide.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                switch_dialog.dismiss();
                Intent intent = new Intent();
                intent.setClass(getActivity(),PlaceActivity.class);
                // 參速傳送
                Bundle b = new Bundle();
                b.putParcelableArray("Google_Place", google_search.GetPlace(select_list));
                intent.putExtras(b);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

            }
        });
        // 取消
        switch_cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                switch_dialog.dismiss();
            }
        });
        // 切換列表時 勾選搜索點
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageView check_box = (ImageView) view.findViewById(R.id.check_box);
                if (check_box.getTag().equals("off")) {
                    check_box.setBackgroundResource(R.drawable.checked);
                    select_list.add(i);
                    Log.e("check","set");
                    check_box.setTag("on");
                }

                else {
                    check_box.setBackgroundResource(R.drawable.check);
                    select_list.remove(Integer.valueOf(i));
                    Log.e("check","not set");
                    check_box.setTag("off");
                }
            }
        });
    }

    private void InitMapClick(final GoogleMap googleMap){
        // 點擊地圖
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(final LatLng latLng){
                // 開啟探索點設定視窗
                dialog.show();
                add.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        // 關閉虛擬鍵盤
                        hideKeyboardFrom(getContext(),getView());
                        if(radius.getText().toString() == null || radius.getText().toString().isEmpty()){
                            Snackbar.make(getView(), "請輸入半徑", Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                            dialog.dismiss();
                            return;
                        }
                        // 開啟Loading視窗
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loading_window.ReSet();
                            }
                        });
                        // 開始搜尋
                        new Thread(new Runnable() { // 子執行緒
                            @Override
                            public void run() {
                                // 關閉虛擬鍵盤
                                hideKeyboardFrom(getContext(),getView());
                                google_search.SearchNearPlace(latLng,radius.getText().toString(),null,0,spinner.getSelectedItem().toString());
                            }
                        }).start();
                        // 繪製搜尋範圍
                        DrawSearchShape(latLng,Float.parseFloat(radius.getText().toString()));
                        // 點擊位置標記
                        Markers[count] = new MarkerOptions().position(latLng).title(title.getText().toString()).snippet("類型: "+spinner.getSelectedItem().toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.map_flag2));
                        mMap.addMarker(Markers[count]);
                        count++;
                        dialog.dismiss();


                    }
                });
                // 取消
                cancel.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    // 繪製搜索範圍
    private void DrawSearchShape(LatLng pos,float radius){
        root_circle = mMap.addCircle( (new CircleOptions() )
                .center(pos)
                .radius(radius)
                .strokeWidth(10)
                .strokeColor(Color.argb(30,255,0,0))
                .fillColor(Color.argb(18,255,0,0))
        );
        root_circle.setVisible(true);
    }

    // 詢問取得定位權限
    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void showDefaultLocation() {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Taiwan));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocationIfPermitted();
                } else {
                    showDefaultLocation();
                }
                return;
            }

        }
    }


    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}