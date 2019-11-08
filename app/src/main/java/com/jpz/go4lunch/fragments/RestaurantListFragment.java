package com.jpz.go4lunch.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.RestaurantListAdapter;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.MyUtilsNavigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.jpz.go4lunch.activities.MainActivity.LAT_LNG_BUNDLE_KEY;
import static com.jpz.go4lunch.activities.MainActivity.PLACES_ID_BUNDLE_KEY;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantListFragment extends Fragment
        implements RestaurantListAdapter.Listener, RestaurantListAdapter.DataToSort,
        CurrentPlace.CurrentPlacesListener, CurrentPlace.PlaceDetailsListener {

    // Declare View, Adapter & a LatLng
    private RecyclerView recyclerView;
    private RestaurantListAdapter restaurantListAdapter;
    private LatLng deviceLatLng;

    // Places SDK
    private List<Place> placeList = new ArrayList<>();

    // List of placesId from the autocomplete request
    private ArrayList<String> placesId;

    // List of data to sort
    private ArrayList<RestaurantData> placesToSort = new ArrayList<>();

    // Utils
    private MyUtilsNavigation utilsNavigation = new MyUtilsNavigation();

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

        setHasOptionsMenu(true);

        // Add the PlaceDetailsListener in the list of listeners from CurrentPlace Singleton...
        CurrentPlace.getInstance(getActivity()).addDetailsListener(this);

        // Get deviceLatLng value from the map and placesId from autocomplete
        if (getArguments() != null) {
            deviceLatLng = getArguments().getParcelable(LAT_LNG_BUNDLE_KEY);
            // List of placesId from the autocomplete query
            placesId = getArguments().getStringArrayList(PLACES_ID_BUNDLE_KEY);
            Log.i(TAG, "placesId = " + placesId);
        }

        // If there is a request from autocomplete, fetch a placeDetails
        if (placesId != null) {
            // Clear the placeList before use it in the onPlaceDetailsFetch
            placeList.clear();
            if (placesId.isEmpty()) {
                Toast.makeText(getActivity(), getString(R.string.no_result), Toast.LENGTH_SHORT).show();
                // Remove the key from the bundle
                getArguments().remove(PLACES_ID_BUNDLE_KEY);
            } else {
                // For each placeId from autocomplete
                for (String placeId : placesId) {
                    // request a detailsPlace
                    CurrentPlace.getInstance(getActivity()).findDetailsPlaces(placeId);
                }
            }
            // Else fetch findCurrentPlace then findDetailsPlace
        } else {
            // Add the CurrentPlacesListener in the list of listeners from CurrentPlace Singleton...
            CurrentPlace.getInstance(getActivity()).addListener(this);
            // ...to allow fetching places in the method below :
            CurrentPlace.getInstance(getActivity()).findCurrentPlace();
        }

        configureRecyclerView();

        return view;
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_restaurant_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //this.item = item;
        // Handle action on menu items
        if (item.getItemId() == R.id.menu_toolbar_test) {
            //Toast.makeText(getActivity(), "TEST", Toast.LENGTH_SHORT).show();
            List<Place> listSorted = new ArrayList<>();

            if (!placesToSort.isEmpty()) {
                //restaurantListAdapter.notifyItemRangeRemoved(0, placesToSort.size());
                Log.w(TAG, "placesToSort size = " + placesToSort.size());
                sortByProximity(placesToSort);

                // Update UI with the list to sort
                for (RestaurantData restaurantData : placesToSort) {
                    listSorted.add(restaurantData.getPlace());
                }
                updateUI(listSorted);
                // Clear the placesToSort in case of reuse it
                placesToSort.clear();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //----------------------------------------------------------------------------------
    // Configure RecyclerViews, Adapters, LayoutManager & glue it together

    private void configureRecyclerView() {
        // Create the adapter
        this.restaurantListAdapter = new RestaurantListAdapter(deviceLatLng, this, this);
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
        Log.i(TAG, "place.name = " + place.getName());
        utilsNavigation.startDetailsRestaurantActivity(getActivity(), place.getId());
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onPlacesFetch(List<Place> places) {
        // For each place from CurrentPlacesListener
        for (Place place : places) {
            // request a detailsPlace
            CurrentPlace.getInstance(getActivity()).findDetailsPlaces(place.getId());
        }
        // Clear the placeList before use it in the onPlaceDetailsFetch
        placeList.clear();
    }

    // Use the Interface PlaceDetailsListener to attach the place
    @Override
    public void onPlaceDetailsFetch(Place place) {
        // Update UI with the list of restaurant from the request
        placeList.add(place);
        updateUI(placeList);
        // Remove the key from the bundle
        if (getArguments() != null) {
            getArguments().remove(PLACES_ID_BUNDLE_KEY);
        }
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        CurrentPlace.getInstance(getActivity()).removeListener(this);
        super.onDestroy();
    }

    //----------------------------------------------------------------------------------


    @Override
    public void onSortItem(Place place, Integer proximity, Integer rating, Integer numberWorkmates) {

        Log.w(TAG, "placeName = " + place.getName());

        //RestaurantData restaurantData = new RestaurantData(place, proximity, rating, numberWorkmates);
        RestaurantData restaurantData = new RestaurantData();
        restaurantData.setPlace(place);
        restaurantData.setProximity(proximity);
        restaurantData.setRating(rating);
        restaurantData.setNumberWorkmates(numberWorkmates);

        boolean isTheSame = false;

        if (!placesToSort.isEmpty()) {
            for (RestaurantData data : placesToSort) {
                Log.w(TAG, "In LOOP placeName = " + data.getPlace().getName());
                if (data.getPlace().getId() != null && restaurantData.getPlace().getId() != null) {
                    if (data.getPlace().getId().equals(restaurantData.getPlace().getId())) {
                        isTheSame = true;
                    }
                }
            }
        }
        if (!isTheSame) {
            placesToSort.add(restaurantData);
        }
    }

    private void sortByProximity(List<RestaurantData> restaurantDataList) {

        Collections.sort(restaurantDataList, (o1, o2) -> {
            if (o1.getProximity().equals(o2.getProximity())) {
                return 0;
            }
            return o1.getProximity().compareTo(o2.getProximity());
        });
    }

    private class RestaurantData {
        // Data to sort the list of restaurants

        private Place place;
        private Integer proximity;
        private Integer rating;
        private Integer numberWorkmates;

        /*
        public RestaurantData(Place place, int proximity, double rating, int numberWorkmates) {
            this.place = place;
            this.proximity = proximity;
            this.rating = rating;
            this.numberWorkmates = numberWorkmates;
        }
         */


        private Place getPlace() {
            return place;
        }

        private Integer getProximity() {
            return proximity;
        }

        private Integer getRating() {
            return rating;
        }

        private Integer getNumberWorkmates() {
            return numberWorkmates;
        }

        private void setPlace(Place place) {
            this.place = place;
        }

        private void setProximity(int proximity) {
            this.proximity = proximity;
        }

        private void setRating(int rating) {
            this.rating = rating;
        }

        private void setNumberWorkmates(int numberWorkmates) {
            this.numberWorkmates = numberWorkmates;
        }

    }

}