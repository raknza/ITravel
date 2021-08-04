package com.example.itravel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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

public class ActivitiesMapActivity extends AppCompatActivity {

    TripActivity[] activities;
    MarkerOptions markers[];
    MapView mMapView;
    private GoogleMap mMap;
    int data_length;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activities_map);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Parcelable[] data = bundle.getParcelableArray("activities");
        activities = new TripActivity[data.length];
        markers = new MarkerOptions[data.length];
        data_length = 0;
        for(int i = 0; data[i]!=null; i++){
            activities[i] = (TripActivity) data[i];
            data_length++;
        }
        // Google Map init
        mMapView = findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap Map) {
                mMap = Map;
                //  建立鏡頭 設定縮放及傾斜角度等
                CameraPosition cameraPosition;
                for(int i=0;activities[i]!=null;i++) {
                    // 如果使用GooglePlace 直接定位至該處
                    if (activities[i].GetGooglePlaceUsed()) {
                        LatLng latlng = new LatLng(activities[i].GetPlace().GetLat(), activities[i].GetPlace().GetLng());
                        cameraPosition = new CameraPosition.Builder().target(latlng).zoom(14).tilt(20).build();
                        BitmapDescriptor _icon = BitmapDescriptorFactory.fromResource(TripActivity.TripActivityIcon[Integer.valueOf(activities[i].GetIconPath())]);
                        markers[i] = new MarkerOptions().title("活動地點"+(i+1)).snippet(activities[i].GetArea()).position(latlng).icon(_icon);
                        mMap.addMarker(markers[i]);
                        if(i==0)
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        if(i!=0 && markers[i-1]!= null) {
                            Polyline line = mMap.addPolyline(new PolylineOptions()
                                    .add(markers[i].getPosition()).add(markers[i - 1].getPosition())
                                    .width(12)
                                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                    .geodesic(true)
                            );
                        }
                        if(i == data_length-1 && markers[0]!=null){
                            Polyline line = mMap.addPolyline(new PolylineOptions()
                                    .add(markers[i].getPosition()).add(markers[0].getPosition())
                                    .width(12)
                                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                    .geodesic(true)
                            );
                        }

                    } else { // 否則搜尋地點

                        final int finalI = i;
                        new Thread(new Runnable() { // 子執行緒
                            @Override
                            public void run() {
                                SearchPlace(activities[finalI].GetArea(),finalI);
                            }
                        }).start();
                    }
                }
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(true);
            }
        });


        ImageButton back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
    }


    private void SearchPlace(String area,final int i){
        String search_data = "";
        // https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%E9%AB%98%E9%9B%84&inputtype=textquery&language=zh-TW&fields=name,geometry&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c
        try {
            URL url;
            url = new URL("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input="+area+"&inputtype=textquery&language=zh-TW&fields=name,geometry&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c");
            // 設定連線
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            // 輸入設定
            InputStream inputStream = httpURLConnection.getInputStream();
            // 輸入串流
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");
            // 緩衝讀取
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuffer buffer = new StringBuffer();
            String temp = null;
            // 讀取直到為空
            while ((temp = bufferedReader.readLine()) != null) {
                buffer.append(temp);
            }
            search_data = buffer.toString();
            bufferedReader.close();
            reader.close();
            inputStream.close();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 定位至搜尋地區
        try{
            JSONArray jsarr =new JSONObject(search_data).getJSONArray("candidates");
            float lat = Float.parseFloat(jsarr.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat"));
            float lng = Float.parseFloat(jsarr.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng"));
            final LatLng get_place =  new LatLng(lat,lng);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(i == 0)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(get_place,14));
                    BitmapDescriptor _icon = BitmapDescriptorFactory.fromResource(TripActivity.TripActivityIcon[Integer.valueOf(activities[i].GetIconPath())]);
                    markers[i] = new MarkerOptions().title("活動地點"+(i+1)).snippet(activities[i].GetArea()).position(get_place).icon(_icon);
                    mMap.addMarker(markers[i]);
                    // 繪製區域
                    if(i!=0 && markers[i - 1]!=null) {
                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .add(markers[i].getPosition()).add(markers[i - 1].getPosition())
                                .width(12)
                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                .geodesic(true)
                        );
                    }
                    if( (i == data_length-1)){
                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .add(markers[i].getPosition()).add(markers[0].getPosition())
                                .width(12)
                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                .geodesic(true)
                        );
                    }
                    if( (i==0 && markers[data_length-1]!=null)){
                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .add(markers[data_length-1].getPosition()).add(markers[0].getPosition())
                                .width(12)
                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                .geodesic(true)
                        );
                    }

                    if(markers[i+1]!=null){
                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .add(markers[i+1].getPosition()).add(markers[i].getPosition())
                                .width(12)
                                .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                .geodesic(true)
                        );
                    }


                }
            });
        }catch(JSONException e){
            Log.e("Exception",e.getMessage()); // 資料抓取失敗
            e.printStackTrace();
        }

    }
}
