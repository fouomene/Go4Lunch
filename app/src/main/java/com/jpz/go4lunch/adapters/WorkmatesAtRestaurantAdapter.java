package com.jpz.go4lunch.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.views.WorkmatesAtRestaurantViewHolder;

public class WorkmatesAtRestaurantAdapter extends FirestoreRecyclerAdapter<Workmate,
        WorkmatesAtRestaurantViewHolder> {

    // For data
    private final RequestManager glide;

    public WorkmatesAtRestaurantAdapter(@NonNull FirestoreRecyclerOptions<Workmate> options,
                            RequestManager glide) {
        super(options);
        this.glide = glide;
    }

    @NonNull
    @Override
    public WorkmatesAtRestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder and inflating its xml layout
        return new WorkmatesAtRestaurantViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.workmates_at_restaurant_item, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmatesAtRestaurantViewHolder holder, int position,
                                    @NonNull Workmate model) {
        holder.updateViewHolder(model, this.glide);
    }

}
