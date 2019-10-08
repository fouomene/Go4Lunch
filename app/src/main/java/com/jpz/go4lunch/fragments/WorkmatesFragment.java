package com.jpz.go4lunch.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jpz.go4lunch.R;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.FirebaseUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends Fragment {

    private TextView textView;
    private Workmate workmate = new Workmate();
    private FirebaseUtils firebaseUtils = new FirebaseUtils();


    public WorkmatesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);

        textView = view.findViewById(R.id.text_view);

        updateWorkmatesData();

        textView.setText(workmate.getUsername());

        return view;
    }

    private void updateWorkmatesData() {
        if (firebaseUtils.getCurrentUser() != null) {
            workmate.setUsername(firebaseUtils.getCurrentUser().getDisplayName());
        }
    }

}
