package com.jpz.go4lunch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
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
    private interface CurrentPlacesListener {
        void onPlacesFetch(List<Place> places);
    }

    // List of places for the CurrentPlaceListListener
    private List<Place> placeList = new ArrayList<>();
    // Listener from the CurrentPlaceListListener
    private CurrentPlacesListener currentPlacesListener;

    // Method to add a CurrentPlacesListener (initialized in this class).
    private void addListener(CurrentPlacesListener currentPlacesListener) {
        this.currentPlacesListener = currentPlacesListener;
    }

    //----------------------------------------------------------------------------------

    // Interface to retrieve the photo from a list of places when the task is complete.
    public interface PlacePhotoListener {
        void onPhotoFetch(Bitmap bitmap);
    }

    //----------------------------------------------------------------------------------

    // Interface to retrieve the details of a list of place when the task is complete.
    public interface PlacesDetailsListener {
        void onPlacesDetailsFetch(List<Place> places);
    }

    // List of places for the PlaceDetailsListener
    private List<Place> placeDetailsList = new ArrayList<>();
    // List of listeners from the PlaceDetailsListener
    private List<PlacesDetailsListener> placeDetailsListeners = new ArrayList<>();

    // Method to add a PlacesDetailsListener (initialized in the map or list fragment) in the list of listeners.
    public void addDetailsListener(PlacesDetailsListener placeDetailsListener) {
        placeDetailsListeners.add(placeDetailsListener);
    }

    // Method to remove a PlacesDetailsListener (initialized in the map or list fragment) from the list of listeners.
    public void removeListener(PlacesDetailsListener placeDetailsListener) {
        placeDetailsListeners.remove(placeDetailsListener);
    }

    //----------------------------------------------------------------------------------

    /**
    This singleton CurrentPlace class contains the methods :
     - to find the places near the user location,
     - to get the details of these places.
     */

    private static final String TAG = CurrentPlace.class.getSimpleName();

    private static CurrentPlace ourInstance;

    // Places
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

    private void findCurrentPlace() {
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

                    // Attach the placeList (which contains the results of the request)
                    currentPlacesListener.onPlacesFetch(placeList);

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

    public void findPhotoPlace(PhotoMetadata photo, PlacePhotoListener placePhotoListener) {
        // Create a FetchPhotoRequest.
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photo)
                .build();
        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
            Bitmap bitmap = fetchPhotoResponse.getBitmap();
            // Attach the bitmap (photo from the request)
            placePhotoListener.onPhotoFetch(bitmap);

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + statusCode + exception.getMessage());
            }
        });
    }

    //----------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------

    public void findDetailsPlaces(String id) {
        // If a list of places details was already created, fetch places details in the listener with it.
        if (!placeDetailsList.isEmpty()) { //&& id == null) {
            // For the placeDetailsListener from the map or list fragment, fetch the list of places.
            for (PlacesDetailsListener placeDetailsListener : placeDetailsListeners) {
                Log.i(TAG, "placeDetailsListener in loop = " + placeDetailsListener);
                placeDetailsListener.onPlacesDetailsFetch(placeDetailsList);
            }
            return;
        }

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS, Place.Field.ADDRESS_COMPONENTS, Place.Field.PHOTO_METADATAS,
                Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);

        // If there is no id, use the CurrentPlace request to find identifiers from the local restaurants
        if (id == null) {
            // Call the CurrentPlace request...
            findCurrentPlace();
            // ...and use these places to call PlaceDetails requests
            this.addListener(places -> {

                for (Place place : places) {
                    if (place.getId() != null) {
                        // Construct a request object, passing the place ID and fields array.
                        request = FetchPlaceRequest.newInstance(place.getId(), placeFields);
                    }
                    placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                        Place placeDetails = response.getPlace();
                        Log.i(TAG, "Place details found: " + placeDetails.getName());

                        placeDetailsList.add(placeDetails);
                        // For the PlaceDetailsListener from the map or list fragment, fetch the list of places and bitmaps.
                        for (PlacesDetailsListener placeDetailsListener : placeDetailsListeners) {
                            placeDetailsListener.onPlacesDetailsFetch(placeDetailsList);
                        }

                    }).addOnFailureListener((exception) -> {
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            int statusCode = apiException.getStatusCode();
                            // Handle error with given status code.
                            Log.e(TAG, "Place details not found: " + exception.getMessage());
                        }
                    });
                }
            });
        // Else use the id to fetch details for the restaurant
        } else {
            // Construct a request object, passing the place ID and fields array.
            request = FetchPlaceRequest.newInstance(id, placeFields);

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place placeDetails = response.getPlace();
                Log.i(TAG, "Place details found: " + placeDetails.getName());

                placeDetailsList.add(placeDetails);
                // For the PlaceDetailsListener from the map or list fragment, fetch the list of places and bitmaps.
                for (PlacesDetailsListener placeDetailsListener : placeDetailsListeners) {
                    placeDetailsListener.onPlacesDetailsFetch(placeDetailsList);
                }

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