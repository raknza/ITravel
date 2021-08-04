package com.example.itravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.filter.Filter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

public class ActivityAddActivity extends AppCompatActivity  implements TimePickerDialog.OnTimeSetListener,  View.OnClickListener {

    TripActivity activity;
    String trip_id;
    String day;

    TextView title ;
    EditText input_name;
    EditText input_area ;
    EditText input_start ;
    EditText input_end ;
    EditText input_description ;
    EditText input_cost ;
    Button input_cover;
    ImageView title_icon;
    ImageView title_cover;
    GooglePlace place ;
    Button select_area_btn;

    public static final int MY_PERMISSION_ACCESS_FILE = 1;

    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Intent intent = getIntent();
        // 判別mode
        mode = intent.getIntExtra("Mode",0);
        // 讀取Trip id
        trip_id = intent.getStringExtra("Trip_ID");
        // 讀取天數
        day = Integer.toString(intent.getIntExtra("Day",0));
        Button add_btn = findViewById(R.id.add_btn);
        title = findViewById(R.id.title);
        input_name = findViewById(R.id.input_name);
        input_area = findViewById(R.id.input_area);
        input_start = findViewById(R.id.input_start);
        input_end = findViewById(R.id.input_end);
        input_description = findViewById(R.id.input_description);
        input_cost = findViewById(R.id.input_cost);
        input_cover = findViewById(R.id.input_cover);
        title_icon = findViewById(R.id.title_icon);
        title_cover = findViewById(R.id.title_cover);
        select_area_btn = findViewById(R.id.select_area_btn);

        input_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableMyFileIfPermitted();
            }
        });

        if(mode == 1){
            add_btn.setText("修改");
            title.setText("編輯活動");
            // 讀取參數
            Bundle bundle =intent.getExtras();
            Parcelable data = bundle.getParcelable("activity");
            activity = (TripActivity)data;
            input_name.setText(activity.GetName());
            input_area.setText(activity.GetArea());
            input_start.setText(activity.GetStartTime());
            input_end.setText(activity.GetEndTime());
            input_description.setText(activity.GetDescription());
            input_cost.setText(String.valueOf(activity.GetCost()));
            if(!activity.GetCoverPath().isEmpty()){
                cover_uri = Uri.parse("content://media"+activity.GetCoverPath());
                title_cover.setImageURI(cover_uri);
            }
            selected_icon = Integer.valueOf(activity.GetIconPath());
            title_icon.setImageResource(TripActivity.TripActivityIcon[selected_icon]);
            place = activity.GetPlace();
            if(place!=null){
                select_area_btn.setText("移除選擇地點");
                select_area_btn.setTag("delete");

            }
        }
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 活動目錄存入trip
                String my_activities = "";
                // 讀取原旅遊計畫檔案
                FileInputStream fis = null;
                try {
                    fis = openFileInput(trip_id + "_activities"+day);
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
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //Toast.makeText(getApplicationContext(),"讀取舊檔案成功("+trip_id + "_activities"+day + "):" +my_activities ,Toast.LENGTH_SHORT);
                // 加入新活動
                try {
                    // icon選擇
                    // cover 選擇
                    // Google Place選擇
                    String old_id = null;
                    if(mode == 1){
                        old_id = activity.GetID();
                    }
                    if(input_cost.getText().toString().isEmpty()){
                        input_cost.setText("0");
                    }
                    if(cover_uri != null){
                        activity = new TripActivity(input_name.getText().toString(),input_area.getText().toString(),selected_icon+"",cover_uri.getPath(),input_start.getText().toString(),input_end.getText().toString(),input_description.getText().toString(),Integer.valueOf(input_cost.getText().toString()),place);

                    }
                    else
                        activity = new TripActivity(input_name.getText().toString(),input_area.getText().toString(),selected_icon+"","",input_start.getText().toString(),input_end.getText().toString(),input_description.getText().toString(),Integer.valueOf(input_cost.getText().toString()),place);
                    // 重複ID
                    while(mode == 0 && my_activities.contains(trip_id + activity.GetID() ) ){
                        activity = new TripActivity(activity);
                    }
                    if(old_id!=null) {
                        activity.ChangeID(old_id);
                    }
                    activity.SaveFile(getApplicationContext(), trip_id + activity.GetID());
                    if(mode == 0)
                        my_activities+= trip_id + activity.GetID()+"\n";
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if(mode == 0) {
                    // 輸出檔案
                    FileOutputStream fos = null;
                    try {
                        fos = openFileOutput(trip_id + "_activities" + day, MODE_PRIVATE);
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
                }
                //Snackbar.make(view,"新增成功:" + trip_id + activity.GetID() ,Snackbar.LENGTH_SHORT).show();
                // 新增/修改完成 返回
                onBackPressed();
            }
        });

        input_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_set =true;
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "選擇開始時間");
            }
        });
        input_end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_set = false;
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(), "選擇結束時間");
            }
        });
        // 返回button
        ImageButton back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        // 新增Icon button
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup parent = (ViewGroup)findViewById(R.id.icon_layout);
        for( int i=0;i<TripActivity.TripActivityIcon.length;i++) {
            View view = inflater.inflate(R.layout.activity_icon_button, parent);
            final ImageButton btn = view.findViewWithTag("new");
            btn.setTag(i);
            btn.setBackgroundResource(TripActivity.TripActivityIcon[i]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selected_icon = Integer.valueOf(btn.getTag().toString());
                    title_icon.setImageResource(TripActivity.TripActivityIcon[selected_icon]);
                }
            });
        }


        // 從收藏地點中選擇 Button
        select_area_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!select_area_btn.getTag().toString().equals("delete")) {
                    // 讀取收藏地點檔案
                    GooglePlace[] _place = new GooglePlace[10000];
                    // 讀取收藏地點檔案
                    FileInputStream fis = null;
                    try {
                        fis = openFileInput("collect_place");
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader br = new BufferedReader(isr);
                        StringBuilder sb = new StringBuilder();
                        String text;
                        int i = 0;
                        while ((text = br.readLine()) != null) {
                            sb.append(text).append("\n");
                            _place[i] = GooglePlace.LoadFile(getApplicationContext(), text);
                            i++;

                        }

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

                    // 包入Bundle
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArray("Google_Place", _place);
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), PlaceActivity.class);
                    intent.putExtras(bundle);
                    intent.putExtra("mode", 1);
                    startActivityForResult(intent, 31);
                }
                else{
                    select_area_btn.setText("從收藏地點選擇");
                    place = null;
                    input_area.setText("");
                    input_description.setText("");
                    select_area_btn.setTag("");
                }
            }
        });
    }
    int selected_icon = 0;
    boolean start_set;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        String hour = hourOfDay+"";
        String min = minute+"";
        if(hourOfDay <=12)
            hour = "上午" + hourOfDay;
        else
            hour = "下午" + (hourOfDay -12);
        if(minute < 10)
            min = "0"+minute;

        if(start_set){

            input_start.setText(hour + ":" + min);
        }
        else{
            input_end.setText(hour + ":" + min);
        }
    }


    @SuppressLint("CheckResult")
    @Override
    public void onClick(final View v) {
        enableMyFileIfPermitted();
    }

    // 詢問取得檔案存取權限
    private void enableMyFileIfPermitted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSION_ACCESS_FILE);
        }
        else
        {
            OpenImageBrowser();
        }
    }

    // 要求權限 使用者回應後
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FILE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    OpenImageBrowser();
                }
                return;
            }

        }
    }

    private void OpenImageBrowser(){
        Matisse.from(this)
                .choose(MimeType.ofImage())
                .theme(R.style.Matisse_Dracula)
                .countable(false)
                .addFilter(new GifSizeFilter(200, 200, 5 * Filter.K * Filter.K))
                .maxSelectable(1)
                .originalEnable(true)
                .maxOriginalSize(10)
                .imageEngine(new PicassoEngine())
                .forResult(23);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 選擇活動封面圖片
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 23 && resultCode == RESULT_OK) {
            cover_uri = Matisse.obtainResult(data).get(0);
            title_cover.setImageURI(cover_uri);
        }
        // 選擇收藏地點
        if(requestCode == 31){
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                place = bundle.getParcelable("selected_place");
                input_area.setText(place.GetName());
                input_description.setText(place.GetName() + "\n" + place.GetRating()+"\n" +place.GetVicinty());
                select_area_btn.setText("移除選擇地點");
                select_area_btn.setTag("delete");
            }
        }

    }

    Uri cover_uri;
}
