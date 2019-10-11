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

import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.api.WorkmateHelper;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.FirebaseUtils;

import static com.jpz.go4lunch.utils.MyUtilsNavigation.KEY_PLACE;


public class DetailsRestaurantActivity extends AppCompatActivity implements CurrentPlace.PlacePhotoListener {

    // Widgets
    private TextView name, opinions, type, address;
    private ImageView restaurantImage;
    private Button call, like, website;

    // Places
    private Place place;

    // Models nd Api
    private Workmate workmate = new Workmate();
    private WorkmateHelper workmateHelper = new WorkmateHelper();

    // Utils
    private ConvertData convertData = new ConvertData();
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    private String phoneNumber;
    private Uri uriWebsite;
    boolean fabIsChecked;

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


        // Set the restaurant choice from Firestore
        if (firebaseUtils.getCurrentUser() != null) {
            workmateHelper.getRestaurantChoice(firebaseUtils.getCurrentUser().getUid());
        }

        FloatingActionButton floatingActionButton = findViewById(R.id.details_fab);
        // Check the choice of the restaurant and display the button according to the choice
        verifyRestaurantChoice();
        Log.i(TAG, "restaurant choice = " + workmate.getSelectedPlace());

        if (fabIsChecked) {
            floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_circle));
        } else floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_highlight_off));

        // Listen to the user choice when click on the floatingActionButton
        floatingActionButton.setOnClickListener((View v) -> {
            if (!fabIsChecked) {
                floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_circle));
                chooseRestaurant();
                fabIsChecked = true;
            } else {
                floatingActionButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_highlight_off));
                clearRestaurantChoice();
                fabIsChecked = false;
            }
        });
    }

    private void updateUI() {
        // Get Place data
        name.setText(place.getName());
        address.setText(convertData.getAddress(place));
        phoneNumber = place.getPhoneNumber();
        uriWebsite = place.getWebsiteUri();
        Log.i(TAG, "Uri " + place.getWebsiteUri());
        // Use findPhotoPlace method to retrieve the photo of the restaurant
        if (place.getPhotoMetadatas() != null) {
            CurrentPlace.getInstance(this).findPhotoPlace(place.getPhotoMetadatas().get(0), this);
        }
    }

    @Override
    public void onPhotoFetch(Bitmap bitmap) {
        // Get the photo metadata and fetch it in the imageView
        restaurantImage.setImageBitmap(bitmap);
    }

    //----------------------------------------------------------------------------------

    // Verify if the restaurant chosen is the same that the details restaurant
    private void verifyRestaurantChoice() {
        if (place.getName() != null && place.getName().equals(workmate.getSelectedPlace())) {
            fabIsChecked = true;
        }
    }

    private void chooseRestaurant() {
        if (firebaseUtils.getCurrentUser() != null) {
            WorkmateHelper.updateRestaurant(firebaseUtils.getCurrentUser().getUid(), place.getName());
        }
    }

    private void clearRestaurantChoice() {
        if (firebaseUtils.getCurrentUser() != null) {
            WorkmateHelper.updateRestaurant(firebaseUtils.getCurrentUser().getUid(), null);
        }
    }

}
