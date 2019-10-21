package com.jpz.go4lunch.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.views.WorkmateViewHolder;

public class WorkmatesAdapter extends FirestoreRecyclerAdapter<Workmate, WorkmateViewHolder> {

    // Declaring callback
    private final Listener callback;

    // For data
    private final RequestManager glide;

    public WorkmatesAdapter(@NonNull FirestoreRecyclerOptions<Workmate> options,
                            RequestManager glide, Listener callback) {
        super(options);
        this.glide = glide;
        this.callback = callback;
    }

    // Create interface for callback
    public interface Listener {
        void onClickItem(String restaurantId, int position);
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolder and inflating its xml layout
        return new WorkmateViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wormate_item, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position, @NonNull Workmate model) {
        holder.updateViewHolder(model, this.glide, this.callback);
    }

}
