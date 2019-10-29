package com.jpz.go4lunch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
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
    public interface CurrentPlacesListener {
        void onPlacesFetch(List<Place> places);
    }

    // List of places for the CurrentPlaceListListener
    private List<Place> placeList = new ArrayList<>();

    // List of listener from the CurrentPlacesListener
    private List<CurrentPlacesListener> currentPlacesListeners = new ArrayList<>();

    // Method to add a CurrentPlacesListener (initialized in the map or list Fragment).
    public void addListener(CurrentPlacesListener currentPlacesListener) {
        //this.currentPlacesListener = currentPlacesListener;
        currentPlacesListeners.add(currentPlacesListener);
    }

    // Method to remove a PlacesDetailsListener (initialized in the map or list fragment) from the list of listeners.
    public void removeListener(CurrentPlacesListener currentPlacesListener) {
        currentPlacesListeners.remove(currentPlacesListener);
    }

    //----------------------------------------------------------------------------------

    // Interface to retrieve the details of a list of place when the task is complete.
    public interface PlaceDetailsListener {
        //void onPlacesDetailsFetch(List<Place> places);
        void onPlaceDetailsFetch(Place place);
    }

    // Listener from the PlaceDetailsListener
    private PlaceDetailsListener placeDetailsListener;

    // Method to add a PlaceDetailsListener (initialized in list Fragment or the details Activity)
    public void addDetailsListener(PlaceDetailsListener placeDetailsListener) {
        this.placeDetailsListener = placeDetailsListener;
    }

    //----------------------------------------------------------------------------------

    // Interface to retrieve the photo from a list of places when the task is complete.
    public interface PlacePhotoListener {
        void onPhotoFetch(Bitmap bitmap);
    }

    //----------------------------------------------------------------------------------

    /**
     * This singleton CurrentPlace class contains the methods :
     * - to find the places near the user location,
     * - to get the details of a place,
     * - to fetch a photo of a place.
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

    public void findCurrentPlace() {

        Log.i(TAG, "placeList in findCurrentPlace = " + placeList);
        // If a list of places was already created, fetch places in the listener with it.
        if (!placeList.isEmpty()) {
            // For the currentPlaceListListener from the map or list fragment, fetch the list of places.
            for (CurrentPlacesListener currentPlacesListener : currentPlacesListeners) {
                Log.i(TAG, "currentPlaceListListener in loop = " + currentPlacesListener);
                currentPlacesListener.onPlacesFetch(placeList);
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

                            placeList.add(placeLikelihood.getPlace());
                            Log.i(TAG, "Place found: " + placeLikelihood.getPlace());
                        }
                    }

                    // Attach the placeList (which contains the results of the request)
                    for (CurrentPlacesListener currentPlacesListener : currentPlacesListeners) {
                        currentPlacesListener.onPlacesFetch(placeList);
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

    public void findDetailsPlaces(String id) {
        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.OPENING_HOURS, Place.Field.ADDRESS_COMPONENTS, Place.Field.PHOTO_METADATAS,
                Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.RATING);

        if (id != null) {
            // Construct a request object, passing the place ID and fields array.
            request = FetchPlaceRequest.newInstance(id, placeFields);
        }
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place placeDetails = response.getPlace();
            Log.i(TAG, "Place details found: " + placeDetails.getName());

            // Attach the placeDetails
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

    //----------------------------------------------------------------------------------

    public void autoComplete(String query, LatLng latLng) {

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Create a RectangularBounds object.
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(latLng.latitude - 0.01, latLng.longitude - 0.01),
                new LatLng(latLng.latitude + 0.01, latLng.longitude + 0.01));
        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setCountry("fr") // France
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(predictionsRequest).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.i(TAG, prediction.getPlaceId());
                Log.i(TAG, prediction.getPrimaryText(null).toString());
            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
            }
        });
    }

}