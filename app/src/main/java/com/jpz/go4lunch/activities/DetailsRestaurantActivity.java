package com.jpz.go4lunch.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.ConvertMethods;

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

    // Utils
    private ConvertMethods convertMethods;

    private String phoneNumber;
    private Uri uriWebsite;

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
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME,Place.Field.ADDRESS_COMPONENTS,
                Place.Field.PHOTO_METADATAS, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);

        // Construct a request object, passing the place ID and fields array.
        request = FetchPlaceRequest.newInstance(placeId, placeFields);

        // Create a new Places client instance
        placesClient = Places.createClient(this);

        fetchPlace();

        // Display the phone number
        call.setOnClickListener((View v) -> {
            String dial = "tel:" + phoneNumber;
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
        });

        like.setOnClickListener((View v) -> {
            // Save like on Firebase
        });

        // Display the website
        website.setOnClickListener((View v) -> {
            if (uriWebsite == null)
                Toast.makeText(this, getString(R.string.no_website), Toast.LENGTH_SHORT).show();
            else {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriWebsite.toString()));
                startActivity(webIntent);
            }
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.details_fab);
        floatingActionButton.setOnClickListener((View v) -> {
                boolean isChecked = true;
                if (isChecked)
                    floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_circle));
                else floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_highlight_off));
        });
    }

    private void fetchPlace() {
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();

            // Get data
            name.setText(place.getName());
            address.setText(convertMethods.getAddress(place));
            phoneNumber = place.getPhoneNumber();
            uriWebsite = place.getWebsiteUri();
            Log.i(TAG, "Uri " + place.getWebsiteUri());

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
