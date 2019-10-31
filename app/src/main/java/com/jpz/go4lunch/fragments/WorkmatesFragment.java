package com.jpz.go4lunch.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.WorkmatesAdapter;
import com.jpz.go4lunch.api.WorkmateHelper;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.MyUtilsNavigation;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends Fragment implements WorkmatesAdapter.Listener {

    // Declare View
    private RecyclerView recyclerView;

    private WorkmatesAdapter workmatesAdapter;

    private MyUtilsNavigation utilsNavigation = new MyUtilsNavigation();

    private static final String TAG = WorkmatesFragment.class.getSimpleName();

    public WorkmatesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        recyclerView = view.findViewById(R.id.workmates_recycler_view);
        setHasOptionsMenu(true);
        configureRecyclerView();
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.clear();
    }

    // Configure RecyclerView with a Query
    private void configureRecyclerView() {
        //Configure Adapter & RecyclerView
        workmatesAdapter = new WorkmatesAdapter(generateOptionsForAdapter
                (WorkmateHelper.getAllWorkmates()), Glide.with(this), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(workmatesAdapter);
    }

    // Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<Workmate> generateOptionsForAdapter(Query query) {
        return new FirestoreRecyclerOptions.Builder<Workmate>()
                .setQuery(query, Workmate.class)
                .setLifecycleOwner(this)
                .build();
    }

    @Override
    public void onClickItem(String restaurantId, int position) {
        Workmate workmate = workmatesAdapter.getItem(position);
        Log.i(TAG, "workmate name from position = " + workmate.getUsername());
        if (workmate.getRestaurantId() != null) {
            utilsNavigation.startDetailsRestaurantActivity(getActivity(), restaurantId);
        }
    }
}
