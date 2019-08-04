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
import com.google.android.gms.maps.model.LatLng;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.models.FieldRestaurant;
import com.jpz.go4lunch.utils.APIClient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantListFragment extends Fragment implements AdapterListRestaurant.Listener {

    // Declare View, Disposable, Adapter & a list of fields
    private RecyclerView recyclerView;
    private Disposable disposable;
    private AdapterListRestaurant adapterListRestaurant;
    private List<FieldRestaurant> fieldRestaurantList;

    private LatLng testLatLng = new LatLng(-48.8748, 2.34698);

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

        fetchData();

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

    private void updateUI(List<FieldRestaurant> restaurantList) {
        // Add the list from the request and notify the adapter
        fieldRestaurantList.addAll(restaurantList);
        adapterListRestaurant.notifyDataSetChanged();
    }

    // To dispose subscription
    private void disposeWhenDestroy(){
        if (this.disposable != null && !this.disposable.isDisposed()) this.disposable.dispose();
    }

    // ----------------------------------------------------------------------------

    // HTTP (RxJAVA)
    private void fetchData() {
        // Execute the stream subscribing to Observable defined inside APIClient
        this.disposable = APIClient.getNearbySearchRestaurants(testLatLng)
                .subscribeWith(new DisposableObserver<List<FieldRestaurant>>() {
                    @Override
                    public void onNext(List<FieldRestaurant> fieldRestaurantList) {
                        Log.i(TAG,"On Next NearbySearch");
                        // Update UI with the list of NearbySearch
                        updateUI(fieldRestaurantList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,"On Error NearbySearch" + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onComplete() {
                        Log.i(TAG,"On Complete NearbySearch");
                    }
                });
    }

    // ----------------------------------------------------------------------------

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Dispose subscription when activity is destroyed
        this.disposeWhenDestroy();
    }

    @Override
    public void onClickItem(int position) {

    }
}