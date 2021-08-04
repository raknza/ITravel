package com.example.itravel;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GooglePlace  implements Parcelable,Serializable {

    private String name;
    private float lat;
    private float lng;
    private float rating;
    private boolean opened;
    private String vicinty;
    private String photo_ref;
    private String place_id;

    public GooglePlace(String name ,float lat,float lng ,float rating,boolean opened,String vicinty,String photo_ref,String place_id){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.rating = rating;
        this.opened = opened;
        this.vicinty = vicinty;
        this.photo_ref = photo_ref;
        this.place_id = place_id;
    }

    protected GooglePlace(Parcel in) {
        name = in.readString();
        lat = in.readFloat();
        lng = in.readFloat();
        rating = in.readFloat();
        opened = in.readByte() != 0;
        vicinty = in.readString();
        photo_ref = in.readString();
        place_id = in.readString();
    }

    public static final Creator<GooglePlace> CREATOR = new Creator<GooglePlace>() {
        @Override
        public GooglePlace createFromParcel(Parcel in) {
            return new GooglePlace(in);
        }

        @Override
        public GooglePlace[] newArray(int size) {
            return new GooglePlace[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeFloat(lat);
        dest.writeFloat(lng);
        dest.writeFloat(rating);
        dest.writeByte((byte) (opened ? 1 : 0));
        dest.writeString(vicinty);
        dest.writeString(photo_ref);
        dest.writeString(place_id);
    }

    @Override
    public int describeContents() {
        return 0;
    }



    public String GetName(){return name;}
    public float GetLat(){return  lat;}
    public String GetVicinty(){return vicinty;}
    public float GetLng(){return  lng;}
    public String GetPhotoRef(){return photo_ref;}
    public float GetRating(){return  rating;}
    public boolean GetOpened(){return opened;}
    public String GetPlaceId(){return  place_id;}

    public void SaveFile(Context context,String fileName) throws IOException, ClassNotFoundException {
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(this);
        os.close();
        fos.close();
    }

    public static GooglePlace LoadFile(Context context,String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream is = new ObjectInputStream(fis);
        GooglePlace get_object = (GooglePlace) is.readObject();
        is.close();
        fis.close();
        return get_object;
    }
}
