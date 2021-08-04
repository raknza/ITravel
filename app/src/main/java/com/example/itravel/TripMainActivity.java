package com.example.itravel;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class TripMainActivity extends AppCompatActivity {

    Trip my_trip;
    int day_length,selected_day;
    int btn_drawable[] = {
            R.drawable.selector_button_primary,
            R.drawable.selector_button_success,
            R.drawable.selector_button_warning,
            R.drawable.selector_button_info
    };

    int btn_style[] = {
            R.style.Button_Primary,
            R.style.Button_Success,
            R.style.Button_Warning,
            R.style.Button_Info
    };
    int data_length;
    TripActivity[] activities;
    ListView list;
    int selected_activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_main);
        // 讀取參數
        Bundle bundle = getIntent().getExtras();
        Parcelable data = bundle.getParcelable("my_trip");
        my_trip = (Trip)data;
        // 初始化參數
        day_length = 1;
        selected_day = 1;
        InitTextView();
        InitButton();
        InitActivity();
        // 初始化上方cover
        LinearLayout upper_layout = findViewById(R.id.upper_layout);
        if(my_trip.GetInternalCover()) {
            upper_layout.setBackgroundResource(Trip.flags[Integer.valueOf(my_trip.GetCoverPath())]);
        }
        else {
            Uri uri = Uri.parse("content://media"+my_trip.GetCoverPath());
            File f = new File(getRealPathFromURI(uri));
            Drawable d = Drawable.createFromPath(f.getAbsolutePath());
            upper_layout.setBackground(d);
        }
        InitDialog();
    }

    private void InitTextView(){
        TextView title  = findViewById(R.id.trip_title);
        TextView date = findViewById(R.id.trip_date);
        TextView country = findViewById(R.id.trip_country);
        title.setText(my_trip.GetTitle());
        String _date = my_trip.GetStartYear() +" . " +my_trip.GetStartMonth()+" . " +my_trip.GetStartDay()+" - " ;
        _date += (my_trip.GetEndYear() +" . " +my_trip.GetEndMonth()+" . " +my_trip.GetEndDay());
        date.setText(_date);
        country.setText(my_trip.GetCountry());

    }
    private void InitButton(){
        ImageButton back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        // 依據天數設立Button
        // 設立初始日期
        Calendar calendar_start = Calendar.getInstance();
        calendar_start.set(Integer.valueOf(my_trip.GetStartYear()),Integer.valueOf(my_trip.GetStartMonth()),Integer.valueOf(my_trip.GetStartDay()));
        Calendar calendar_end = Calendar.getInstance();
        calendar_end.set(Integer.valueOf(my_trip.GetEndYear()),Integer.valueOf(my_trip.GetEndMonth()),Integer.valueOf(my_trip.GetEndDay()));
        // 尋找root
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup parent = (ViewGroup)findViewById(R.id.daybtn_group);
        while( !(calendar_start.compareTo(calendar_end)==1) ) {
            View view = inflater.inflate(R.layout.trip_day_button, parent);
            final Button btn = (Button)view.findViewWithTag("new");
            btn.setTag(day_length);
            int year = calendar_start.get(Calendar.YEAR);
            int month = calendar_start.get(Calendar.MONTH);
            int day = calendar_start.get(Calendar.DAY_OF_MONTH);
            btn.setText("第"+(day_length)+"天\n "+year+"."+month+"."+day);
            calendar_start.add(Calendar.DATE,1);
            int index = (day_length-1)%4;
            btn.setBackgroundResource(btn_drawable[index]);
            btn.setTextAppearance(getApplicationContext(), btn_style[index]);
            day_length++;
            btn.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View view) {
                    selected_day = Integer.valueOf(btn.getTag().toString());
                    InitActivity();
                }
            });
        }
        // 新增活動
        FloatingActionButton fab_add = findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),ActivityAddActivity.class);
                // 放入參數
                intent.putExtra("Day",selected_day);
                intent.putExtra("Trip_ID",my_trip.GetTripID());
                intent.putExtra("Mode",0);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        // 切換至地圖路線預覽
        Button switch_to_map = findViewById(R.id.switch_map);
        switch_to_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent();
                intent.setClass(getApplicationContext(),ActivitiesMapActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArray("activities",activities);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

    }
    private void InitActivity(){
        activities = new TripActivity[500];
        data_length = 0;
        // 讀取收藏地點檔案
        FileInputStream fis = null;
        try {
            fis = openFileInput(my_trip.GetTripID() + "_activities"+selected_day);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            int i = 0;
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
                activities[i] = TripActivity.LoadFile(getApplicationContext(),text);
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
        InitActivityList();
    }

    private void InitActivityList(){
        int _cost = 0;
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        for(int i=0;i<data_length;i++){
            HashMap<String, String> hm = new HashMap<String,String>();
            hm.put("title",  activities[i].GetName());
            hm.put("area", activities[i].GetArea());
            hm.put("start_time",activities[i].GetStartTime());
            hm.put("end_time",activities[i].GetEndTime());
            hm.put("cost",activities[i].GetCost()+" NT");
            hm.put("icon",String.valueOf(TripActivity.TripActivityIcon[ Integer.valueOf(activities[i].GetIconPath() ) ]));
            _cost += activities[i].GetCost();
            aList.add(hm);
        }
        String[] from = {"title","area","start_time", "end_time","cost","icon"};

        int[] to = { R.id.title,R.id.area,R.id.start_time,R.id.end_time,R.id.cost,R.id.icon};
        list = (ListView)findViewById(R.id.activity_list);
        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.listview_activity, from, to);
        list.setAdapter(adapter);
        TextView cost = findViewById(R.id.cost);
        cost.setText( _cost+ " NT");
        // 長按刪除
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected_activity = i;
                delete_dialog.show();
                return true;
            }
        });
        // 查看活動
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected_activity = i;
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),ActivityShowActivity.class);
                // 放入參數
                Bundle bundle = new Bundle();
                bundle.putParcelable("activity",activities[selected_activity]);;
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
    }
    @Override
    public void onResume() {
        super.onResume();
        InitActivity();
    }


    Dialog delete_dialog;
    private void InitDialog(){
        delete_dialog = new Dialog(this);
        delete_dialog.setContentView(R.layout.delete_activity);
        Button delete_btn = (Button)delete_dialog.findViewById(R.id.btn_delete);
        Button cancel_btn = (Button)delete_dialog.findViewById(R.id.btn_cancel);
        Button edit_btn = delete_dialog.findViewById(R.id.btn_edit);
        // 刪除活動
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String my_activities = "";
                // 讀取原旅遊計畫檔案
                FileInputStream fis = null;
                try {
                    fis = openFileInput(my_trip.GetTripID() + "_activities"+selected_day);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;
                    while ((text = br.readLine()) != null) {
                        sb.append(text).append("\n");
                    }
                    my_activities = sb.toString();
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
                // 刪除活動
                my_activities = my_activities.replace(my_trip.GetTripID() + activities[selected_activity].GetID()+"\n","");
                // 輸出檔案
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput(my_trip.GetTripID() + "_activities"+selected_day, MODE_PRIVATE);
                    fos.write(my_activities.getBytes());
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
                //Snackbar.make(view,"刪除" + my_trip.GetTripID() + "_activities"+selected_day + " 的 " + my_trip + activities[selected_activity].GetID() ,Snackbar.LENGTH_SHORT).show();
                InitActivity();
            }
        });
        // 編輯活動
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_dialog.dismiss();
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),ActivityAddActivity.class);
                // 放入參數
                intent.putExtra("Day",selected_day);
                intent.putExtra("Trip_ID",my_trip.GetTripID());
                intent.putExtra("Mode",1);
                Bundle bundle = new Bundle();
                bundle.putParcelable("activity",activities[selected_activity]);;
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });
        // 取消
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete_dialog.dismiss();
            }
        });

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
