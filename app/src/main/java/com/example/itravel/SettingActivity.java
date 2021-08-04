package com.example.itravel;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    RadioGroup radioGroup;
    Button radio_btn;
    int selected = 0;
    RadioButton before_btn;
    RadioButton r_btn[] ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        r_btn = new RadioButton[7];
        setContentView(R.layout.activity_setting);
        r_btn[0] = findViewById(R.id.first2);
        r_btn[1] = findViewById(R.id.first3);
        r_btn[2] = findViewById(R.id.first4);
        r_btn[3] = findViewById(R.id.first5);
        r_btn[4] = findViewById(R.id.first6);
        r_btn[5] = findViewById(R.id.first7);
        r_btn[6] = findViewById(R.id.first8);
        // read
        SharedPreferences sharedPreferences = getSharedPreferences("data" , MODE_PRIVATE);
        selected = sharedPreferences.getInt("theme_color" , 0);
        r_btn[selected].setChecked(true);
        before_btn = (RadioButton)r_btn[selected];
    }
    @Override
    public void onBackPressed() {
        SharedPreferences sharedPreferences = getSharedPreferences("data" , MODE_PRIVATE);
        sharedPreferences.edit().putInt("theme_color" , selected).apply();

        Intent data = new Intent();
        data.putExtra("selected",selected);
        setResult(RESULT_OK,data);
        finish();
        super.onBackPressed();
    }

    public void OnRadioClick(View view){

        int view_id = view.getId();
        for(int i=0;i<7;i++){
            if(view_id == r_btn[i].getId())
                selected = i;
        }
        before_btn.setChecked(false);
        before_btn = r_btn[selected];
    }
}
