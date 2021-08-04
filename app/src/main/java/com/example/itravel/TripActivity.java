package com.example.itravel;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class TripActivity implements Parcelable, Serializable {

    private String name;
    private String area;
    private String icon_path;
    private String cover_path;
    private String start_time;
    private String end_time;
    private String description;
    private int cost;
    private GooglePlace place;
    boolean googleplace_used;
    private String activity_id;


    public TripActivity(String name,String area,String icon_path,String cover_path,String start_time,String end_time,String description,int cost,GooglePlace place){
        this.name = name;
        this.icon_path = icon_path;
        this.cover_path = cover_path;
        this.start_time = start_time;
        this.end_time = end_time;
        this.description = description;
        this.cost = cost;
        this.place = place;
        this.area = area;
        if(place == null)
            googleplace_used = false;
        else
            googleplace_used = true;

        activity_id = Trip.RandomID();
    }


    public TripActivity(TripActivity copy){
        this.name = copy.name;
        this.icon_path = copy.icon_path;
        this.cover_path = copy.cover_path;
        this.start_time = copy.start_time;
        this.end_time = copy.end_time;
        this.description = copy.description;
        this.cost = copy.cost;
        this.place = copy.place;
        this.area = copy.area;
        if(place == null)
            googleplace_used = false;
        else
            googleplace_used = true;

        activity_id = Trip.RandomID();

    }

    protected TripActivity(Parcel in) {
        name = in.readString();
        area = in.readString();
        icon_path = in.readString();
        cover_path = in.readString();
        start_time = in.readString();
        end_time = in.readString();
        description = in.readString();
        cost = in.readInt();
        place = in.readParcelable(GooglePlace.class.getClassLoader());
        googleplace_used = in.readByte() != 0;
        activity_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(area);
        dest.writeString(icon_path);
        dest.writeString(cover_path);
        dest.writeString(start_time);
        dest.writeString(end_time);
        dest.writeString(description);
        dest.writeInt(cost);
        dest.writeParcelable(place, flags);
        dest.writeByte((byte) (googleplace_used ? 1 : 0));
        dest.writeString(activity_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TripActivity> CREATOR = new Creator<TripActivity>() {
        @Override
        public TripActivity createFromParcel(Parcel in) {
            return new TripActivity(in);
        }

        @Override
        public TripActivity[] newArray(int size) {
            return new TripActivity[size];
        }
    };


    public void SaveFile(Context context, String fileName) throws IOException, ClassNotFoundException {
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(this);
        os.close();
        fos.close();
    }

    public static TripActivity LoadFile(Context context, String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        TripActivity get_object = (TripActivity) is.readObject();
        is.close();
        fis.close();
        return get_object;
    }


    public String GetName(){return name;}
    public String GetArea(){return  area;}
    public String GetIconPath(){return icon_path;}
    public String GetCoverPath(){return cover_path;}
    public String GetStartTime(){return start_time;}
    public String GetEndTime(){return end_time;}
    public String GetDescription(){return description;}
    public int GetCost(){return cost;}
    public GooglePlace GetPlace(){return place;}
    public String GetID(){return activity_id;}
    public boolean GetGooglePlaceUsed(){return googleplace_used;}
    protected void ChangeID(String id){activity_id = id;}
    protected void ChangeCoverPath(String path){cover_path = path;}
    protected void ChangeIconPath(String path){icon_path = path;}



    public static int[] TripActivityIcon = new int[]{
            R.drawable.ic_menu_myplace,
            R.drawable.ic_list_star,
            R.drawable.food_icon,
            R.drawable.house_icon,
            R.drawable.banknote_icon,
            R.drawable.bike_icon,
            R.drawable.bus_icon,
            R.drawable.car_icon,
            R.drawable.train_icon,
            R.drawable.ship_icon,
            R.drawable.gift_icon,
            R.drawable.house_icon,
            R.drawable.shopping_icon,
            R.drawable.tower_icon

    };

}
