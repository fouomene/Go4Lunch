package com.jpz.go4lunch.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;

import java.util.Arrays;
import java.util.List;

import static com.jpz.go4lunch.fragments.RestaurantMapFragment.KEY_RESTAURANT_ID;

public class DetailsRestaurantActivity extends AppCompatActivity {

    // Widgets
    private TextView name, opinions, type, address;
    private ImageView restaurantImage;
    private Button call, like, website;

    // Places
    private PlacesClient placesClient;
    private FetchPlaceRequest request;
    private PhotoMetadata photoMetadata;

    private static final String TAG = DetailsRestaurantActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_restaurant);

        name = findViewById(R.id.details_name);
        type = findViewById(R.id.details_type);
        address = findViewById(R.id.details_address);
        opinions = findViewById(R.id.details_opinions);

        restaurantImage = findViewById(R.id.details_image_restaurant);

        call = findViewById(R.id.details_button_call);
        like = findViewById(R.id.details_button_like);
        website = findViewById(R.id.details_button_website);

        // Get the transferred data from the source activity and define a Place ID.
        Intent intent = getIntent();
        String placeId = intent.getStringExtra(KEY_RESTAURANT_ID);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.PHOTO_METADATAS,Place.Field.ADDRESS);

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

            // Get data
            name.setText(place.getName());
            address.setText(place.getAddress());

            // Get the photo metadata.
            if (place.getPhotoMetadatas() != null)
                photoMetadata = place.getPhotoMetadatas().get(0);

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .build();
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();
                restaurantImage.setImageBitmap(bitmap);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + statusCode + exception.getMessage());
                }
            });
        });
    }

}
