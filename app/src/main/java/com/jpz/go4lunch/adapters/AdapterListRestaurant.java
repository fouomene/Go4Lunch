package com.jpz.go4lunch.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.views.ViewHolderRestaurant;

import java.util.Arrays;
import java.util.List;

public class AdapterListRestaurant extends RecyclerView.Adapter<ViewHolderRestaurant> {

    private static final String TAG = AdapterListRestaurant.class.getSimpleName();

    // Declaring callback
    private final Listener callback;

    // For data
    private List<Place> placeList;

    private Place placeDetails;

    private PlacesClient placesClient;

    //private Context context;

    // Declaring a Glide object
    private RequestManager glide;

    public AdapterListRestaurant(List<Place> placeList, RequestManager glide, Listener callback) {
        //this.places = CurrentPlace.getInstance().getPlaces();
        this.placeList = placeList;
        this.glide = glide;
        this.callback = callback;
    }

    // Create interface for callback
    public interface Listener {
        void onClickItem(int position);
    }

    @NonNull
    @Override
    public ViewHolderRestaurant onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolderNews and inflating its xml layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_item, parent, false);

        // Create a new Places client instance
        placesClient = Places.createClient(context);

        return new ViewHolderRestaurant(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRestaurant holder, int position) {
        //Context context = holder.itemView.getContext();

        //Log.i(TAG, "placeList 1st index  = " + fetchPlaceDetails(placeList.get(position)));

        holder.updateViewHolder(fetchPlaceDetails(placeList.get(position)), this.glide, this.callback);

        //holder.updateViewHolder(this.placeList.get(position), this.glide, this.callback, context);
    }

    @Override
    public int getItemCount() {
        return this.placeList.size();
    }

    // Return the position of an item in the list
    public Place getPosition(int position){
        return this.placeList.get(position);
    }



    private Place fetchPlaceDetails(Place place) {

        // Create a new Places client instance
        //PlacesClient placesClient = Places.createClient(context);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS_COMPONENTS, Place.Field.PHOTO_METADATAS);

        if (place.getId() != null) {

            // Construct a request object, passing the place ID and fields array.
            FetchPlaceRequest request = FetchPlaceRequest.newInstance(place.getId(), placeFields);

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                placeDetails = response.getPlace();
                Log.i(TAG, "Place found: " + placeDetails.getName());
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + statusCode + exception.getMessage());
                }
            });
        }
        return placeDetails;
    }



}
