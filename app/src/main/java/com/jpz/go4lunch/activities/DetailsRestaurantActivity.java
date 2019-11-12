package com.jpz.go4lunch.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

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

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.WorkmatesAtRestaurantAdapter;
import com.jpz.go4lunch.api.WorkmateHelper;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.FirebaseUtils;

import static com.jpz.go4lunch.api.WorkmateHelper.getCurrentWorkmate;
import static com.jpz.go4lunch.api.WorkmateHelper.getWorkmatesAtRestaurant;
import static com.jpz.go4lunch.api.WorkmateHelper.updateRestaurant;
import static com.jpz.go4lunch.utils.MyUtilsNavigation.KEY_ID;

public class DetailsRestaurantActivity extends AppCompatActivity
        implements CurrentPlace.PlaceDetailsListener, CurrentPlace.PlacePhotoListener {

    // Declare Layout and View
    private ConstraintLayout constraintLayout;
    private RecyclerView recyclerView;

    // Widgets
    private TextView name, address;
    private ImageView restaurantImage, firstStar, secondStar, thirdStar;
    private Button call, like, website;
    private FloatingActionButton floatingActionButton;

    // Private Data
    private String restaurantId;
    private String phoneNumber;
    private Uri uriWebsite;
    private boolean fabIsChecked;
    private boolean likeIsChecked;

    // Models and API
    private Workmate currentWorkmate = new Workmate();
    private WorkmateHelper workmateHelper = new WorkmateHelper();

    // Utils
    private ConvertData convertData = new ConvertData();
    private FirebaseUtils firebaseUtils = new FirebaseUtils();
    private FirebaseUser currentUser;

    private static final String TAG = DetailsRestaurantActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_restaurant);

        constraintLayout = findViewById(R.id.details_constraint_layout);
        name = findViewById(R.id.details_name);
        address = findViewById(R.id.details_address);
        firstStar = findViewById(R.id.details_first_star);
        secondStar = findViewById(R.id.details_second_star);
        thirdStar = findViewById(R.id.details_third_star);
        restaurantImage = findViewById(R.id.details_image_restaurant);
        call = findViewById(R.id.details_button_call);
        like = findViewById(R.id.details_button_like);
        website = findViewById(R.id.details_button_website);
        floatingActionButton = findViewById(R.id.details_fab);
        recyclerView = findViewById(R.id.details_recycler_view);

        // Initialize FireBase User
        currentUser = firebaseUtils.getCurrentUser();

        // Get the transferred Place data from the source activity
        Intent intent = getIntent();
        restaurantId = intent.getStringExtra(KEY_ID);

        // Add the currentDetailsListener in the list of listeners from CurrentPlace Singleton...
        CurrentPlace.getInstance(this).addDetailsListener(this);
        // ...to allow fetching places in the method below :
        CurrentPlace.getInstance(this).findDetailsPlaces(restaurantId);

        // Buttons comportment
        callRestaurant();
        getRestaurantLiked();
        likeRestaurant();
        visitWebsiteRestaurant();
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onPlaceDetailsFetch(Place place) {
        // Update data for the restaurant
        updateDetailsRestaurantData(place);
        // Update fab UI according to the restaurant choice from Firestore
        getFirestoreRestaurantChoice(place);
        // Listen the restaurant choice from the current workmate and update Firestore
        listenRestaurantChoice(place);
        // Display the workmates joining to the restaurant in a RecyclerView
        configureRecyclerView(place);
    }


    //----------------------------------------------------------------------------------
    // For the restaurant data

    private void updateDetailsRestaurantData(Place place) {
        // Get Place data
        name.setText(place.getName());
        address.setText(convertData.getAddress(place));
        phoneNumber = place.getPhoneNumber();
        uriWebsite = place.getWebsiteUri();
        // Use findPhotoPlace method to retrieve the photo of the restaurant
        if (place.getPhotoMetadatas() != null) {
            CurrentPlace.getInstance(this).findPhotoPlace(place.getPhotoMetadatas().get(0), this);
        }
        // Update rating and display stars
        convertData.updateRating(place, firstStar, secondStar, thirdStar);
    }

    @Override
    public void onPhotoFetch(Bitmap bitmap) {
        // Get the photo metadata and fetch it in the imageView
        restaurantImage.setImageBitmap(bitmap);
    }

    //----------------------------------------------------------------------------------
    // For the restaurant choice

    // Method to retrieve the current workmate with Firestore data and update UI
    private void getFirestoreRestaurantChoice(Place place) {
        getCurrentWorkmate(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    currentWorkmate = documentSnapshot.toObject(Workmate.class);
                    // Check the restaurant choice
                    compareRestaurants(place);
                    Log.i(TAG, "restaurant choice = " +
                            currentWorkmate.getRestaurantId() + currentWorkmate.getRestaurantName()
                    + currentWorkmate.getRestaurantDate());
                });
    }

    private void compareRestaurants(Place place) {
        // Verify if the restaurant chosen is the same that the details restaurant displayed
        if (place.getId() != null && place.getId().equals(currentWorkmate.getRestaurantId())
        && convertData.getTodayDate().equals(currentWorkmate.getRestaurantDate())) {
            fabIsChecked = true;
        }
        // Update fab UI according to this choice
        if (fabIsChecked) {
            floatingActionButton.setImageDrawable(VectorDrawableCompat.create(getResources(),
                    R.drawable.ic_check_circle, null));
        } else {
            floatingActionButton.setImageDrawable(VectorDrawableCompat.create(getResources(),
                    R.drawable.ic_highlight_off, null));
        }
    }

    private void listenRestaurantChoice(Place place) {
        // Listen to the user choice when click on the floatingActionButton
        floatingActionButton.setOnClickListener((View v) -> {
            if (!fabIsChecked) {
                floatingActionButton.setImageDrawable(VectorDrawableCompat.create(getResources(),
                        R.drawable.ic_check_circle, null));
                chooseRestaurant(place);
                fabIsChecked = true;
            } else {
                floatingActionButton.setImageDrawable(VectorDrawableCompat.create(getResources(),
                        R.drawable.ic_highlight_off, null));
                deleteRestaurantChoice();
                fabIsChecked = false;
            }
        });
    }

    // Current workmate is choosing a restaurant, update Firestore
    private void chooseRestaurant(Place place) {
        if (currentUser != null) {
            // Update the workmates collection
            updateRestaurant(currentUser.getUid(), place.getId(), place.getName(),
                    convertData.getAddress(place), convertData.getTodayDate());
        }
    }

    // Current workmate is deleting a restaurant, update Firestore
    private void deleteRestaurantChoice() {
        if (currentUser != null) {
            // Update the workmates collection
            updateRestaurant(currentUser.getUid(), null, null, null, null);
        }
    }

    //----------------------------------------------------------------------------------
    // Comportment of the buttons

    // Display the phone number
    private void callRestaurant() {
        call.setOnClickListener((View v) -> {
            String dial = "tel:" + phoneNumber;
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(dial)));
        });
    }

    // Display the website
    private void visitWebsiteRestaurant() {
        website.setOnClickListener((View v) -> {
            if (uriWebsite == null) {
                Toast.makeText(this, getString(R.string.no_website), Toast.LENGTH_SHORT).show();
            } else {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriWebsite.toString()));
                startActivity(webIntent);
            }
        });
    }

    // Retrieve like data from Firestore
    private void getRestaurantLiked() {
        // Check if the user already like this restaurant and update UI
        getCurrentWorkmate(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    Workmate workmate = documentSnapshot.toObject(Workmate.class);
                    if (workmate != null && workmate.getRestaurantsLikedId() != null) {
                        if (workmate.getRestaurantsLikedId().contains(restaurantId)) {
                            likeIsChecked = true;
                            like.setText(getString(R.string.unlike));
                        } else {
                            likeIsChecked = false;
                            like.setText(getString(R.string.like));
                        }
                    }
                });
    }

    // Like or unlike the restaurant and update Firestore when the button nis clicked
    private void likeRestaurant() {
        like.setOnClickListener((View v) -> {
            // Check if the user doesn't already like this restaurant, so add his like
            if (!likeIsChecked) {
                like.setText(getString(R.string.unlike));
                workmateHelper.addLike(currentUser.getUid(), restaurantId);
                convertData.showSnackbar(constraintLayout, getString(R.string.add_like));
                likeIsChecked = true;
            // Else the user already like this restaurant, so remove his like
            } else {
                like.setText(getString(R.string.like));
                workmateHelper.removeLike(currentUser.getUid(), restaurantId);
                convertData.showSnackbar(constraintLayout, getString(R.string.remove_like));
                likeIsChecked = false;
            }
        });
    }

    //----------------------------------------------------------------------------------
    // For the RecyclerView

    // Configure RecyclerView with a Query
    private void configureRecyclerView(Place place) {
        //Configure Adapter & RecyclerView
        WorkmatesAtRestaurantAdapter adapter = new WorkmatesAtRestaurantAdapter(generateOptionsForAdapter
                (getWorkmatesAtRestaurant(place.getId(), convertData.getTodayDate())), Glide.with(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    // Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<Workmate> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Workmate>()
                .setQuery(query, Workmate.class)
                .setLifecycleOwner(this)
                .build();
    }

}
