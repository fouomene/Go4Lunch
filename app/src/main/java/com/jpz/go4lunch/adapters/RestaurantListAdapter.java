package com.jpz.go4lunch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.views.RestaurantViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RestaurantListAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    // Declaring callback
    private final Listener callback;

    // For data
    private List<Place> placeList = new ArrayList<>();
    private LatLng latLng;

    public RestaurantListAdapter(LatLng latLng, Listener callback) {
        this.latLng = latLng;
        this.callback = callback;
    }

    // Create interface for callback
    public interface Listener {
        void onClickItem(int position);
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder and inflating its xml layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.restaurant_item, parent, false);

        return new RestaurantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.updateViewHolder(this.placeList.get(position), this.latLng, this.callback);
    }

    @Override
    public int getItemCount() {
        return this.placeList.size();
    }

    @Override
    public void onViewRecycled(@NonNull RestaurantViewHolder holder) {
        super.onViewRecycled(holder);
        holder.removeFirestoreListener();
    }

    public void setPlaces(List<Place> places) {
        this.placeList = places;
        notifyDataSetChanged();
    }

    // Return the position of an item in the list
    public Place getPosition(int position) {
        return this.placeList.get(position);
    }
}