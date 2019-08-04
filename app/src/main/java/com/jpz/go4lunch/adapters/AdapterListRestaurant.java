package com.jpz.go4lunch.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.models.FieldRestaurant;
import com.jpz.go4lunch.views.ViewHolderRestaurant;

import java.util.List;

public class AdapterListRestaurant extends RecyclerView.Adapter<ViewHolderRestaurant> {

    // Declaring callback
    private final Listener callback;

    // For data
    private List<FieldRestaurant> fieldRestaurantList;

    // Declaring a Glide object
    private RequestManager glide;

    public AdapterListRestaurant(List<FieldRestaurant> fieldRestaurantList,
                                 RequestManager glide, Listener callback) {
        this.fieldRestaurantList = fieldRestaurantList;
        this.glide = glide;
        this.callback = callback;
    }

    // Create interface for callback
    public interface Listener {
        void onClickItem(int position);
    }

    @NonNull
    @Override
    public ViewHolderRestaurant onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create ViewHolderNews and inflating its xml layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_item, parent, false);

        return new ViewHolderRestaurant(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRestaurant holder, int position) {
        holder.updateViewHolder(this.fieldRestaurantList.get(position), this.glide, this.callback);
    }

    @Override
    public int getItemCount() {
        return this.fieldRestaurantList.size();
    }

    // Return the position of an item in the list
    public FieldRestaurant getPosition(int position){
        return this.fieldRestaurantList.get(position);
    }
}
