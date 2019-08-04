package com.jpz.go4lunch.views;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.AdapterListRestaurant;
import com.jpz.go4lunch.models.FieldRestaurant;

import java.lang.ref.WeakReference;


public class ViewHolderRestaurant extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Represent an item (line) in the RecyclerView

    private TextView name, distance, type, address, workmates, hours, opinions;
    private ImageView restaurantImage;

    // Declare a Weak Reference to our Callback
    private WeakReference<AdapterListRestaurant.Listener> callbackWeakRef;

    public ViewHolderRestaurant(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.item_name);
        distance = itemView.findViewById(R.id.item_distance);
        type = itemView.findViewById(R.id.item_type);
        address = itemView.findViewById(R.id.item_address);
        workmates = itemView.findViewById(R.id.item_workmates);
        hours = itemView.findViewById(R.id.item_hours);
        opinions = itemView.findViewById(R.id.item_opinions);

        restaurantImage = itemView.findViewById(R.id.item_image_restaurant);
    }

    public void updateViewHolder(FieldRestaurant fieldRestaurant, RequestManager glide,
                                 AdapterListRestaurant.Listener callback){
        // Update widgets
        name.setText(fieldRestaurant.name);
        distance.setText(fieldRestaurant.distance);
        type.setText(fieldRestaurant.type);
        address.setText(fieldRestaurant.address);
        workmates.setText(fieldRestaurant.workmates);
        hours.setText(fieldRestaurant.hours);
        opinions.setText(fieldRestaurant.opinions);

        glide.load(fieldRestaurant.image).into(restaurantImage);

        // Create a new weak Reference to our Listener
        this.callbackWeakRef = new WeakReference<>(callback);
        // Implement Listener
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // When a click happens, we fire our listener to get the item position in the list
        AdapterListRestaurant.Listener callback = callbackWeakRef.get();
        if (callback != null) callback.onClickItem(getAdapterPosition());
    }
}
