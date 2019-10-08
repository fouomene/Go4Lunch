package com.jpz.go4lunch.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.ConvertData;

import java.lang.ref.WeakReference;


public class ViewHolderRestaurant extends RecyclerView.ViewHolder implements View.OnClickListener,
        CurrentPlace.PlacePhotoListener {
    // Represent an item (line) in the RecyclerView

    // Utils
    private ConvertData convertData = new ConvertData();

    // Views and Context
    private TextView name, distance, type, address, workmates, hours, opinions;
    private ImageView restaurantImage;
    private Context context;

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
    }

    public void updateViewHolder(Place place, LatLng latLng, AdapterListRestaurant.Listener callback){
        // Update Place widgets
        name.setText(place.getName());

        hours.setText(convertData.openingHours(place, context));
        if (convertData.openingHours(place, context).contains("Clos")) {
            hours.setTextColor(context.getApplicationContext().getResources().getColor(R.color.crimson));
            hours.setTypeface(Typeface.DEFAULT_BOLD);
        }
        if (convertData.openingHours(place, context).contains("Open")) {
            hours.setTypeface(null, Typeface.ITALIC);
        }

        address.setText(convertData.getAddress(place));

        // Use findPhotoPlace method to retrieve the photo of the restaurant
        if (place.getPhotoMetadatas() != null) {
            CurrentPlace.getInstance(context).findPhotoPlace(place.getPhotoMetadatas().get(0), this);
        }

        // Update others widgets
        if (place.getLatLng() != null) {
            distance.setText(context.getString(R.string.distance,  convertData.distanceCalculation
                    (latLng.latitude, latLng.longitude, place.getLatLng().latitude, place.getLatLng().longitude)));
        }

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

    @Override
    public void onPhotoFetch(Bitmap bitmap) {
        // Get the photo metadata and fetch it in the imageView
        restaurantImage.setImageBitmap(bitmap);
    }
}
