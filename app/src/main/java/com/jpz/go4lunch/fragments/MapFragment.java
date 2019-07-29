package com.jpz.go4lunch.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.jpz.go4lunch.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment {

    private static View rootView;

    private GoogleMap mMap;


    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_map, container, false);

        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_map, container, false);
        return rootView;

    }

}
