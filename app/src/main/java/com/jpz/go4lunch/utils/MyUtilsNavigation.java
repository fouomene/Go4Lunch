package com.jpz.go4lunch.utils;

import android.content.Context;
import android.content.Intent;

import com.jpz.go4lunch.activities.DetailsRestaurantActivity;

public class MyUtilsNavigation {
    // Class to navigate between activities

    // Key for Intent
    public static final String KEY_ID = "key_id";

    // Start DetailsRestaurantActivity when click the user click on a restaurant
    // (from the map or list), a workmate or "your lunch" and transfer the Place id.
    public void startDetailsRestaurantActivity(Context context, String id) {
        Intent intent = new Intent(context, DetailsRestaurantActivity.class);
        intent.putExtra(KEY_ID, id);
        context.startActivity(intent);
    }
}