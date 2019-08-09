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
import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.models.FieldRestaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantListFragment extends Fragment implements AdapterListRestaurant.Listener {

    // Declare View, Adapter & a list of fields
    private RecyclerView recyclerView;
    private AdapterListRestaurant adapterListRestaurant;
    private List<FieldRestaurant> fieldRestaurantList;

    // Places
    private PlacesClient placesClient;
    private FetchPlaceRequest request;

    private FieldRestaurant fieldRestaurant = new FieldRestaurant();

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


        // Define a Place ID.
        //String placeId = fieldRestaurant.id;

        // Specify the fields to return.
        //List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        // Construct a request object, passing the place ID and fields array.
        //request = FetchPlaceRequest.newInstance(placeId, placeFields);

        if (getActivity() != null)
            // Create a new Places client instance
            placesClient = Places.createClient(getActivity());

        configureRecyclerView();

        fetchPlace();

        //getRestaurantsId(fieldRestaurant.idList);

        return view;
    }

    // ----------------------------------------------------------------------------
    // Configure RecyclerViews, Adapters, LayoutManager & glue it together

    private void configureRecyclerView(){
        // Reset list
        this.fieldRestaurantList = new ArrayList<>();
        // Create the adapter by passing the list of restaurants
        this.adapterListRestaurant = new AdapterListRestaurant(fieldRestaurantList,
                Glide.with(this), this);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(adapterListRestaurant);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void getRestaurantsId(List<FieldRestaurant> restaurantList) {
        // Add the list from the request and notify the adapter
        fieldRestaurantList.addAll(restaurantList);
        adapterListRestaurant.notifyDataSetChanged();
    }

    // ----------------------------------------------------------------------------

    private void fetchPlace() {

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        //for (String id : fieldRestaurant.getIdList()) {

            //fieldRestaurant.id = id;

            request = FetchPlaceRequest.newInstance(fieldRestaurant.id, placeFields);

            placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                Place place = response.getPlace();
                Log.i(TAG, "Place found: " + place.getName());

                //getRestaurantsId(fieldRestaurantList);

            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    int statusCode = apiException.getStatusCode();
                    // Handle error with given status code.
                    Log.e(TAG, "Place not found: " + statusCode + exception.getMessage());
                }
            });
        //}
    }


    @Override
    public void onClickItem(int position) {

    }
}