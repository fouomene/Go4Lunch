package com.jpz.go4lunch.utils;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class MySharedPreferences {

    // Use to store data for notifications

    private SharedPreferences prefs;

    // Keys for the notifications
    private static final String NOTIFICATION_KEY = "NOTIFICATION_KEY";

    // Constructor
    public MySharedPreferences(Context context){
        prefs = context.getSharedPreferences("Preferences", MODE_PRIVATE);
    }

    // Save the state of notification
    public void saveNotificationState(Boolean isChecked) {
        prefs.edit().putBoolean(NOTIFICATION_KEY, isChecked).apply();
    }

    // Get the state of notification
    public Boolean getNotificationState() {
        return prefs.getBoolean(NOTIFICATION_KEY, false);
    }

}
