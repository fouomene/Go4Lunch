package com.jpz.go4lunch.api;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.jpz.go4lunch.models.Workmate;

import java.util.HashMap;
import java.util.Map;

public class WorkmateHelper {

    private static final String COLLECTION_NAME = "workmates";
    public static final String DOCUMENT_RESTAURANT_ID = "restaurantId";

    private Workmate workmate = new Workmate();

    private static final String TAG = WorkmateHelper.class.getSimpleName();

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getWorkmatesCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createWorkmate(String id, String username, String urlPicture,
                                            String restaurantId, String restaurantName) {
        Workmate workmateToCreate = new Workmate(id, username, urlPicture, restaurantId, restaurantName);
        return WorkmateHelper.getWorkmatesCollection().document(id).set(workmateToCreate);
    }

    // Method used to update or create a workmate without deleting the selectedPlace
    public void setUsernamePhotoWithMerge(String id, String username, String urlPicture) {
        // Update id, username and urlPicture fields, creating the document if it does not already exist.
        Map<String, Object> dataId = new HashMap<>();
        Map<String, Object> dataUsername = new HashMap<>();
        Map<String, Object> dataUrlPicture = new HashMap<>();
        // Update id
        dataUsername.put("id", id);
        getWorkmatesCollection().document(id).set(dataId, SetOptions.merge());
        // Update username
        dataUsername.put("username", username);
        getWorkmatesCollection().document(id).set(dataUsername, SetOptions.merge());
        // Update urlPicture
        dataUrlPicture.put("urlPicture", urlPicture);
        getWorkmatesCollection().document(id).set(dataUrlPicture, SetOptions.merge());
    }

    // --- QUERY ---

    // Retrieve all workmates and class them especially by a restaurant choice for WorkmatesFragment
    public static Query getAllWorkmates(){
        return WorkmateHelper.getWorkmatesCollection()
                .orderBy("restaurantName", Query.Direction.DESCENDING)
                .orderBy("username", Query.Direction.DESCENDING);
    }

    // Retrieve all workmates with the same restaurant choice for DetailsRestaurantActivity
    public static Query getWorkmatesAtRestaurant(String id){
        return WorkmateHelper.getWorkmatesCollection()
                .whereEqualTo("restaurantId", id);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getCurrentWorkmate(String id){
        return WorkmateHelper.getWorkmatesCollection().document(id).get();
    }

    public void getRestaurantChoice(String id) {
        DocumentReference docRef = getWorkmatesCollection().document(id);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    workmate.setRestaurantId(document.getString("restaurantId"));
                    Log.i(TAG, "restaurant choice = " + workmate.getRestaurantId() + workmate.getRestaurantName());
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    // --- LISTENER ---

    public void listenToRestaurantChoice(String id) {
        final DocumentReference docRef = getWorkmatesCollection().document(id);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Set the restaurant choice with the data from Firestore
                workmate.setRestaurantId(snapshot.getString("restaurantId"));
                Log.d(TAG, "Current data: " + snapshot.getData());
            } else {
                Log.d(TAG, "Current data: null. Create workmate");
            }
        });
    }

    // --- UPDATE ---

    // Update the choice of the workmate's restaurant
    public static Task<Void> updateRestaurant(String id, String placeId, String placeName) {
        return WorkmateHelper.getWorkmatesCollection().document(id)
                .update("restaurantId", placeId, "restaurantName", placeName);
    }

    // --- DELETE ---

    public static Task<Void> deleteWorkmate(String id) {
        return WorkmateHelper.getWorkmatesCollection().document(id).delete();
    }

}
