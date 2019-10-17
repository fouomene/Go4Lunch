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
import com.jpz.go4lunch.models.Workmate;

public class WorkmatesAtRestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Represent an item (line) of a workmate in the RecyclerView

    // Views
    private TextView textView;
    private ImageView workmateImage;

    private Context context;

    public WorkmatesAtRestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_workmates_at_restaurant_text);
        workmateImage = itemView.findViewById(R.id.item_workmates_at_restaurant_image);

        context = itemView.getContext();
    }

    public void updateViewHolder(Workmate workmate, RequestManager glide){
        // Update text
        textView.setTextColor(context.getResources().getColor(android.R.color.black));
        textView.setText(context.getString(R.string.workmate_is_joining, workmate.getUsername()));
        // Update image
        if (workmate.getUrlPicture() != null) {
            glide.load(workmate.getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(workmateImage);
        }
    }

    @Override
    public void onClick(View v) {
        // void
    }
}
