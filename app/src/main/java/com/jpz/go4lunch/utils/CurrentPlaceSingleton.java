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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CurrentPlaceSingleton {

    private static CurrentPlaceSingleton ourInstance;

    private ArrayList<LatLng> latLngList = new ArrayList<>();
    private ArrayList<String> listId = new ArrayList<>();

    private static final String TAG = CurrentPlaceSingleton.class.getSimpleName();

    // Private constructor
    private CurrentPlaceSingleton(Context context) {
        getFindCurrentPlace(context.getApplicationContext());
    }

    public static synchronized CurrentPlaceSingleton getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new CurrentPlaceSingleton(context);
        return ourInstance;
    }

    private void getFindCurrentPlace(Context context) {
        findCurrentPlace(context);
    }

    public ArrayList<String> getIdCurrentPlace() {
        return listId;
    }

    public ArrayList<LatLng> getLatLngCurrentPlace() {
        return latLngList;
    }

    private void findCurrentPlace(Context context) {
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

                            // Collect the LatLng of the places likelihood
                            LatLng latLng = new LatLng
                                    (placeLikelihood.getPlace().getLatLng().latitude,
                                            placeLikelihood.getPlace().getLatLng().longitude);
                            latLngList.add(latLng);
                            Log.i(TAG, "Place's latLng = " + latLng);

                            // Collect the identities of the places likelihood
                            String id = placeLikelihood.getPlace().getId();
                            listId.add(id);
                            Log.i(TAG, "Place's id = " + id);
                            Log.i(TAG, "list of id's = " + listId);
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