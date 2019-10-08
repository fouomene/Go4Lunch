package com.jpz.go4lunch.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.MyUtils;

import static com.jpz.go4lunch.utils.MyUtils.KEY_PLACE;


public class DetailsRestaurantActivity extends AppCompatActivity {

    // Widgets
    private TextView name, opinions, type, address;
    private ImageView restaurantImage;
    private Button call, like, website;

    // Places
    private Place place;

    // Utils
    private MyUtils myUtils = new MyUtils();

    private String phoneNumber;
    private Uri uriWebsite;
    boolean fabIsChecked = false;

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

        // Get the transferred Place data from the source activity
        Intent intent = getIntent();
        place = intent.getParcelableExtra(KEY_PLACE);

        updateUI();

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
            if (uriWebsite == null) {
                Toast.makeText(this, getString(R.string.no_website), Toast.LENGTH_SHORT).show();
            } else {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriWebsite.toString()));
                startActivity(webIntent);
            }
        });

        FloatingActionButton floatingActionButton = findViewById(R.id.details_fab);
        floatingActionButton.setOnClickListener((View v) -> {
            if (!fabIsChecked) {
                floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_circle));
                fabIsChecked = true;
            } else {
                floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_highlight_off));
                fabIsChecked = false;
            }
        });
    }

    private void updateUI() {
        // Get Place data
        name.setText(place.getName());
        address.setText(myUtils.getAddress(place));
        phoneNumber = place.getPhoneNumber();
        uriWebsite = place.getWebsiteUri();
        Log.i(TAG, "Uri " + place.getWebsiteUri());

        // Get the photo metadata
        if (place.getPhotoMetadatas() != null) {
            PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
            myUtils.findAndFetchPhoto(this, photoMetadata, restaurantImage);
        }
    }

}
