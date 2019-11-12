package com.jpz.go4lunch.views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.adapters.WorkmatesAdapter;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.ConvertData;

import java.lang.ref.WeakReference;

public class WorkmateViewHolder extends RecyclerView.ViewHolder {
    // Represent an item (line) of a workmate in the RecyclerView

    // Views
    private TextView textView;
    private ImageView workmateImage;

    // Utils
    private ConvertData convertData = new ConvertData();

    private Context context;

    public WorkmateViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_workmate_text);
        workmateImage = itemView.findViewById(R.id.item_workmate_image);
        context = itemView.getContext();
    }

    public void updateViewHolder(Workmate workmate, RequestManager glide, WorkmatesAdapter.Listener callback) {
        // Update text
        if (workmate.getRestaurantId() != null && convertData.getTodayDate().equals(workmate.getRestaurantDate())) {
            textView.setTextColor(context.getResources().getColor(android.R.color.black));
            textView.setTypeface(null);
            textView.setText(context.getString(R.string.workmate_has_a_restaurant_choice,
                    workmate.getUsername(), workmate.getRestaurantName()));
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.darkGrey));
            textView.setTypeface(null, Typeface.ITALIC);
            textView.setText(context.getString(R.string.workmate_has_not_a_restaurant_choice,
                    workmate.getUsername()));
        }

        // Update image
        if (workmate.getUrlPicture() != null) {
            glide.load(workmate.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(workmateImage);
        }

        // Create a new weak Reference to our Listener
        WeakReference<WorkmatesAdapter.Listener> callbackWeakRef = new WeakReference<>(callback);
        callback = callbackWeakRef.get();

        // Redefine callback to use it with lambda
        WorkmatesAdapter.Listener finalCallback = callback;
        // Implement Listener
        itemView.setOnClickListener(v -> {
            // When a click happens, we fire our listener to get the item position in the list
            //callback = callbackWeakRef.get();
            if (finalCallback != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                if (convertData.getTodayDate().equals(workmate.getRestaurantDate())) {
                    finalCallback.onClickItem(workmate.getRestaurantId(), getAdapterPosition());
                }
            }
        });
    }

}
