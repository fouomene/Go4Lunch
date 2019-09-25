package com.jpz.go4lunch.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.activities.DetailsRestaurantActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ConvertMethods {
    // Class to convert restaurant data

    // Key for Intent
    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";

    private static final String TAG = ConvertMethods.class.getSimpleName();

    // Format the address with the components
    public String getAddress(Place place) {
        String streetNumber = null;
        String route = null;
        String address = null;
        if (place.getAddressComponents() != null) {
            for (AddressComponent addressComponent : place.getAddressComponents().asList()) {
                if (addressComponent.getTypes().get(0).equals("street_number")) {
                    streetNumber = addressComponent.getName();
                }
                if (addressComponent.getTypes().get(0).equals("route"))
                    route = addressComponent.getName();
            }
            address = streetNumber + " " + route;
        }
        return address;
    }

    // Load photo and display it
    public void fetchPhoto(PlacesClient placesClient, PhotoMetadata photo, ImageView imageView) {
        // Create a FetchPhotoRequest.
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo)
                .build();

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            imageView.setImageBitmap(bitmap);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + statusCode + exception.getMessage());
            }
        });
    }

    // Recover the closer hour
    public String closureHour(Place place, Context context) {
        String closureHour;

        Calendar calendar = Calendar.getInstance();
        // Set date format for full weekday
        String dateFormat = "EEEE";
        // Create a SimpleDateFormat with full weekday
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        // Get the weekday in uppercase
        String weekday = sdf.format(calendar.getTime()).toUpperCase();

        // Prevent the user if there is no data
        if (place.getOpeningHours() == null)
            closureHour = context.getString(R.string.not_disclosed);
        // Else get today's closing time
        else closureHour = hourCalculation(place, DayOfWeek.valueOf(weekday),context);

        return closureHour;
    }

    // Calculate closure hour
    private String hourCalculation(Place place, DayOfWeek dayOfWeek, Context context) {
        // Closed by default
        String closureHour = context.getString(R.string.closed);

        // Set the calendar for now
        Calendar now = Calendar.getInstance();
        Date today = now.getTime();
        now.setTime(today);

        if (place.getOpeningHours() != null)
            for (int i = 0; i < (place.getOpeningHours().getPeriods().size() - 1); i++) {

                // Verify opening day and opening hour for lunch
                if (place.getOpeningHours().getPeriods().get(i).getOpen().getDay() == dayOfWeek
                        && place.getOpeningHours().getPeriods().get(i).getOpen().getTime().getHours() < 13) {

                    int restaurantCloseHour = place.getOpeningHours().getPeriods().get(i).getClose().getTime().getHours();
                    int restaurantCloseHourPM = (place.getOpeningHours().getPeriods().get(i).getClose().getTime().getHours() - 12);
                    int restaurantCloseMinute = place.getOpeningHours().getPeriods().get(i).getClose().getTime().getMinutes();

                    // Set the calendar for restaurant closure hour
                    Calendar restaurantCloseCalendar = Calendar.getInstance();
                    restaurantCloseCalendar.setTimeInMillis(System.currentTimeMillis());

                    restaurantCloseCalendar.set(Calendar.HOUR_OF_DAY, restaurantCloseHour);
                    restaurantCloseCalendar.set(Calendar.MINUTE, restaurantCloseMinute);
                    restaurantCloseCalendar.set(Calendar.SECOND, 0);

                    //Date restaurantDate = restaurantCloseCalendar.getTime();
                    //restaurantCloseCalendar.setTime(restaurantDate);

                    // Calculation between the closing time of the restaurant and now
                    long diff = restaurantCloseCalendar.getTimeInMillis() - now.getTimeInMillis();

                    // If the difference is less than 30 minutes, prevent the user
                    if (diff > 0 && diff < 30 * 60 * 1000)
                        closureHour = context.getString(R.string.closing_soon);

                    // If difference is negative, the restaurant is closed for lunch time
                    else if (diff < 0)
                        closureHour = context.getString(R.string.closed);

                    // Else display closure hour without the minutes
                    else if (restaurantCloseMinute == 0)
                        closureHour = context.getString(R.string.open_until_hour, restaurantCloseHourPM);

                    // Else display closure hour with the hours and minutes
                    else closureHour = context.getString(R.string.open_until_hour_minute, restaurantCloseHourPM, restaurantCloseMinute);
                }
            }
        return closureHour;
    }

    // Start DetailsRestaurantActivity when click the user click on a restaurant (from the map or list)
    public void startDetailsRestaurantActivity(Context context, String restaurantId) {
        Intent intent = new Intent(context, DetailsRestaurantActivity.class);
        intent.putExtra(KEY_RESTAURANT_ID, restaurantId);
        context.startActivity(intent);
    }

}
