package com.jpz.go4lunch.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CurrentPlace {

    private static final String TAG = CurrentPlace.class.getSimpleName();
    private static CurrentPlace ourInstance;
    private ArrayList<Place> places = new ArrayList<>();

    // Private constructor
    private CurrentPlace() {
    }

    public static synchronized CurrentPlace getInstance() {
        if (ourInstance == null)
            ourInstance = new CurrentPlace();
        return ourInstance;
    }

    public ArrayList<Place> getPlaces() {
        return places;
    }

    public void findCurrentPlace(Context context) {
        /*
        if (places != null)
            return;
        */
        // Initialize the SDK
        Places.initialize(context.getApplicationContext(), context.getString(R.string.google_api_key));
        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(context.getApplicationContext());
        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.TYPES, Place.Field.LAT_LNG, Place.Field.ID);
        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        try {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse response = task.getResult();

                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {

                        if (placeLikelihood.getPlace().getTypes() != null
                                && placeLikelihood.getPlace().getLatLng() != null
                                && placeLikelihood.getPlace().getTypes()
                                .contains(Place.Type.RESTAURANT)) {

                            if (places == null) {
                                places = new ArrayList<>();
                            }
                            places.add(placeLikelihood.getPlace());
                        }
                    }

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
    }
}