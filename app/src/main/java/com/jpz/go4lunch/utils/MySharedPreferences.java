package com.jpz.go4lunch.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class MySharedPreferences {
    // Use to store data for likes

    private SharedPreferences prefs;

    // Keys for the notifications
    private static final String LIKE_KEY = "LIKE_KEY";

    // Constructor
    public MySharedPreferences(Context context){
        prefs = context.getSharedPreferences("Preferences", MODE_PRIVATE);
    }

    //----------------------------------------------------------------------------------

    // Get a key, which is the restaurant identifier, to have a like comportment for each restaurant
    private String getRestaurantIdKey(String restaurantId) {
        return "RestaurantId_" + restaurantId;
    }

    // Save the state of the restaurant's like
    public void saveLikeState(String restaurantId, Boolean likeIsChecked) {
        prefs.edit().putBoolean(getRestaurantIdKey(restaurantId), likeIsChecked).apply();
    }

    // Get the state of the restaurant's like
    public Boolean getLikeState(String restaurantId) {
        return prefs.getBoolean(getRestaurantIdKey(restaurantId), false);
    }

}
