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

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.RestaurantListAdapter;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.MyUtilsNavigation;

import java.util.List;

import static com.jpz.go4lunch.activities.MainActivity.LAT_LNG_BUNDLE_KEY;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantListFragment extends Fragment implements RestaurantListAdapter.Listener,
        CurrentPlace.PlacesDetailsListener {

    // Declare View, Adapter & a LatLng
    private RecyclerView recyclerView;
    private RestaurantListAdapter restaurantListAdapter;
    private LatLng deviceLatLng;

    // Utils
    private MyUtilsNavigation utilsNavigation = new MyUtilsNavigation();

    public RestaurantListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get layout of this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant_list, container, false);

        recyclerView = view.findViewById(R.id.restaurant_list_recycler_view);

        // Get deviceLatLng value from the map
        if (getArguments() != null) deviceLatLng = getArguments().getParcelable(LAT_LNG_BUNDLE_KEY);

        configureRecyclerView();

        // Add the currentDetailsListener in the list of listeners from CurrentPlace Singleton...
        CurrentPlace.getInstance(getActivity()).addDetailsListener(this);
        // ...to allow fetching places in the method below :
        CurrentPlace.getInstance(getActivity()).findDetailsPlaces(null);

        return view;
    }

    //----------------------------------------------------------------------------------
    // Configure RecyclerViews, Adapters, LayoutManager & glue it together

    private void configureRecyclerView(){
        // Create the adapter
        this.restaurantListAdapter = new RestaurantListAdapter(deviceLatLng, this);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(restaurantListAdapter);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void updateUI(List<Place> places) {
        // Add the list from the request and notify the adapter
        restaurantListAdapter.setPlaces(places);
    }

    //----------------------------------------------------------------------------------

    // Start DetailsRestaurantActivity when click the user click on a restaurant
    @Override
    public void onClickItem(int position) {
        Place place = restaurantListAdapter.getPosition(position);
        Log.i("ListFragment", "place.name = " + place.getName());
        utilsNavigation.startDetailsRestaurantActivity(getActivity(), place,  null);
    }

    //----------------------------------------------------------------------------------

    // Use the Interface CurrentPlace to attach the list of places
    @Override
    public void onPlacesDetailsFetch(List<Place> places) {
        // Update UI with the list of restaurant from the request
        updateUI(places);
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        CurrentPlace.getInstance(getActivity()).removeListener(this);
        super.onDestroy();
    }
}