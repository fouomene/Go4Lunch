package com.jpz.go4lunch.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.utils.CurrentPlace;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantListFragment extends Fragment implements AdapterListRestaurant.Listener, CurrentPlace.CurrentPlaceListListener {

    // Declare View, Adapter & a list of places
    private RecyclerView recyclerView;
    private AdapterListRestaurant adapterListRestaurant;
    private List<Place> placeList;

    private static final String TAG = RestaurantListFragment.class.getSimpleName();

    public RestaurantListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get layout of this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        recyclerView = view.findViewById(R.id.restaurant_list_recycler_view);

        configureRecyclerView();

        // Initialize currentPlaceListListener
        CurrentPlace.CurrentPlaceListListener currentPlaceListListener = this;

        Log.i(TAG, "currentPlaceListListener = " + currentPlaceListListener);

        // Add the currentPlaceListListener in the list of listeners from CurrentPlace Singleton...
        CurrentPlace.getInstance().addListener(currentPlaceListListener);

        if (getActivity() != null)
            // ...to allow fetching places in the method below :
            CurrentPlace.getInstance().findCurrentPlace(getActivity());

        return view;
    }

    //----------------------------------------------------------------------------------
    // Configure RecyclerViews, Adapters, LayoutManager & glue it together

    private void configureRecyclerView(){
        // Reset list
        this.placeList = new ArrayList<>();
        // Create the adapter by passing the list of restaurants
        this.adapterListRestaurant = new AdapterListRestaurant(placeList, Glide.with(this), this);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(adapterListRestaurant);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void updateUI(List<Place> places) {
        // Add the list from the request and notify the adapter
        placeList.addAll(places);
        adapterListRestaurant.notifyDataSetChanged();
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onClickItem(int position) {

    }

    //----------------------------------------------------------------------------------

    // Use the Interface to attach the list of places
    @Override
    public void onPlacesFetch(List<Place> places) {
        updateUI(places);
    }
}