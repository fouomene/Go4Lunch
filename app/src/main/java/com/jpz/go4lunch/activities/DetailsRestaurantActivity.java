package com.jpz.go4lunch.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.models.FieldRestaurant;

import java.util.Arrays;
import java.util.List;

import static com.jpz.go4lunch.fragments.RestaurantMapFragment.KEY_RESTAURANT_ID;

public class DetailsRestaurantActivity extends AppCompatActivity {

    // Places
    private PlacesClient placesClient;
    private FetchPlaceRequest request;

    private FieldRestaurant fieldRestaurant = new FieldRestaurant();

    private static final String TAG = DetailsRestaurantActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_restaurant);

        // Get the transferred data from the source activity and define a Place ID.
        Intent intent = getIntent();
        String placeId = intent.getStringExtra(KEY_RESTAURANT_ID);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Construct a request object, passing the place ID and fields array.
        request = FetchPlaceRequest.newInstance(placeId, placeFields);

        // Create a new Places client instance
        placesClient = Places.createClient(this);

        fetchPlace();

    }

    private void fetchPlace() {
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + statusCode + exception.getMessage());
            }
        });
    }
}
