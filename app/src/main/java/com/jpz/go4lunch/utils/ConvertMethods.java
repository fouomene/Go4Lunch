package com.jpz.go4lunch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.activities.DetailsRestaurantActivity;

import java.util.Calendar;
import java.util.Date;

public class ConvertMethods {
    // Class to convert restaurant data

    private static final String TAG = DetailsRestaurantActivity.class.getSimpleName();

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

    // Recover the day of week
    public String recoverDayOfWeek(Place place, Context context) {

        String todayTime = "";

        Date today = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (place.getOpeningHours() == null)
            todayTime = context.getString(R.string.not_disclosed);

        else switch (dayOfWeek) {
            case 1: // Sunday
                todayTime = place.getOpeningHours().getWeekdayText().get(6);
                break;
            case 2: // Monday
                todayTime = place.getOpeningHours().getWeekdayText().get(0);
                break;
            case 3: // Tuesday
                todayTime = place.getOpeningHours().getWeekdayText().get(1);
                break;
            case 4: // Wednesday
                todayTime = place.getOpeningHours().getWeekdayText().get(2);
                break;
            case 5: // Thursday
                todayTime = place.getOpeningHours().getWeekdayText().get(3);
                break;
            case 6: // Friday
                todayTime = place.getOpeningHours().getWeekdayText().get(4);
                break;
            case 7: // Saturday
                todayTime = place.getOpeningHours().getWeekdayText().get(5);
                break;
        }
        return todayTime;
    }

}
