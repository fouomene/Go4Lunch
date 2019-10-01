package com.jpz.go4lunch.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.views.ViewHolderRestaurant;

import java.util.List;

public class AdapterListRestaurant extends RecyclerView.Adapter<ViewHolderRestaurant> {

    // Declaring callback
    private final Listener callback;

    // For data
    private List<Place> placeList;

    private LatLng latLng;

    private PlacesClient placesClient;
    private FetchPlaceRequest request;

    private ViewHolderRestaurant viewHolder;


    public AdapterListRestaurant(List<Place> placeList, LatLng latLng, Listener callback) {
        this.placeList = placeList;
        this.latLng = latLng;
        this.callback = callback;
    }

    // Create interface for callback
    public interface Listener {
        void onClickItem(int position);
    }

    @NonNull
    @Override
    public ViewHolderRestaurant onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder and inflating its xml layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_item, parent, false);

        // Create a new Places client instance
        //placesClient = Places.createClient(context);

        return new ViewHolderRestaurant(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRestaurant holder, int position) {
        //viewHolder = holder;
        //this.position = position;

        // Add the placeDetailsListener in the list of listeners from CurrentPlace Singleton...
        //CurrentPlace.getInstance().addDetailsListener(this);
        //Log.w("ADAPTER", "this : " + this);
        // ...to allow fetching details in the method below :
        //CurrentPlace.getInstance().fetchDetailsPlace(this.placeList.get(position), placesClient, request);

        holder.updateViewHolder(this.placeList.get(position), this.latLng, this.callback);
    }

    @Override
    public int getItemCount() {
        return this.placeList.size();
    }

    // Return the position of an item in the list
    public Place getPosition(int position){
        return this.placeList.get(position);
    }

    /*
    @Override
    public void onPlaceDetailsFetch(Place placeDetails) {
        //viewHolder.updateViewHolder(placeDetails, callback);
    }

     */
}