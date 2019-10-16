package com.jpz.go4lunch.views;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.WorkmatesAtRestaurantAdapter;
import com.jpz.go4lunch.models.Workmate;

import java.lang.ref.WeakReference;

public class WorkmatesAtRestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Represent an item (line) of a workmate in the RecyclerView

    // Views
    private TextView textView;
    private ImageView workmateImage;

    // Declare a Weak Reference to our Callback
    private WeakReference<WorkmatesAtRestaurantAdapter.Listener> callbackWeakRef;

    private Context context;

    public WorkmatesAtRestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_workmates_at_restaurant_text);
        workmateImage = itemView.findViewById(R.id.item_workmates_at_restaurant_image);

        context = itemView.getContext();
    }

    public void updateViewHolder(Workmate workmate, RequestManager glide, WorkmatesAtRestaurantAdapter.Listener callback){
        // Update text
        textView.setTextColor(context.getResources().getColor(android.R.color.black));
        textView.setText(context.getString(R.string.workmate_is_joining, workmate.getUsername()));
        // Update image
        if (workmate.getUrlPicture() != null) {
            glide.load(workmate.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(workmateImage);
        }

        // Create a new weak Reference to our Listener
        this.callbackWeakRef = new WeakReference<>(callback);
        // Implement Listener
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // When a click happens, we fire our listener to get the item position in the list
        WorkmatesAtRestaurantAdapter.Listener callback = callbackWeakRef.get();
        if (callback != null) callback.onClickItem(getAdapterPosition());
    }
}
