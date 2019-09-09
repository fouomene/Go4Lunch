package com.jpz.go4lunch.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;

import java.util.Arrays;
import java.util.List;


public class CurrentPlaceSingleton {

    private static CurrentPlaceSingleton ourInstance;

    private LatLng restaurantLatLng;
    private String id;

    private static final String TAG = CurrentPlaceSingleton.class.getSimpleName();

    // Private constructor
    private CurrentPlaceSingleton(Context context) {
        context = context.getApplicationContext();
    }

    public static synchronized CurrentPlaceSingleton getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new CurrentPlaceSingleton(context);

        return ourInstance;
    }

    public String getFindCurrentPlace(Context context) {
        return findCurrentPlace(context);
    }

    private String findCurrentPlace(Context context) {

        // Initialize the SDK
        Places.initialize(context.getApplicationContext(), context.getString(R.string.google_api_key));

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(context.getApplicationContext());

        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.TYPES, Place.Field.NAME, Place.Field.ID);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        try {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse response = task.getResult();
                    Log.i(TAG, "response = " + response.getPlaceLikelihoods().get(0));

                    if (response.getPlaceLikelihoods().get(0).getPlace().getTypes() != null &&
                            response.getPlaceLikelihoods().get(0).getPlace().getTypes().contains(Place.Type.RESTAURANT))
                        id = response.getPlaceLikelihoods().get(0).getPlace().getId();

                    /*
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {

                        if (placeLikelihood.getPlace().getTypes() != null
                                && placeLikelihood.getPlace().getLatLng() != null
                                && placeLikelihood.getPlace().getTypes()
                                .contains(Place.Type.RESTAURANT)) {

                            // Collect the LatLng of the places likelihood
                            restaurantLatLng = new LatLng
                                    (placeLikelihood.getPlace().getLatLng().latitude,
                                            placeLikelihood.getPlace().getLatLng().longitude);

                            // Collect the identities of the places likelihood
                            id = placeLikelihood.getPlace().getId();
                            Log.i("Tag", "Place has id = " + fieldRestaurant.id);

                        }
                    }
                    */

                } else {
                    Exception exception = task.getException();
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                    }
                }
            });

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }

        Log.i(TAG, "return the id = " + id);
        return id;
    }



}
