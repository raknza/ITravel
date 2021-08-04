package com.example.itravel;

import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

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
import java.util.List;

public class GooglePlaceSearch {
    // json資料
    String data = "";
    JSONObject jsonObj;
    JSONArray jsonArray;
    int data_lenth;
    // GoogleMap
    GoogleMap mMap;
    Activity activity;
    // rootView
    View view;
    // Loading 視窗
    LoadDialog loading_window ;
    //  地點標記
    private MarkerOptions Markers[][];
    private int Marker_num = 0;
    private int search_counts = 0;
    private GooglePlace place[][];

    public GooglePlaceSearch(GoogleMap mMap, Activity activity,LoadDialog loading_window,View view){
        this.mMap = mMap;
        data_lenth = 0;
        this.activity = activity;
        this.loading_window = loading_window;
        this.view = view;
        Markers = new MarkerOptions[100][60];
        place = new GooglePlace[100][60];
    }

    public void SearchPlace(String place){
        String search_data = "";
        // https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%E9%AB%98%E9%9B%84&inputtype=textquery&language=zh-TW&fields=name,geometry&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c
        try {
            URL url;
            url = new URL("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input="+place+"&inputtype=textquery&language=zh-TW&fields=name,geometry&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c");
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

        loading_window.End();

        // 定位至搜尋地區
        try{
            JSONArray jsarr =new JSONObject(search_data).getJSONArray("candidates");
            float lat = Float.parseFloat(jsarr.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lat"));
            float lng = Float.parseFloat(jsarr.getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getString("lng"));
            final LatLng get_place =  new LatLng(lat,lng);
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(get_place,14));
                }
            });
        }catch(JSONException e){
            Log.e("Exception",e.getMessage()); // 資料抓取失敗
            e.printStackTrace();
            Looper.prepare();
            Snackbar.make(view ,"查無此地點", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            Looper.loop();
        }

    }

    // 設定標記
    private void processMarker() {

        //Log.d("json陣列長度",String.valueOf(jsonArray.length()));
        data_lenth += jsonArray.length();
        //Log.d("總資料數量: " ,Integer.toString(num));
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = null;
            float lat = 0;
            float lng = 0;
            String title="";
            String vicinity ="";
            String place_id ="";
            try{
                obj = jsonArray.getJSONObject(i);
                lat = Float.parseFloat(obj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                lng = Float.parseFloat(obj.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                title = obj.getString("name");
                vicinity = obj.getString("vicinity");
                place_id = obj.getString("place_id");
            }catch (JSONException e){

            }
            String photo_ref = "";
            try {
                if(obj!=null)
                    photo_ref = obj.getJSONArray("photos").getJSONObject(0).getString("photo_reference");
            } catch (JSONException e) {

            }
            float rating = 0;
            boolean opened = false;
            int temp_num = Marker_num;
            try {
                // 具有評分的資料
                rating = Float.parseFloat(obj.getString("rating"));
                String opening = (obj.getJSONObject("opening_hours").getString("open_now"));
                String description = "  評分: " + rating + "  營業中: " + (opening == "true" ? "是" : "否");
                opened = opening == "true" ? true : false;
                Markers[search_counts - 1][Marker_num++] = new MarkerOptions().position(new LatLng(lat, lng)).title(title).snippet(description);
            } catch (JSONException e) {
                // 不是否營業中者
                // 具有評分的資料
                try {
                    rating = Float.parseFloat(obj.getString("rating"));
                } catch (JSONException f){}
                //Log.e("Exception try ",e.getMessage());
                String description = "  評分: " + rating;
                Markers[search_counts - 1][Marker_num++] = new MarkerOptions().position(new LatLng(lat, lng)).title(title).snippet(description);
            }
            if (Marker_num == temp_num)
                Markers[search_counts - 1][Marker_num++] = new MarkerOptions().position(new LatLng(lat, lng)).title(title).snippet("");
            place[search_counts - 1][Marker_num - 1] = new GooglePlace(title, lat, lng, rating, opened, vicinity, photo_ref,place_id);
        }


        // 標記
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i;
                for (i = 0; i < Marker_num; i++) {
                    if (Markers[i] != null) {
                        mMap.addMarker(Markers[search_counts - 1][i]);
                    }
                }
            }
        });
    }
    public void SearchNearPlace(final LatLng pos, final String radius, String nextPageToken, final int count, final String place_type){
        // 全部資料抓取完畢
        Boolean download_overed = false;
        try {
            URL url;
            if( nextPageToken == null ||  ( nextPageToken!=null && nextPageToken == "" ) ) {
                url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + pos.latitude + "," + pos.longitude + "&radius=" + radius + "&types=" + place_type + "&language=zh-TW&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c");
                search_counts++;
                Marker_num = 0;
            }
            else
                url = new URL("https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=" + nextPageToken + "&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c");
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
            data = buffer.toString();
            //Log.e("data",data);
            bufferedReader.close();
            reader.close();
            inputStream.close();
            try {
                jsonObj = new JSONObject(data);
            }catch (Exception e){}
            try {
                jsonArray = jsonObj.getJSONArray("results");
            }catch (Exception e){}
            Snackbar.make(view, "完成讀取"+ (jsonArray.length()+count) + "筆資料", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            // Json處理 讀入marker
            processMarker();
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String my_nextPageToken ="" ;
        try{
            my_nextPageToken = jsonObj.getString("next_page_token"); // 抓取下一頁
        }catch (JSONException e) { // 抓取不到下一頁
            download_overed = true; // 讀取結束
            // 關閉讀取中視窗
            loading_window.End();
        }

        final String token = my_nextPageToken;
        // 繼續抓取下一頁
        if(!download_overed) {
            try {
                Thread loadingThread = new Thread(new Runnable() { // 還有資料未讀取則再開執行緒讀取
                    @Override
                    public void run() {
                        SearchNearPlace(pos,radius,token,count+jsonArray.length(),place_type);
                    }
                });
                loadingThread.sleep(2000); // 等待2秒避免抓取太快被google擋住
                loadingThread.start();
            }catch (InterruptedException e) {

            }
        }
    }

    public GooglePlace[] GetPlace(int index){
        return place[index];
    }
    public GooglePlace[] GetPlace(int start,int end){
        GooglePlace[] _place = new GooglePlace[6000];
        int k = 0;
        for(int i=start;i<=end;i++){
            for(int j=0;j<60;j++) {
                if(place[i][j]!=null){
                    _place[k] = place[i][j];
                    k++;
                }
            }
        }
        return _place;
    }
    public GooglePlace[] GetPlace(List<Integer> arr){
        GooglePlace[] _place = new GooglePlace[6000];
        int k = 0;
        for(int i=0;i<arr.size();i++){
            int index = arr.get(i);
            for(int j=0;j<60;j++) {
                if(place[index][j]!=null){
                    _place[k] = place[index][j];
                    k++;
                }
            }
        }
        return _place;
    }

    public int GetPlaceLength(int index){
        int length = 0;
        for(int i=0;i<60;i++){
            if(place[index][i]!=null)
                length++;
        }
        return length;
    }
}
