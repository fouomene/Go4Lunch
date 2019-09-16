package com.jpz.go4lunch.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.utils.ConvertMethods;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;


public class ViewHolderRestaurant extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Represent an item (line) in the RecyclerView

    private static final String TAG = ViewHolderRestaurant.class.getSimpleName();

    // Utils
    private ConvertMethods convertMethods = new ConvertMethods();

    private TextView name, distance, type, address, workmates, hours, opinions;
    private ImageView restaurantImage;

    private String placeDetail;

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

        //placesClient = Places.createClient(itemView.getContext());

        //context = itemView.getContext();
    }

    public void updateViewHolder(Place place, RequestManager glide, AdapterListRestaurant.Listener callback, Context context){

        Log.i(TAG, "place  = " + place);

        // Update widgets
        name.setText(fetchPlaceDetails(place, context));
        distance.setText("distance");
        type.setText("type");
        address.setText(convertMethods.getAddress(place));
        workmates.setText("wormates");
        hours.setText("hours");
        opinions.setText("opinions");

        if (place.getPhotoMetadatas() != null)
            glide.load(place.getPhotoMetadatas().get(0)).into(restaurantImage);

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


    private String fetchPlaceDetails(Place place, Context context) {

        if (place.getId() != null) {

            // Specify the fields to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS_COMPONENTS, Place.Field.PHOTO_METADATAS);

            // Construct a request object, passing the place ID and fields array.
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(place.getId(), placeFields);

            // Create a new Places client instance
            PlacesClient placesClient = Places.createClient(context);

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                placeDetail = response.getPlace().getName();
                Log.i(TAG, "Place detail found: " + response.getPlace().getName());

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place detail not found: " + statusCode + exception.getMessage());
                }
            });

        }

        Log.i(TAG, "placeDetail = " + placeDetail);

        return placeDetail;

    }



}
