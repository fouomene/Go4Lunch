package com.jpz.go4lunch.views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.utils.ConvertMethods;

import java.lang.ref.WeakReference;


public class ViewHolderRestaurant extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Represent an item (line) in the RecyclerView

    private static final String TAG = ViewHolderRestaurant.class.getSimpleName();

    // Utils
    private ConvertMethods convertMethods = new ConvertMethods();

    // Views and Context
    private TextView name, distance, type, address, workmates, hours, opinions;
    private ImageView restaurantImage;
    private Context context;

    // Places
    private PlacesClient placesClient;
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

        // Create a new Places client instance the photoRequest
        placesClient = Places.createClient(context);
    }

    public void updateViewHolder(Place place, LatLng latLng, AdapterListRestaurant.Listener callback){
        // Update Place widgets
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

        // Update others widgets
        if (place.getLatLng() != null)
            distance.setText(context.getString(R.string.distance,  convertMethods.distanceCalculation
                    (latLng.latitude, latLng.longitude, place.getLatLng().latitude, place.getLatLng().longitude)));

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

}
