package com.example.itravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.zhihu.matisse.Matisse;

import androidx.appcompat.app.ActionBar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity    {

    private AppBarConfiguration mAppBarConfiguration;
    View headerLayout;
    Toolbar toolbar;


    int[] theme_color = new int[]{
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.buttonWarning,
            R.color.buttonSuccess,
            R.color.colorAccent,
            R.color.buttonInfo,
            R.color.dracula_bottom_toolbar_apply_text,
    };
    int selected_theme_color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,R.id.nav_maps)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        headerLayout = navigationView.getHeaderView(0); // 0-index header

        // read theme color
        SharedPreferences sharedPreferences = getSharedPreferences("data" , MODE_PRIVATE);
        selected_theme_color = sharedPreferences.getInt("theme_color" , 0);
        // set theme color
        toolbar.setBackgroundColor(getResources().getColor(theme_color[selected_theme_color]));
        headerLayout.setBackgroundColor(getResources().getColor(theme_color[selected_theme_color]));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void Switch(View view ){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // 取得點選項目的id
        int id = item.getItemId();
        // 依照id判斷點了哪個項目並做相應事件
        if (id == R.id.action_settings) {
            // 按下「設定」要做的事
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(),SettingActivity.class);
            startActivityForResult(intent,1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            // read theme color
            selected_theme_color = data.getIntExtra("selected",0);
            // set theme color
            toolbar.setBackgroundColor(getResources().getColor(theme_color[selected_theme_color]));
            headerLayout.setBackgroundColor(getResources().getColor(theme_color[selected_theme_color]));
        }
    }


}
