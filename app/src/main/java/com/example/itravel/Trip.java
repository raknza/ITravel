package com.example.itravel;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Random;

public class Trip implements Parcelable, Serializable {

    String cover_path;
    String title;
    String country;
    String start_year,start_month,start_day,end_year,end_month,end_day;
    String trip_id;
    boolean internal_cover;

    static String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static int[] flags = new int[]{
            R.drawable.koushoun,
            R.drawable.taipei,
            R.drawable.ilan,
            R.drawable.taouyun,
            R.drawable.sun_under,
            R.drawable.sun_under2

    };


    public Trip(String title,String country,String cover_path,String start_year,String start_month,String start_day,String end_year,String end_month,String end_day){
        this.title = title;
        this.country = country;
        this.start_year = start_year;
        this.start_month = start_month;
        this.start_day = start_day;
        this.end_year = end_year;
        this.end_month = end_month;
        this.end_day = end_day;
        this.cover_path = cover_path;
        trip_id = RandomID();
        if(cover_path.matches("[0-9]+")){
            internal_cover = true;
        }
    }

    public Trip(Trip copy){
        this.title = copy.title;
        this.country = copy.country;
        this.start_year = copy.start_year;
        this.start_month = copy.start_month;
        this.start_day = copy.start_day;
        this.end_year = copy.end_year;
        this.end_month = copy.end_month;
        this.end_day = copy.end_day;
        this.cover_path = copy.cover_path;
        trip_id = RandomID();
        if(cover_path.matches("[0-9]+")){
            internal_cover = true;
        }
    }

    protected Trip(Parcel in) {
        title = in.readString();
        country = in.readString();
        start_year = in.readString();
        start_month = in.readString();
        start_day = in.readString();
        end_year = in.readString();
        end_month = in.readString();
        end_day = in.readString();
        trip_id = in.readString();
        cover_path = in.readString();
        if(cover_path.matches("[0-9]+")){
            internal_cover = true;
        }
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(country);
        parcel.writeString(start_year);
        parcel.writeString(start_month);
        parcel.writeString(start_day);
        parcel.writeString(end_year);
        parcel.writeString(end_month);
        parcel.writeString(end_day);
        parcel.writeString(trip_id);
        parcel.writeString(cover_path);
    }


    public void SaveFile(Context context,String fileName) throws IOException, ClassNotFoundException {
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(this);
        os.close();
        fos.close();
    }

    public static Trip LoadFile(Context context, String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        Trip get_object = (Trip) is.readObject();
        is.close();
        fis.close();
        return get_object;
    }

    public static String RandomID(){
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<32;i++){
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public String GetTitle(){return title;}
    public String GetCountry(){return  country;}
    public String GetCoverPath(){return cover_path;}
    public String GetStartYear(){return  start_year;}
    public String GetStartMonth(){return  start_month;}
    public String GetStartDay(){return  start_day;}
    public String GetEndYear(){return  end_year;}
    public String GetEndMonth(){return  end_month;}
    public String GetEndDay(){return  end_day;}
    public String GetTripID(){return  trip_id;}
    public boolean GetInternalCover(){return internal_cover;}
}
