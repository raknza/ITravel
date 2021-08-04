package com.example.itravel.ui.home;

import android.app.Dialog;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;

import com.example.itravel.GooglePlace;
import com.example.itravel.PlaceActivity;
import com.example.itravel.Trip;
import com.example.itravel.TripMainActivity;
import com.example.itravel.trip_create;
import com.github.florent37.shapeofview.shapes.ArcView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.itravel.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SENSOR_SERVICE;

public class HomeFragment extends Fragment implements SensorEventListener {

    private HomeViewModel homeViewModel;
    private SensorManager mSensorManager;
    private float[] gravity = new float[3];

    private Trip my_trips[];
    int data_length;
    ListView list;

    int icon = R.drawable.ic_menu_myplace;
    ImageView BackImage;
    OnScrollListenerImpl scrollListener;
    View root;

    Dialog delete_dialog;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        InitTrips();
        // Set Fab Button
        FloatingActionButton fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getActivity(), trip_create.class);
                // 參速傳送
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        // 刪除旅遊計畫視窗
        InitDialog();
        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
        InitTrips();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if(BackImage== null) {
            return;
        }
        //判斷感測器類別
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER: //加速度感測器
                final float alpha = (float) 0.8;
                gravity[0] = alpha * gravity[0] +  (1 - alpha) * event.values[0];
                gravity[1] = alpha * gravity[1]  + (1 - alpha) * event.values[1];
                gravity[2] = alpha * gravity[2]  + (1 - alpha) * event.values[2];

                break;
            case Sensor.TYPE_GYROSCOPE://重力感測器
                gravity[0] = event.values[0];//單位m/s^2
                gravity[1] = event.values[1];
                gravity[2] = event.values[2];
                int delta_x = BackImage.getScrollX()+ (int)(gravity[1] * -2);
                int delta_y = BackImage.getScrollY()+ (int)(gravity[0] * -2) ;
                if(delta_x < 0)
                    delta_x = 0;
                if(delta_y < 0)
                    delta_y = 0;
                if(delta_x > 1900)
                    delta_x = 1900;
                if(delta_y > 1200)
                    delta_y = 1200;
                BackImage.setScrollX(delta_x);
                BackImage.setScrollY(delta_y);
                break;
            default:
                break;
        }
    }
    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    class OnScrollListenerImpl implements AbsListView.OnScrollListener {

        ListView listview;
        View home_cover_layout_first;
        View home_cover_layout_third;
        LinearLayout.LayoutParams layoutParams_first;
        LinearLayout.LayoutParams layoutParams_third;

        int lastState = 0;
        int scrollStates;
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            scrollStates = scrollState;
        }
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastInScreen = firstVisibleItem + visibleItemCount-1;
            // smooth need
            if(lastInScreen == 1  ) {
                if(home_cover_layout_first == null) {
                    View root_LinearLayout = getViewByPosition(0, list);
                    View home_cover_layout = root_LinearLayout.findViewById(R.id.HomeCoverLayout);
                    home_cover_layout_first = home_cover_layout;
                }
                if(layoutParams_first == null){
                    float scale = getResources().getDisplayMetrics().density;
                    int dpWidthInPx = home_cover_layout_first.getWidth();
                    int dpHeightInPx = (int) (250 * scale);
                    layoutParams_first = new LinearLayout.LayoutParams(dpWidthInPx,dpHeightInPx);
                }
                home_cover_layout_first.setLayoutParams(layoutParams_first);
                if(lastState == 1) {
                    View c = list.getChildAt(0);
                    int scrolly = -c.getTop() + list.getFirstVisiblePosition() * c.getHeight();
                    //list.setScrollY(scrolly);
                }
                lastState = 0;

            }
            else if ( lastInScreen == 3 ){
                if(home_cover_layout_third == null) {
                    View root_LinearLayout = getViewByPosition(lastInScreen, list);
                    View home_cover_layout = root_LinearLayout.findViewById(R.id.HomeCoverLayout);
                    home_cover_layout_third = home_cover_layout;
                }
                if(layoutParams_third == null){
                    float scale = getResources().getDisplayMetrics().density;
                    int dpWidthInPx = home_cover_layout_first.getWidth();
                    int dpHeightInPx = (int) (0 * scale);
                    layoutParams_third = new LinearLayout.LayoutParams(dpWidthInPx,dpHeightInPx);
                }
                home_cover_layout_third.setLayoutParams(layoutParams_third);
                lastState = 1;
            }
            // image set
            for(int i=firstVisibleItem;i<=lastInScreen;i++) {
                if( my_trips[i].GetInternalCover()== false && my_trips[i].GetCoverPath()!=null && !my_trips[i].GetCoverPath().isEmpty() ){
                    // get ImageView
                    Log.d("cover_path",my_trips[i].GetCoverPath());
                    ImageView img;
                    View root_LinearLayout = getViewByPosition(i, list);
                    img = root_LinearLayout.findViewById(R.id.trip_cover);
                    Uri uri = Uri.parse("content://media"+my_trips[i].GetCoverPath());
                    if(img!=null) {
                        img.setImageURI(uri);
                    }
                    else
                        Log.d("cover_img","null ?");
                }
            }
        }
    }

    private void InitTrips(){
        my_trips = new Trip[1000];
        data_length = 0;
        // 讀取收藏地點檔案
        FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput("my_trips");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            int i = 0;
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
                my_trips[i] = Trip.LoadFile(getActivity().getApplicationContext(),text);
                i++;
                data_length++;
            }
            //Log.e("log",sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        InitList();
    }

    private void InitList(){
        // Set ListView
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        for(int i=0;i<data_length;i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("trip_title",  my_trips[i].GetTitle());
            hm.put("trip_area", my_trips[i].GetCountry());
            String date = my_trips[i].GetStartYear() +"-" +my_trips[i].GetStartMonth()+"-" +my_trips[i].GetStartDay()+" - " ;
            date += (my_trips[i].GetEndYear() +"-" +my_trips[i].GetEndMonth()+"-" +my_trips[i].GetEndDay());
            hm.put("trip_date",date);
            if(my_trips[i].GetInternalCover() ) {
                hm.put("trip_cover", Integer.toString(Trip.flags[Integer.valueOf(my_trips[i].GetCoverPath())]));
            }
            hm.put("icon", Integer.toString(icon) );
            if(i == 0)
                hm.put("TripHomeBackView",Integer.toString(R.drawable.taouyun));
            aList.add(hm);
        }
        String[] from = {"TripHomeBackView","trip_title","trip_date", "trip_cover","icon","trip_area" };
        int[] to = { R.id.TripHomeBackView,R.id.trip_title,R.id.trip_date,R.id.trip_cover,R.id.icon,R.id.trip_area};
        list = (ListView)root.findViewById(R.id.listView1);
        SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), aList, R.layout.listview_layout, from, to);
        list.setAdapter(adapter);

        // set TripHomeBackView
        list.post(new Runnable(){
            @Override
            public void run() {
                if(data_length> 0) {
                    View root_LinearLayout = getViewByPosition(0, list);
                    View home_cover_layout = root_LinearLayout.findViewById(R.id.HomeCoverLayout);
                    ImageView trip_home_backview = root_LinearLayout.findViewById(R.id.TripHomeBackView);
                    float scale = getResources().getDisplayMetrics().density;
                    int dpWidthInPx = home_cover_layout.getWidth();
                    int dpHeightInPx = (int) (250 * scale);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dpWidthInPx, dpHeightInPx);
                    home_cover_layout.setLayoutParams(layoutParams);
                    BackImage = trip_home_backview;
                    BackImage.setAlpha(0.95f);
                    BackImage.setScrollX(1900);
                    BackImage.setScrollY(1200);
                    scrollListener = new OnScrollListenerImpl();
                    list.setOnScrollListener(scrollListener);
                    scrollListener.listview = list;
                }
            }
        });

        // Set Sensor
        mSensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        // 長按刪除
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                delete_dialog.show();
                selected_trip = i;
                return true;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent= new Intent();
                intent.setClass(getActivity(), TripMainActivity.class);
                // 參速傳送
                Bundle b = new Bundle();
                b.putParcelable("my_trip", my_trips[i]);
                intent.putExtras(b);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);

            }
        });
    }

    int selected_trip ;
    private  void InitDialog(){
        delete_dialog = new Dialog(getContext());
        delete_dialog.setContentView(R.layout.delete_trip);
        Button delete_btn = (Button)delete_dialog.findViewById(R.id.btn_delete);
        Button cancel_btn = (Button)delete_dialog.findViewById(R.id.btn_cancel);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String _my_trips = "";
                // 讀取原旅遊計畫檔案
                FileInputStream fis = null;
                try {
                    fis = getActivity().openFileInput("my_trips");
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;
                    while ((text = br.readLine()) != null) {
                        sb.append(text).append("\n");
                    }
                    _my_trips = sb.toString();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }  finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 刪除旅遊計畫
                _my_trips = _my_trips.replace(my_trips[selected_trip].GetTripID()+"\n","");
                // 輸出檔案
                FileOutputStream fos = null;
                try {
                    fos = getActivity().openFileOutput("my_trips", getActivity().MODE_PRIVATE);
                    fos.write(_my_trips.getBytes());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                delete_dialog.dismiss();
                InitTrips();
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_dialog.dismiss();
            }
        });

    }

}
