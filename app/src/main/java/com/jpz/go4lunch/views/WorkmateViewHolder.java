package com.jpz.go4lunch.views;

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

import java.lang.ref.WeakReference;

public class WorkmateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    // Represent an item (line) of a workmate in the RecyclerView

    // Views
    private TextView textView;
    private ImageView workmateImage;

    // Declare a Weak Reference to our Callback
    private WeakReference<WorkmatesAdapter.Listener> callbackWeakRef;

    public WorkmateViewHolder(@NonNull View itemView) {
        super(itemView);
        textView = itemView.findViewById(R.id.item_workmate_text);
        workmateImage = itemView.findViewById(R.id.item_workmate_image);
    }

    public void updateViewHolder(Workmate workmate, RequestManager glide, WorkmatesAdapter.Listener callback){
        // Update text
        textView.setText(workmate.getUsername());
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
        WorkmatesAdapter.Listener callback = callbackWeakRef.get();
        if (callback != null) callback.onClickItem(getAdapterPosition());
    }
}
