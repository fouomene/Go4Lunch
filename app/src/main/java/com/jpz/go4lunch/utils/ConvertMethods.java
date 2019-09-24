package com.jpz.go4lunch.utils;

import android.content.Context;
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

import java.util.Calendar;
import java.util.Date;

public class ConvertMethods {
    // Class to convert restaurant data

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

        Date today = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        int thisDay = calendar.get(Calendar.DAY_OF_WEEK);

        // Prevent the user if there is no data
        if (place.getOpeningHours() == null)
            closureHour = context.getString(R.string.not_disclosed);

        else switch (thisDay) {
            case 1: // Sunday
                closureHour = hourCalculation(place, DayOfWeek.SUNDAY, context);
                break;
            case 2: // Monday
                closureHour = hourCalculation(place, DayOfWeek.MONDAY, context);
                break;
            case 3: // Tuesday
                closureHour = hourCalculation(place, DayOfWeek.TUESDAY, context);
                break;
            case 4: // Wednesday
                closureHour = hourCalculation(place, DayOfWeek.WEDNESDAY, context);
                break;
            case 5: // Thursday
                closureHour = hourCalculation(place, DayOfWeek.THURSDAY, context);
                break;
            case 6: // Friday
                closureHour = hourCalculation(place, DayOfWeek.FRIDAY, context);
                break;
            case 7: // Saturday
                closureHour = hourCalculation(place, DayOfWeek.SATURDAY, context);
                break;
            default:
                closureHour = context.getString(R.string.closed_lunch_time);
        }
        return closureHour;
    }

    // Calculate closure hour
    private String hourCalculation(Place place, DayOfWeek dayOfWeek, Context context) {
        // Closed by default
        String closureHour = context.getString(R.string.closed_lunch_time);

        // Set the calendar for now
        Calendar now = Calendar.getInstance();
        Date today = now.getTime();
        now.setTime(today);

        if (place.getOpeningHours() != null)
            for (int i = 0; i < (place.getOpeningHours().getPeriods().size() - 1); i++) {

                // Verify opening day and opening hour for lunch
                if (place.getOpeningHours().getPeriods().get(i).getClose().getDay() == dayOfWeek
                        && place.getOpeningHours().getPeriods().get(i).getOpen().getTime().getHours() < 13) {

                    int restaurantCloseHour = place.getOpeningHours().getPeriods().get(i).getClose().getTime().getHours();
                    int restaurantCloseMinute = place.getOpeningHours().getPeriods().get(i).getClose().getTime().getMinutes();

                    // Set the calendar for restaurant closure hour
                    Calendar restaurantCloseCalendar = Calendar.getInstance();
                    restaurantCloseCalendar.setTimeInMillis(System.currentTimeMillis());

                    restaurantCloseCalendar.set(Calendar.HOUR_OF_DAY, restaurantCloseHour);
                    restaurantCloseCalendar.set(Calendar.MINUTE, restaurantCloseMinute);
                    restaurantCloseCalendar.set(Calendar.SECOND, 0);

                    Date restaurantDate = restaurantCloseCalendar.getTime();
                    restaurantCloseCalendar.setTime(restaurantDate);

                    // Calculation between the closing time of the restaurant and now
                    long diff = restaurantCloseCalendar.getTimeInMillis() - now.getTimeInMillis();
                   
                    // If the difference is less than 30 minutes, prevent the user
                    if (diff > 0 && diff < 30 * 60 * 1000)
                        closureHour = context.getString(R.string.closing_soon);

                    // If difference is negative, the restaurant is closed
                    else if (diff < 0)
                        closureHour = context.getString(R.string.closed_lunch_time);

                    // Else display closure hour
                    else closureHour = "Open until " + restaurantCloseHour + "." + restaurantCloseMinute + "pm";
                }
            }
        return closureHour;
    }

}
