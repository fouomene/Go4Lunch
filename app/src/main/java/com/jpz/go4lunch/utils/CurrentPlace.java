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

    //----------------------------------------------------------------------------------

    // Interface to retrieve the list of current places when the task is complete.
    public interface CurrentPlaceListListener {
        void onPlacesFetch(List<Place> places);
    }

    //----------------------------------------------------------------------------------

    // This singleton CurrentPlace class contains the method to find places near the user location.

    private static final String TAG = CurrentPlace.class.getSimpleName();

    private static CurrentPlace ourInstance;

    private List<Place> placeList = new ArrayList<>();

    // List of listeners from the Interface
    private List<CurrentPlaceListListener> listeners = new ArrayList<>();

    // Private constructor
    private CurrentPlace() {
    }

    // If there is no instance available : create new one, else return the old instance.
    public static synchronized CurrentPlace getInstance() {
        if (ourInstance == null)
            ourInstance = new CurrentPlace();
        return ourInstance;
    }

    // Method to add a currentPlaceListListener (initialized in RestaurantMapFragment) in the list of listeners.
    public void addListener(CurrentPlaceListListener currentPlaceListListener) {

        // If the list is empty, add the listener from MapFragment in the index 0
        if (listeners.isEmpty())
            listeners.add(0, currentPlaceListListener);
        // Else there is already a listener, replace the listener from MapFragment
        else if (currentPlaceListListener.getClass().getName().contains("RestaurantMapFragment"))
            listeners.set(0, currentPlaceListListener);
        // Else there is already a listener, add the listener from ListFragment in the index 1
        else if (listeners.size() == 1 && currentPlaceListListener.getClass().getName().contains("RestaurantListFragment"))
            listeners.add(1, currentPlaceListListener);
        // Else there is already 2 listeners, replace the listener from ListFragment
        else if (listeners.size() == 2 && currentPlaceListListener.getClass().getName().contains("RestaurantListFragment"))
            listeners.set(1, currentPlaceListListener);
    }

    public void findCurrentPlace(Context context) {
        //Log.i(TAG, "placeList in findCurrentPlace = " + placeList);

        // If a list of places was already created, fetch places in the listener with it.
        if (!placeList.isEmpty()) {
            // For the currentPlaceListListener from the RestaurantMapFragment, fetch the list of places.
            for (CurrentPlaceListListener currentPlaceListListener : listeners) {
                Log.i(TAG, "currentPlaceListListener in loop = " + currentPlaceListListener);

                currentPlaceListListener.onPlacesFetch(placeList);
            }
            return;
        }

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
                                && placeLikelihood.getPlace().getTypes().contains(Place.Type.RESTAURANT)) {

                            if (placeList == null) {
                                placeList = new ArrayList<>();
                            }
                            placeList.add(placeLikelihood.getPlace());
                            Log.i(TAG, "Place found: " + placeLikelihood.getPlace());
                        }
                    }

                    // For the currentPlaceListListener from the RestaurantMapFragment, fetch the list of places.
                    for (CurrentPlaceListListener currentPlaceListListener : listeners) {
                        currentPlaceListListener.onPlacesFetch(placeList);
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