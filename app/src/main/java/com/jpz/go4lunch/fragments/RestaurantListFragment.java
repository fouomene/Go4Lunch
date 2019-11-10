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
import com.jpz.go4lunch.models.RestaurantDataToSort;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.MyUtilsNavigation;

import java.util.ArrayList;
import java.util.List;

import static com.jpz.go4lunch.activities.MainActivity.LAT_LNG_BUNDLE_KEY;
import static com.jpz.go4lunch.activities.MainActivity.PLACES_ID_BUNDLE_KEY;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantListFragment extends Fragment
        implements RestaurantListAdapter.ClickListener, RestaurantListAdapter.DataToSort,
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
    private ArrayList<RestaurantDataToSort> placesToSort = new ArrayList<>();

    // Utils
    private MyUtilsNavigation utilsNavigation = new MyUtilsNavigation();
    private ConvertData convertData = new ConvertData();

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

    // Specific menu in toolbar for this fragment, used to sort restaurants.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Create a sorted list for output
        List<Place> sortedList;
        // Sort the list
        switch (item.getItemId()) {
            case R.id.sub_item_proximity:
                sortedList = new ArrayList<>();
                sortRestaurantsAndUpdateUI(placesToSort, sortedList, R.id.sub_item_proximity);
                break;
            case R.id.sub_item_rating:
                sortedList = new ArrayList<>();
                sortRestaurantsAndUpdateUI(placesToSort, sortedList, R.id.sub_item_rating);
                break;
            case R.id.sub_item_number_workmates:
                sortedList = new ArrayList<>();
                sortRestaurantsAndUpdateUI(placesToSort, sortedList, R.id.sub_item_number_workmates);
                break;
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

    // Use the Interface DataToSort to retrieve data to sort
    @Override
    public void onSortItem(Place place, int proximity, double rating, int numberWorkmates) {
        Log.i(TAG, "placeName = " + place.getName());

        RestaurantDataToSort restaurantData =
                new RestaurantDataToSort(place, proximity, rating, numberWorkmates);
        boolean isEqual = false;

        // Check if there is already the same restaurantDataToSort in the list to sort
        if (!placesToSort.isEmpty()) {
            for (RestaurantDataToSort dataToSort : placesToSort) {
                Log.i(TAG, "In LOOP placeName = " + dataToSort.getPlace().getName());
                if (dataToSort.getPlace().getId() != null && restaurantData.getPlace().getId() != null) {
                    if (dataToSort.getPlace().getId().equals(restaurantData.getPlace().getId())) {
                        isEqual = true;
                    }
                }
            }
        }
        if (!isEqual) {
            placesToSort.add(restaurantData);
        }
    }

    //----------------------------------------------------------------------------------

    // Final method to sort restaurants and update UI
    private void sortRestaurantsAndUpdateUI(ArrayList<RestaurantDataToSort> placesToSort,
                                            List<Place> listSorted, int itemId) {
        // Assign a sorting method based on the user's choice
        if (!placesToSort.isEmpty()) {
            Log.i(TAG, "placesToSort size = " + placesToSort.size());
            switch (itemId) {
                case R.id.sub_item_proximity:
                    convertData.sortByProximity(placesToSort);
                    break;
                case R.id.sub_item_rating:
                    convertData.sortByRating(placesToSort);
                    break;
                case R.id.sub_item_number_workmates:
                    convertData.sortByNumberWorkmates(placesToSort);
                    break;
            }
            // Update UI with the list to sort
            for (RestaurantDataToSort restaurantData : placesToSort) {
                listSorted.add(restaurantData.getPlace());
            }
            updateUI(listSorted);
            // Clear the placesToSort in case of reuse it
            placesToSort.clear();
        }
    }

}