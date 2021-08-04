package com.example.itravel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ActivityShowActivity extends AppCompatActivity {

    TextView name;
    TextView area ;
    TextView time ;
    TextView description ;
    TextView cost ;
    ImageView icon;


    TripActivity activity;

    MapView mMapView;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        // Google Map init
        mMapView =findViewById(R.id.small_map);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap Map) {
                mMap = Map;
                //  建立鏡頭 設定縮放及傾斜角度等
                CameraPosition cameraPosition;
                // 如果使用GooglePlace 直接定位至該處
                if(activity.GetGooglePlaceUsed()) {
                    LatLng latlng = new LatLng(activity.GetPlace().GetLat(),activity.GetPlace().GetLng());
                    cameraPosition = new CameraPosition.Builder().target(latlng).zoom(14).tilt(20).build();
                    BitmapDescriptor _icon = BitmapDescriptorFactory.fromResource(TripActivity.TripActivityIcon[Integer.valueOf(activity.GetIconPath())]);
                    mMap.addMarker(new MarkerOptions().title(area.getText().toString()).position(latlng).icon(_icon));
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                else { // 否則搜尋地點

                    new Thread(new Runnable() { // 子執行緒
                        @Override
                        public void run() {
                            SearchPlace();
                        }
                    }).start();
                }
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMapToolbarEnabled(true);
            }
        });


        Button switch_map_btn = findViewById(R.id.switch_map_btn);
        switch_map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMap.getMapType() == 5)
                    mMap.setMapType(1);
                else
                    mMap.setMapType(mMap.getMapType()+1);
            }
        });
        name = findViewById(R.id.activity_name);
        area = findViewById(R.id.activity_area);
        time = findViewById(R.id.activity_time);
        icon = findViewById(R.id.activity_icon);
        description = findViewById(R.id.activity_description);
        cost = findViewById(R.id.activity_cost);


        // 讀取參數
        Intent intent = getIntent();
        Bundle bundle =intent.getExtras();
        Parcelable data = bundle.getParcelable("activity");
        activity = (TripActivity)data;
        name.setText(activity.GetName());
        area.setText(activity.GetArea());
        time.setText(activity.GetStartTime() + " - " + activity.GetEndTime());
        description.setText(activity.GetDescription());
        cost.setText(String.valueOf(activity.GetCost()) + " NT");

        ImageButton back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        // 初始化上方cover
        LinearLayout upper_layout = findViewById(R.id.upper_layout);
        if(!activity.GetCoverPath().isEmpty()) {

            Uri uri = Uri.parse("content://media"+activity.GetCoverPath());
            File f = new File(getRealPathFromURI(uri));
            Drawable d = Drawable.createFromPath(f.getAbsolutePath());
            upper_layout.setBackground(d);
        }
        else{ // 沒有cover path 就不顯示
            upper_layout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
        }
        // set icon
        icon.setImageResource(TripActivity.TripActivityIcon[ Integer.valueOf(activity.GetIconPath())]);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
    }

    private void SearchPlace(){
        String search_data = "";
        // https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%E9%AB%98%E9%9B%84&inputtype=textquery&language=zh-TW&fields=name,geometry&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c
        try {
            URL url;
            url = new URL("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input="+area.getText()+"&inputtype=textquery&language=zh-TW&fields=name,geometry&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c");
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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(get_place,14));
                    BitmapDescriptor _icon = BitmapDescriptorFactory.fromResource(TripActivity.TripActivityIcon[Integer.valueOf(activity.GetIconPath())]);
                    mMap.addMarker(new MarkerOptions().title(area.getText().toString()).position(get_place).icon(_icon));

                }
            });
        }catch(JSONException e){
            Log.e("Exception",e.getMessage()); // 資料抓取失敗
            e.printStackTrace();
        }

    }
    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
}
