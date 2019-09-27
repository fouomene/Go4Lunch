package com.jpz.go4lunch.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.activities.DetailsRestaurantActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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

    //--------------------------------------------------------------------------------------

    // Load a photo and display it
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

    //--------------------------------------------------------------------------------------

    // Recover the opening hours
    public String openingHours(Place place, Context context) {

        String openingHours;

        Calendar calendar = Calendar.getInstance();
        // Set date format for full weekday
        String dateFormat = "EEEE";
        // Create a SimpleDateFormat with full weekday
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        // Get the weekday in uppercase
        String weekday = sdf.format(calendar.getTime()).toUpperCase();

        // Prevent the user if there is no data
        if (place.getOpeningHours() == null)
            openingHours = context.getString(R.string.not_disclosed);
        // Else get today's closing time
        else openingHours = getHours(getPeriods(place, DayOfWeek.valueOf(weekday)), context);

        return openingHours;
    }

    // Get a list of periods
    private List<Period> getPeriods(Place place, DayOfWeek dayOfWeek) {

        List<Period> periodList = new ArrayList<>();

        if (place.getOpeningHours() != null)
            for (int i = 0; i < (place.getOpeningHours().getPeriods().size()); i++) {
                // If today the restaurant is open, get the periods
                if (place.getOpeningHours().getPeriods().get(i).getOpen().getDay() == dayOfWeek)
                    periodList.add(place.getOpeningHours().getPeriods().get(i));
            }
        return periodList;
    }

    // Calculate closure hour
    private String getHours(List<Period> periodList, Context context) {

        String closureHour;
        // Set the calendar for now
        Calendar actualHour = Calendar.getInstance();
        Date today = actualHour.getTime();
        actualHour.setTime(today);

        List<Integer> closeTimeList = new ArrayList<>();

        // If there is no period, the restaurant is closed
        if (periodList.isEmpty())
            closureHour = context.getString(R.string.closed);

        // If there is one period, calculate the closure hour
        else if (periodList.size() == 1)
            closureHour = displayHours(periodList.get(0), context);

        // If there is several period, compare closing hours
        else {
            // Get a comparison of a list of closing periods
            for (int i = 0; i < periodList.size(); i++) {
                int closeTime = periodList.get(i).getClose().getTime().getHours();
                // Add the close time a list
                closeTimeList.add(closeTime);
            }

            // Then sort the list by order
            Collections.sort(closeTimeList, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o1.compareTo(o2);
                }
            });

            // And get closureHour from the first index of the list
            closureHour = displayHours(periodList.get(0), context);
        }
        return closureHour;
    }

    // Manage UI
    private String displayHours(Period period, Context context){

        // Closed by default
        String openingHours = context.getString(R.string.closed);

        // Set the calendar for now
        Calendar actualHour = Calendar.getInstance();
        Date today = actualHour.getTime();
        actualHour.setTime(today);

        if (period.getOpen() != null && period.getClose() != null) {

            // Get the opening hours of the period
            int openHour = period.getOpen().getTime().getHours();
            int openMinute = period.getOpen().getTime().getMinutes();
            // Set the calendar with opening time
            Calendar openCalendar = Calendar.getInstance();
            openCalendar.setTimeInMillis(System.currentTimeMillis());
            openCalendar.set(Calendar.HOUR_OF_DAY, openHour);
            openCalendar.set(Calendar.MINUTE, openMinute);
            openCalendar.set(Calendar.SECOND, 0);

            // Get the closing hours of the period
            int closeHour = period.getClose().getTime().getHours();
            int closeMinute = period.getClose().getTime().getMinutes();
            int closeHourPM = (period.getClose().getTime().getHours() - 12);
            // Set the calendar with closure hour
            Calendar closeCalendar = Calendar.getInstance();
            closeCalendar.setTimeInMillis(System.currentTimeMillis());
            closeCalendar.set(Calendar.HOUR_OF_DAY, closeHour);
            closeCalendar.set(Calendar.MINUTE, closeMinute);
            closeCalendar.set(Calendar.SECOND, 0);

            // Comparison between the closing time of the restaurant and the actual hour
            long comparison = closeCalendar.getTimeInMillis() - actualHour.getTimeInMillis();

            // If comparison is negative, the restaurant is closed
            if (comparison < 0)
                openingHours = context.getString(R.string.closed);

            // Else if the comparison is positive but is less than 30 minutes, prevent the user of close closure
            else if (comparison > 0 && comparison < 30 * 60 * 1000)
                openingHours = context.getString(R.string.closing_soon);

            // Else if the restaurant is open and the closure hour is "time o'clock", display the closure hour without the minutes
            else if (actualHour.getTimeInMillis() >= openCalendar.getTimeInMillis() && closeMinute == 0)
                openingHours = context.getString(R.string.open_until_hour, closeHourPM);

            // Else if the restaurant is open and display the closure hour with the hours and minutes
            else if (actualHour.getTimeInMillis() >= openCalendar.getTimeInMillis())
                openingHours = context.getString(R.string.open_until_hour_minute, closeHourPM, closeMinute);

            // Otherwise, the restaurant is not open for the moment
            else openingHours = context.getString(R.string.not_open_yet);
        }
        return openingHours;
    }

    //--------------------------------------------------------------------------------------

    // Start DetailsRestaurantActivity when click the user click on a restaurant (from the map or list)
    public void startDetailsRestaurantActivity(Context context, String restaurantId) {
        Intent intent = new Intent(context, DetailsRestaurantActivity.class);
        intent.putExtra(KEY_RESTAURANT_ID, restaurantId);
        context.startActivity(intent);
    }

}
