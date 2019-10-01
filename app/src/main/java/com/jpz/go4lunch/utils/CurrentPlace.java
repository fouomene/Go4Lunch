package com.jpz.go4lunch.utils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
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

    // Interface to retrieve the details of a place when the task is complete.
    public interface PlaceDetailsListener {
        void onPlaceDetailsFetch(List<Place> placeList);
    }

    //----------------------------------------------------------------------------------

    /**
    This singleton CurrentPlace class contains the methods :
     - to find the places near the user location,
     - to get the details of these places.
     */

    private static final String TAG = CurrentPlace.class.getSimpleName();

    private static CurrentPlace ourInstance;

    // List of places for the CurrentPlaceListListener
    private List<Place> placeList = new ArrayList<>();
    // List of listeners from the CurrentPlaceListListener
    private List<CurrentPlaceListListener> listeners = new ArrayList<>();

    // List of places for the PlaceDetailsListener
    private List<Place> placeDetailsList = new ArrayList<>();
    // Listener from the PlaceDetailsListener
    private PlaceDetailsListener placeDetailsListener;

    //
    private PlacesClient placesClient;
    private FetchPlaceRequest request;

    // Private constructor
    private CurrentPlace(Context context) {
        // Initialize the SDK
        Places.initialize(context.getApplicationContext(), context.getString(R.string.google_api_key));
        // Create a new Places client instance
        placesClient = Places.createClient(context.getApplicationContext());
    }

    // If there is no instance available : create new one, else return the old instance.
    public static synchronized CurrentPlace getInstance(Context context) {
        if (ourInstance == null)
            ourInstance = new CurrentPlace(context);
        return ourInstance;
    }

    //----------------------------------------------------------------------------------

    // Method to add a currentPlaceListListener (initialized in the map or list fragment) in the list of listeners.
    public void addListener(CurrentPlaceListListener currentPlaceListListener) {
        listeners.add(currentPlaceListListener);
    }

    // Method to remove a currentPlaceListListener (initialized in the map or list fragment) in the list of listeners.
    public void removeListener(CurrentPlaceListListener currentPlaceListListener) {
        listeners.remove(currentPlaceListListener);
    }

    //----------------------------------------------------------------------------------

    // Method to add a placeDetailsListener (initialized in the list fragment).
    public void addDetailsListener(PlaceDetailsListener placeDetailsListener) {
        this.placeDetailsListener = placeDetailsListener;
    }

    //----------------------------------------------------------------------------------

    public void findCurrentPlace() {
        // If a list of places was already created, fetch places in the listener with it.
        if (!placeList.isEmpty()) {
            // For the currentPlaceListListener from the map or list fragment, fetch the list of places.
            for (CurrentPlaceListListener currentPlaceListListener : listeners) {
                Log.i(TAG, "currentPlaceListListener in loop = " + currentPlaceListListener);
                currentPlaceListListener.onPlacesFetch(placeList);
            }
            return;
        }

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
                            //fetchDetailsPlace(placeLikelihood.getPlace().getId());
                            Log.i(TAG, "Place found: " + placeLikelihood.getPlace());
                        }
                    }
                    // For the currentPlaceListListener from the map or list fragment, fetch the list of places.
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

    //----------------------------------------------------------------------------------

    public void findDetailsPlace(List<Place> placeList) {
        // If a list of places details was already created, fetch places details in the listener with it.
        if (!placeDetailsList.isEmpty()) {
            placeDetailsListener.onPlaceDetailsFetch(placeDetailsList);
            return;
        }

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.OPENING_HOURS, Place.Field.ADDRESS_COMPONENTS, Place.Field.PHOTO_METADATAS);

        for (Place place : placeList) {
            if (place.getId() != null) {
                // Construct a request object, passing the place ID and fields array.
                request = FetchPlaceRequest.newInstance(place.getId(), placeFields);
            }
            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place placeDetails = response.getPlace();
                Log.i(TAG, "Place details found: " + placeDetails.getName());

                placeDetailsList.add(placeDetails);
                placeDetailsListener.onPlaceDetailsFetch(placeDetailsList);

                //placeList.set(index, placeDetails);
                //placeDetailsListener.onPlaceDetailsFetch(placeList);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place details not found: " + exception.getMessage());
                }
            });
        }
    }

}