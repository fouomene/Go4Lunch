package com.jpz.go4lunch.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.utils.ConvertMethods;
import com.jpz.go4lunch.utils.CurrentPlace;

import java.lang.ref.WeakReference;


public class ViewHolderRestaurant extends RecyclerView.ViewHolder
        implements View.OnClickListener, CurrentPlace.PlaceDetailsListener {
    // Represent an item (line) in the RecyclerView

    private static final String TAG = ViewHolderRestaurant.class.getSimpleName();

    // Utils
    private ConvertMethods convertMethods = new ConvertMethods();

    private TextView name, distance, type, address, workmates, hours, opinions;
    private ImageView restaurantImage;
    private Context context;

    // Places
    private PlacesClient placesClient;
    private FetchPlaceRequest request;
    private PhotoMetadata photoMetadata;

    // Declare a Weak Reference to our Callback
    private WeakReference<AdapterListRestaurant.Listener> callbackWeakRef;

    public ViewHolderRestaurant(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.item_name);
        distance = itemView.findViewById(R.id.item_distance);
        type = itemView.findViewById(R.id.item_type);
        address = itemView.findViewById(R.id.item_address);
        workmates = itemView.findViewById(R.id.item_workmates);
        hours = itemView.findViewById(R.id.item_hours);
        opinions = itemView.findViewById(R.id.item_opinions);
        restaurantImage = itemView.findViewById(R.id.item_image_restaurant);

        context = itemView.getContext();

        // Create a new Places client instance
        placesClient = Places.createClient(context);
    }

    public void updateViewHolder(Place place, AdapterListRestaurant.Listener callback){

        // Add the placeDetailsListener in the list of listeners from CurrentPlace Singleton...
        CurrentPlace.getInstance().addDetailsListener(this);
        Log.w(TAG, "this : " + this);

        // ...to allow fetching details in the method below :
        CurrentPlace.getInstance().fetchDetailsPlace(place, placesClient, request);

        /*
        name.setText(place.getName());

        hours.setText(convertMethods.openingHours(place, context));
        if (convertMethods.openingHours(place, context).contains("Clos")) {
            hours.setTextColor(context.getApplicationContext().getResources().getColor(R.color.crimson));
            hours.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (convertMethods.openingHours(place, context).contains("Open"))
            hours.setTypeface(null, Typeface.ITALIC);

        address.setText(convertMethods.getAddress(place));

        // Get the photo metadata
        if (place.getPhotoMetadatas() != null)
            photoMetadata = place.getPhotoMetadatas().get(0);
        convertMethods.fetchPhoto(placesClient, photoMetadata, restaurantImage);

         */


        // Update others widgets
        distance.setText("distance");
        type.setText("type");
        workmates.setText("wormates");
        opinions.setText("opinions");

        // Create a new weak Reference to our Listener
        this.callbackWeakRef = new WeakReference<>(callback);
        // Implement Listener
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // When a click happens, we fire our listener to get the item position in the list
        AdapterListRestaurant.Listener callback = callbackWeakRef.get();
        if (callback != null) callback.onClickItem(getAdapterPosition());
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onPlaceDetailsFetch(Place placeDetails) {
        updatePlaceUI(placeDetails);
    }

    //----------------------------------------------------------------------------------

    private void updatePlaceUI(Place placeDetails) {
        name.setText(placeDetails.getName());

        hours.setText(convertMethods.openingHours(placeDetails, context));
        if (convertMethods.openingHours(placeDetails, context).contains("Clos")) {
            hours.setTextColor(context.getApplicationContext().getResources().getColor(R.color.crimson));
            hours.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (convertMethods.openingHours(placeDetails, context).contains("Open"))
            hours.setTypeface(null, Typeface.ITALIC);

        address.setText(convertMethods.getAddress(placeDetails));

        // Get the photo metadata
        if (placeDetails.getPhotoMetadatas() != null)
            photoMetadata = placeDetails.getPhotoMetadatas().get(0);
        convertMethods.fetchPhoto(placesClient, photoMetadata, restaurantImage);
    }

}
