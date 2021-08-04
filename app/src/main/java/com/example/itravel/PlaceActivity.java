package com.example.itravel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;


import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlaceActivity extends AppCompatActivity {



    ListView list;
    int[] flags = new int[]{
            R.drawable.ic_menu_camera,
            R.drawable.check,
            R.drawable.taipei,
            R.drawable.ic_list_star
    };

    OnScrollListenerImpl scrollListener;

    private GooglePlace place[];
    private DownloadImageTask photo_tasks[];
    private boolean checked[];

    int selected_mode = 0;
    int data_length = 0;
    int selected_item = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.place_list);
        // 讀取參數
        Bundle bundle = getIntent().getExtras();
        Parcelable[] data = bundle.getParcelableArray("Google_Place");
        place = new GooglePlace[data.length];
        for(int i = 0; i < data.length; i++){
            place[i] = (GooglePlace) data[i];
        }
        selected_mode = getIntent().getIntExtra("mode",0);
        // 初始化
        photo_tasks = new DownloadImageTask[data.length];
        checked = new boolean[data.length];
        for(int i=0;i<data.length;i++)
            checked[i] = false;
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        for(int i=0;i<data.length;i++){
            if(place[i]!=null) {
                HashMap<String, String> hm = new HashMap<String, String>();
                hm.put("title", place[i].GetName());
                hm.put("vicinty", place[i].GetVicinty());
                if(place[i].GetRating() !=0) {
                    hm.put("rating", " " + place[i].GetRating()+ " / 5");
                    hm.put("star", Integer.toString(flags[3]) );
                }
                hm.put("opened","營業中："+(place[i].GetOpened()==true?"是":"否"));
                hm.put("flag", Integer.toString(flags[0]));
                hm.put("check_box",Integer.toString(flags[1]));
                aList.add(hm);
                data_length++;
            }
        }
        String[] from = {"title","vicinty", "flag","rating","star","opened","check_box"};
        int[] to = { R.id.title,R.id.vicinty,R.id.flag,R.id.rating,R.id.star,R.id.opened,R.id.check_box};
        list = findViewById(R.id.PlaceListView);
        final SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), aList, R.layout.listview_place_layout, from, to);
        list.setAdapter(adapter);
        list.post(new Runnable() {
            @Override
            public void run() {
                scrollListener = new OnScrollListenerImpl();
                list.setOnScrollListener(scrollListener);
                scrollListener.listview = list;
            }
        });
        InitListOnClick();
        // mode 0
        // 儲存收藏地點為檔案
        Button save_btn = findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String collect_place = "";
                // 讀取原收藏地點檔案
                FileInputStream fis = null;
                try {
                    fis = openFileInput("collect_place");
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    String text;
                    while ((text = br.readLine()) != null) {
                        sb.append(text).append("\n");

                    }
                    collect_place = sb.toString();
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
                // 加入新收藏地點
                for(int i=0;place[i]!=null;i++){
                    if(checked[i]){
                        try {
                            place[i].SaveFile(getApplicationContext(),place[i].GetPlaceId());
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if(!collect_place.contains(place[i].GetPlaceId()))
                            collect_place += place[i].GetPlaceId()+"\n";
                    }
                }
                // 輸出檔案
                FileOutputStream fos = null;
                try {
                    fos = openFileOutput("collect_place", MODE_PRIVATE);
                    fos.write(collect_place.getBytes());
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
                Toast.makeText(PlaceActivity.this,"已收藏地點",Toast.LENGTH_SHORT).show();
            }
        });
        // mode 1
        // 回傳選擇地點
        if(selected_mode == 1){
            save_btn.setText("選擇收藏地點");
            save_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent data = new Intent();
                    Bundle bundle = new Bundle();


                    if(selected_item == -1)
                        Snackbar.make(view,"請選擇一個地點",Snackbar.LENGTH_SHORT).show();
                    else {
                        bundle.putParcelable("selected_place", place[selected_item]);
                        data.putExtras(bundle);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
            });
        }
    }

    private void InitListOnClick(){
        // 加入收藏地點
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ImageView check_box = (ImageView) view.findViewById(R.id.check_box);
                if(selected_mode == 0) {
                    checked[i] = !checked[i];
                    if (checked[i])
                        check_box.setBackgroundResource(R.drawable.checked);
                    else
                        check_box.setBackgroundResource(R.drawable.check);
                }
                else{
                    for(int j=0;j<data_length;j++) {
                        checked[j] = false;
                    }
                    checked[i] = true;
                    selected_item = i;
                    check_box.setBackgroundResource(R.drawable.checked);
                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
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
        int scrollStates;
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            scrollStates = scrollState;
        }
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            int lastInScreen = firstVisibleItem + visibleItemCount-1;
            for(int i=firstVisibleItem;i<=lastInScreen;i++) {
                if(place[i].GetPhotoRef()==null || place[i].GetPhotoRef().isEmpty()){
                    // get ImageView
                    ImageView img;
                    View root_LinearLayout = getViewByPosition(i, list);
                    img = root_LinearLayout.findViewById(R.id.flag);
                    img.setImageResource(R.drawable.ic_menu_camera);
                }
                else if (photo_tasks[i] == null) {
                    // get ImageView
                    ImageView img;
                    View root_LinearLayout = getViewByPosition(i, list);
                    img = root_LinearLayout.findViewById(R.id.flag);
                    // Download image
                    photo_tasks[i] = new DownloadImageTask(img);
                    photo_tasks[i].execute("\n" + "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&maxheight=400&photoreference=" + place[i].GetPhotoRef() + "&key=AIzaSyBcOYjftgB07OFjI5Z9kJN29guefaNn12c");
                } else {
                    photo_tasks[i].Set();
                }
                // check_box
                View root_LinearLayout = getViewByPosition(i, list);
                ImageView check_box = (ImageView) root_LinearLayout.findViewById(R.id.check_box);
                if(checked[i])
                    check_box.setBackgroundResource(R.drawable.checked);
                else
                    check_box.setBackgroundResource(R.drawable.check);
            }
        }
    }

    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Bitmap bmp;
        ImageView img;
        public DownloadImageTask(ImageView img) {
            this.img = img;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            bmp = mIcon11;
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmp = result;
            Set();
        }
        public void Set(){

            img.setImageBitmap(bmp);
        }
    }
}
