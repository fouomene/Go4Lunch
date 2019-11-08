package com.jpz.go4lunch.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.RestaurantListAdapter;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.ConvertData;

import java.lang.ref.WeakReference;

import static com.jpz.go4lunch.api.WorkmateHelper.FIELD_RESTAURANT_DATE;
import static com.jpz.go4lunch.api.WorkmateHelper.FIELD_RESTAURANT_ID;
import static com.jpz.go4lunch.api.WorkmateHelper.getWorkmatesCollection;


public class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        CurrentPlace.PlacePhotoListener {
    // Represent an item (line) of a restaurant in the RecyclerView

    // Utils
    private ConvertData convertData = new ConvertData();

    // Views and Context
    private TextView name, distance, address, workmates, hours;
    private ImageView restaurantImage, workmate_ic, firstStar, secondStar, thirdStar;
    private Context context;

    // To sort data
    private int proximity;
    private int rating;
    private int numberWorkmates;

    // Firestore
    private ListenerRegistration registration;

    // Declare a Weak Reference to our Callback
    private WeakReference<RestaurantListAdapter.Listener> callbackWeakRef;

    private static final String TAG = RestaurantViewHolder.class.getSimpleName();

    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.item_name);
        distance = itemView.findViewById(R.id.item_distance);
        address = itemView.findViewById(R.id.item_address);
        workmates = itemView.findViewById(R.id.item_workmates);
        hours = itemView.findViewById(R.id.item_hours);
        firstStar = itemView.findViewById(R.id.item_first_star);
        secondStar = itemView.findViewById(R.id.item_second_star);
        thirdStar = itemView.findViewById(R.id.item_third_star);
        restaurantImage = itemView.findViewById(R.id.item_image_restaurant);
        workmate_ic = itemView.findViewById(R.id.item_ic_workmate);

        context = itemView.getContext();
    }

    public void updateViewHolder(Place place, LatLng latLng, RestaurantListAdapter.Listener callback,
                                 RestaurantListAdapter.DataToSort dataToSort) {
        // Update Place widgets
        name.setText(place.getName());

        hours.setText(convertData.openingHours(place, context));
        // Set style for english or french data
        if (convertData.openingHours(place, context).contains("Clos")
                || convertData.openingHours(place, context).contains("Ferm")) {
            hours.setTextColor(context.getResources().getColor(R.color.crimson));
            hours.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (convertData.openingHours(place, context).contains("Open")
                || convertData.openingHours(place, context).contains("Ouv")) {
            hours.setTypeface(null, Typeface.ITALIC);
        }

        address.setText(convertData.getAddress(place));

        // Use findPhotoPlace method to retrieve the photo of the restaurant
        if (place.getPhotoMetadatas() != null) {
            CurrentPlace.getInstance(context).findPhotoPlace(place.getPhotoMetadatas().get(0), this);
        }

        // Update others widgets
        if (place.getLatLng() != null && latLng != null) {
            proximity = convertData.distanceCalculation
                    (latLng.latitude, latLng.longitude, place.getLatLng().latitude, place.getLatLng().longitude);
            distance.setText(context.getString(R.string.distance, proximity));
        }

        // By default, number of workmates is empty
        workmates.setText("");

        // Check if workmates join this restaurant in Firestore (real-time) :
        Query query = getWorkmatesCollection()
                .whereEqualTo(FIELD_RESTAURANT_ID, place.getId())
                .whereEqualTo(FIELD_RESTAURANT_DATE, convertData.getTodayDate());

        registration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "listen:error", e);
                return;
            }
            // Display the icon with the number of workmates
            if (snapshots != null && !snapshots.isEmpty()) {
                numberWorkmates = snapshots.size();
                workmate_ic.setVisibility(View.VISIBLE);
                workmates.setText(context.getString(R.string.number_of_workmates, numberWorkmates));
            }
            // Or display anything if the list is empty
            if (snapshots != null && snapshots.isEmpty()) {
                workmate_ic.setVisibility(View.INVISIBLE);
                workmates.setText("");
            }
        });

        // Update rating and display stars
        convertData.updateRating(place, firstStar, secondStar, thirdStar);

        // Create a new weak Reference to our Listener
        this.callbackWeakRef = new WeakReference<>(callback);
        // Implement Listener
        itemView.setOnClickListener(this);

        // Get rating
        if (place.getRating() != null) {
            double d = place.getRating();
            rating = (int) d;
        }

        dataToSort.onSortItem(place, proximity, rating, numberWorkmates);
    }

    @Override
    public void onClick(View v) {
        // When a click happens, we fire our listener to get the item position in the list
        RestaurantListAdapter.Listener callback = callbackWeakRef.get();
        if (callback != null) callback.onClickItem(getAdapterPosition());
    }

    @Override
    public void onPhotoFetch(Bitmap bitmap) {
        // Get the photo metadata and fetch it in the imageView
        restaurantImage.setImageBitmap(bitmap);
    }

    public void removeFirestoreListener() {
        registration.remove();
    }
}
