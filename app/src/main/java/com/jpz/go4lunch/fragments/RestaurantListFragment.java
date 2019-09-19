package com.jpz.go4lunch.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
public class RestaurantListFragment extends Fragment implements AdapterListRestaurant.Listener {

    // Declare View, Adapter & a list of fields
    private RecyclerView recyclerView;
    private AdapterListRestaurant adapterListRestaurant;

    private List<Place> placeList;


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
        //updateUI(CurrentPlace.getInstance().getPlaces());

        return view;
    }

    // ----------------------------------------------------------------------------
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
        placeList.clear();
        placeList.addAll(places);
        adapterListRestaurant.notifyDataSetChanged();
    }

    // ----------------------------------------------------------------------------

    @Override
    public void onClickItem(int position) {

    }
}