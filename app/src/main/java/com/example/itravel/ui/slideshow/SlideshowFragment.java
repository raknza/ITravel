package com.example.itravel.ui.slideshow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.itravel.GooglePlace;
import com.example.itravel.R;

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

public class SlideshowFragment extends Fragment {

    private SlideshowViewModel slideshowViewModel;
    ListView list;
    OnScrollListenerImpl scrollListener;
    int data_length;
    View root;

    private GooglePlace place[];
    private DownloadImageTask photo_tasks[];

    private boolean checked[];
    int[] flags = new int[]{
            R.drawable.ic_menu_camera,
            R.drawable.check,
            R.drawable.taipei,
            R.drawable.ic_list_star
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(SlideshowViewModel.class);
        root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        InitPlaceList();
        // 刪除勾選收藏地點
        Button delete_btn = root.findViewById(R.id.delete_btn);
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String collect_place = "";
                // 讀取原收藏地點檔案
                FileInputStream fis = null;
                try {
                    fis = getActivity().openFileInput("collect_place");
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
                // 刪除收藏地點
                for(int i=0;place[i]!=null;i++){
                    if(checked[i]){
                        if(collect_place.contains(place[i].GetPlaceId()+"\n")) {
                            collect_place = collect_place.replace(place[i].GetPlaceId()+"\n","");
                        }
                    }

                }
                // 輸出檔案
                FileOutputStream fos = null;
                try {
                    fos = getActivity().openFileOutput("collect_place", getActivity().MODE_PRIVATE);
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

                InitPlaceList();
            }
        });

        return root;
    }

    private void InitPlaceList(){
        place = new GooglePlace[10000];
        data_length = 0;
        // 讀取收藏地點檔案
        FileInputStream fis = null;
        try {
            fis = getActivity().openFileInput("collect_place");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            int i = 0;
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
                Log.e("load",text);
                place[i] = GooglePlace.LoadFile(getActivity().getApplicationContext(),text);
                i++;
                data_length++;
            }
            Log.e("log",sb.toString());
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
        photo_tasks = new DownloadImageTask[data_length];
        checked = new boolean[data_length];
        for(int i=0;i<data_length;i++)
            checked[i] = false;

        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        for(int i=0;i<data_length;i++){
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
            }
        }
        String[] from = {"title","vicinty", "flag","rating","star","check_box"};
        int[] to = { R.id.title,R.id.vicinty,R.id.flag,R.id.rating,R.id.star,R.id.check_box};
        list = root.findViewById(R.id.PlaceListView);
        final SimpleAdapter adapter = new SimpleAdapter(getActivity().getApplicationContext(), aList, R.layout.listview_place_layout, from, to);
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
    }

    private void InitListOnClick(){
        // 加入收藏地點
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                checked[i] = !checked[i];
                ImageView check_box = (ImageView) view.findViewById(R.id.check_box);
                if(checked[i])
                    check_box.setBackgroundResource(R.drawable.checked);
                else
                    check_box.setBackgroundResource(R.drawable.check);
            }
        });
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
                if(place[i].GetPhotoRef()==null){
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
