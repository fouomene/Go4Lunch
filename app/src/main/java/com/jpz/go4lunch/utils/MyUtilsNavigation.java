package com.jpz.go4lunch.utils;

import android.content.Context;
import android.content.Intent;

import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.activities.DetailsRestaurantActivity;

public class MyUtilsNavigation {
    // Class to navigate between activities

    // Key for Intent
    public static final String KEY_PLACE = "key_place";

    // Start DetailsRestaurantActivity when click the user click on a restaurant
    // (from the map or list) and transfer Place data.
    public void startDetailsRestaurantActivity(Context context, Place place) {
        Intent intent = new Intent(context, DetailsRestaurantActivity.class);
        intent.putExtra(KEY_PLACE, place);
        context.startActivity(intent);
    }

}
