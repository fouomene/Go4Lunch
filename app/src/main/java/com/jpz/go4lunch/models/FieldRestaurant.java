package com.jpz.go4lunch.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class FieldRestaurant {
    // Common list for the fields of the restaurants

    public String id;
    public LatLng latLng;

    public String name;
    public String distance;
    public String type;
    public String address;
    public String workmates;
    public String hours;
    public String opinions;
    public String image;
    public String phoneNumber;
    public String webSite;

    public List<String> idList;

    public List<String> getIdList() {
        return idList;
    }
}
