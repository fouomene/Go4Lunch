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
        void onPlaceDetailsFetch(Place placeDetails);
    }

    //----------------------------------------------------------------------------------

    // This singleton CurrentPlace class contains the method to find places near the user location.

    private static final String TAG = CurrentPlace.class.getSimpleName();

    private static CurrentPlace ourInstance;

    private List<Place> placeList = new ArrayList<>();

    private List<Place> placeDetailsList = new ArrayList<>();

    private PlaceDetailsListener placeDetailsListener;

    //private List<PlaceDetailsListener> placeDetailsListenerList = new ArrayList<>();

    // List of listeners from the CurrentPlaceListListener
    private List<CurrentPlaceListListener> listeners = new ArrayList<>();

    // List of listeners from the Interface
    private List<PlaceDetailsListener> detailsListenerList = new ArrayList<>();

    // Private constructor
    private CurrentPlace() {
    }

    // If there is no instance available : create new one, else return the old instance.
    public static synchronized CurrentPlace getInstance() {
        if (ourInstance == null)
            ourInstance = new CurrentPlace();
        return ourInstance;
    }

    //----------------------------------------------------------------------------------

    // Method to add a currentPlaceListListener (initialized in th map or list fragment) in the list of listeners.
    public void addListener(CurrentPlaceListListener currentPlaceListListener) {
        listeners.add(currentPlaceListListener);
    }

    // Method to remove a currentPlaceListListener (initialized in th map or list fragment) in the list of listeners.
    public void removeListener(CurrentPlaceListListener currentPlaceListListener) {
        listeners.remove(currentPlaceListListener);
    }

    //----------------------------------------------------------------------------------

    // Method to add a currentPlaceListListener (initialized in th map or list fragment) in the list of listeners.
    public void addDetailsListener(PlaceDetailsListener placeDetailsListener) {
        //detailsListenerList.add(placeDetailsListener);
        this.placeDetailsListener = placeDetailsListener;

    }

    // Method to remove a currentPlaceListListener (initialized in th map or list fragment) in the list of listeners.
    public void removeDetailsListener(PlaceDetailsListener placeDetailsListener) {
        //detailsListeners.remove(placeDetailsListener);
    }

    //----------------------------------------------------------------------------------

    public void findCurrentPlace(Context context) {
        //Log.i(TAG, "placeList in findCurrentPlace = " + placeList);

        // If a list of places was already created, fetch places in the listener with it.
        if (!placeList.isEmpty()) {
            // For the currentPlaceListListener from the map or list fragment, fetch the list of places.
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

    public void fetchDetailsPlace(Place place, PlacesClient placesClient, FetchPlaceRequest request) {

        /*
        // If a placeDetail was already created, fetch it in the listener.
        if (placeDetail == null) {
            // For the currentPlaceDetailsListener from the RestaurantListFragment, fetch the place.
            for (PlaceDetailsListener placeDetailsListener : detailsListeners) {
                Log.i(TAG, "placeDetailsListener in loop = " + placeDetailsListener);
                placeDetailsListener.onPlaceDetailsFetch(placeDetails);
            }
            return;
        }
         */

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.OPENING_HOURS,
                Place.Field.ADDRESS_COMPONENTS, Place.Field.PHOTO_METADATAS);

        if (place.getId() != null)
            // Construct a request object, passing the place ID and fields array.
            request = FetchPlaceRequest.newInstance(place.getId(), placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place placeDetails = response.getPlace();
            Log.i(TAG, "Place details found: " + placeDetails.getName());

            //placeDetailsList.add(placeDetails);

            /*
            // For the placeDetailsListener from the ViewHolder, fetch the placeDetails.
            for (PlaceDetailsListener placeDetailsListener : detailsListenerList) {
                placeDetailsList.clear();
                placeDetailsList.add(placeDetails);
                placeDetailsListener.onPlaceDetailsFetch(placeDetailsList.get(0));
                Log.i(TAG, "detailsListenerList.size in fetchPlace = " + detailsListenerList.size());

                //placeDetailsListener.onPlaceDetailsFetch(placeDetailsList);
                Log.i(TAG, "detailsListenerList in fetchPlace = " + detailsListenerList);
                //Log.w(TAG, "placeDetailsList in fetchPlace = " + placeDetailsList);
            }

             */

            /*
            for (int i = 0; i < detailsListenerList.size(); i++) {
                //placeDetailsList.add(placeDetails);
                placeDetailsListener.onPlaceDetailsFetch(placeDetails);
            }
             */

            // For the placeDetailsListener from the ViewHolder, fetch the place.
            placeDetailsListener.onPlaceDetailsFetch(placeDetails);

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