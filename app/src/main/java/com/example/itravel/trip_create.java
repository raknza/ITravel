package com.example.itravel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.filter.Filter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.List;

import static com.example.itravel.MapsActivity.MY_PERMISSION_ACCESS_FINE_LOCATION;

public class trip_create extends AppCompatActivity  implements View.OnClickListener  {

    // 日曆視窗
    Dialog dialog;
    CalendarView cal_date;
    String start_year,start_month,start_day,end_year,end_month,end_day;
    boolean start = false;
    Button start_date_btn,end_date_btn;
    ImageView cover_img;
    EditText title,country;

    Trip trip;

    public static final int MY_PERMISSION_ACCESS_FILE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_trip_create);

        cover_img = findViewById(R.id.trip_cover);
        title = findViewById(R.id.title);
        country = findViewById(R.id.country);
        InitAddDateDialog();
        InitButton();
        start_year = end_year = "2020";
        start_month = end_month = "6";
        start_day = end_day = "13";

    }

    private void InitButton(){
        start_date_btn = findViewById(R.id.date_btn1);
        start_date_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = true;
                dialog.show();
            }
        });
        end_date_btn = findViewById(R.id.date_btn2);
        end_date_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = false;
                dialog.show();
            }
        });

        // 從檔案選取封面圖片
        ImageButton select_cover_btn2 = findViewById(R.id.select_cover_btn2);
        select_cover_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enableMyFileIfPermitted();
            }
        });
        // 從內建圖庫選取
        ImageButton select_cover_btn = findViewById(R.id.select_cover_btn);
        select_cover_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),CoverInternalActivity.class);
                startActivityForResult(intent,30);
                //overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            }
        });

        Button create_trip_btn = findViewById(R.id.create_btn);
        create_trip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 儲存旅遊計畫為檔案
                String my_trips = "";
                // 讀取原旅遊計畫檔案
                FileInputStream fis = null;
                try {
                    fis = openFileInput("my_trips");
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;
                    while ((text = br.readLine()) != null) {
                        sb.append(text).append("\n");

                    }
                    my_trips = sb.toString();
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

                // 加入新旅遊計畫
                try {
                    if (uri != null) {
                        trip = new Trip(title.getText().toString(),country.getText().toString(),uri.getPath(),start_year,start_month,start_day,end_year,end_month,end_day);
                    }
                    else
                        trip = new Trip(title.getText().toString(),country.getText().toString(),selected+"",start_year,start_month,start_day,end_year,end_month,end_day);
                    // 重複ID
                    while(my_trips.contains( trip.GetTripID() ) ){
                        trip = new Trip(trip);
                    }
                    trip.SaveFile(getApplicationContext(), trip.GetTripID());
                    my_trips+= trip.GetTripID()+"\n";
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
                // 輸出檔案
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput("my_trips", MODE_PRIVATE);
                    fos.write(my_trips.getBytes());
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
                // 儲存成功訊息
                Toast.makeText(trip_create.this, "已建立旅遊計畫", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    private void InitAddDateDialog(){
        dialog = new Dialog(trip_create.this);
        dialog.setTitle("Set Date!");
        dialog.setContentView(R.layout.date_select);
        cal_date = dialog.findViewById(R.id.calendarView);
        cal_date.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            String week[]={"","日","一","二","三","四","五","六"};
            Calendar calendar = Calendar.getInstance();
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                calendar.set(i, i1,i2);
                int day_week = calendar.get(Calendar.DAY_OF_WEEK);
                String dayOfWeek;
                dayOfWeek = week[day_week];
                if(start) {
                    start_year = String.valueOf(i);
                    start_month = String.valueOf(i1+1);
                    start_day = String.valueOf(i2);
                    start_date_btn.setText(start_year +"週" + dayOfWeek +"\n" +start_month+" - " + start_day);
                }
                else{
                    end_year = String.valueOf(i);
                    end_month = String.valueOf(i1+1);
                    end_day = String.valueOf(i2);
                    end_date_btn.setText(end_year +"週"+ dayOfWeek +"\n" +end_month+" - " + end_day);
                }
                dialog.dismiss();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
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
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 23 && resultCode == RESULT_OK) {
            uri = Matisse.obtainResult(data).get(0);
            cover_img.setImageURI(uri);
        }
        if(requestCode == 30){
            if (resultCode == RESULT_OK) {
                selected = data.getIntExtra("selected",0);
                cover_img.setImageResource(Trip.flags[selected]);
                uri = null;
            }
        }
    }
    Uri uri;
    int selected;
}
