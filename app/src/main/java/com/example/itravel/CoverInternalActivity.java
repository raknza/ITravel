package com.example.itravel;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class CoverInternalActivity extends AppCompatActivity {



    int selected = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.internal_cover_layout);
        CustomGrid adapter = new CustomGrid(this,  Trip.flags);
        final GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected = position;
                for(int i=0;i<Trip.flags.length;i++) {
                    View _view = grid.getChildAt(i);
                    if(_view!=null) {
                        ImageView imageView = (ImageView) _view.findViewById(R.id.grid_image);
                        if (selected != i) {
                            imageView.setBackground(null);
                        }
                        else
                            imageView.setBackgroundResource(R.drawable.select_border);
                    }
                }
                //R.drawable.select_border
            }
        });

        Button select_btn = findViewById(R.id.seleted_btn);
        select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("selected",selected);
                setResult(RESULT_OK,data);
                finish();
            }
        });
    }

    public class CustomGrid extends BaseAdapter {
        private Context context;
        private final int[] imageId;
        int length;

        public CustomGrid(Context context,  int[] imageId) {
            this.context = context;
            length = imageId.length;
            this.imageId = imageId;
        }

        @Override
        public int getCount() {
            return length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View grid;
            // Context 動態放入mainActivity
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                grid = new View(context);
                // 將grid_single 動態載入(image+text)
                grid = layoutInflater.inflate(R.layout.cover_grid_layout, null);
                ImageView imageView = (ImageView) grid.findViewById(R.id.grid_image);
                imageView.setImageResource(imageId[position]);
            } else {
                grid = (View) convertView;
            }
            return grid;
        }
    }
}
