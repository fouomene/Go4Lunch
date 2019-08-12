package com.jpz.go4lunch.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class FieldRestaurant implements Parcelable {
    // Common list for the fields of the restaurants

    public String id;
    public String name;
    public String distance;
    public String type;
    public String address;
    public String workmates;
    public String hours;
    public String opinions;
    public String image;

    //public ArrayList<String> idList;

    /*
    public List<String> getIdList() {
        return idList;
    }
    */

    public FieldRestaurant(String id) {
        this.id = id;
    }

    /*
    public FieldRestaurant(String id, ArrayList<String> idList) {
        this.id = id;
        this.idList = idList;
    }
*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(distance);
        dest.writeString(type);
        dest.writeString(address);
        dest.writeString(workmates);
        dest.writeString(hours);
        dest.writeString(opinions);
        dest.writeString(image);
        //dest.writeStringList(idList);
    }

    // Creator
    public static final Parcelable.Creator<FieldRestaurant> CREATOR
            = new Parcelable.Creator<FieldRestaurant>() {

        public FieldRestaurant createFromParcel(Parcel in) {
            return new FieldRestaurant(in);
        }

        public FieldRestaurant[] newArray(int size) {
            return new FieldRestaurant[size];
        }
    };

    // "De-parcel" object
    private FieldRestaurant(Parcel in) {
        id = in.readString();
        name = in.readString();
        distance = in.readString();
        type = in.readString();
        address = in.readString();
        workmates = in.readString();
        hours = in.readString();
        opinions = in.readString();
        image = in.readString();
        //idList = in.createStringArrayList();
    }

}
