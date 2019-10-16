package com.jpz.go4lunch.api;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.jpz.go4lunch.models.Workmate;

import java.util.HashMap;
import java.util.Map;

public class WorkmateHelper {

    private static final String COLLECTION_NAME = "workmates";

    private Workmate workmate = new Workmate();

    private static final String TAG = WorkmateHelper.class.getSimpleName();

    // --- COLLECTION REFERENCE ---

    private static CollectionReference getWorkmatesCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createWorkmate(String uid, String username, String urlPicture, String selectedPlace) {
        Workmate workmateToCreate = new Workmate(uid, username, urlPicture, selectedPlace);
        return WorkmateHelper.getWorkmatesCollection().document(uid).set(workmateToCreate);
    }

    // Method used to update or create a workmate without deleting the selectedPlace
    public void setUidUsernamePhotoWithMerge(String uid, String username, String urlPicture) {
        // Update uid, username and urlPicture fields, creating the document if it does not already exist.
        Map<String, Object> dataUid = new HashMap<>();
        Map<String, Object> dataUsername = new HashMap<>();
        Map<String, Object> dataUrlPicture = new HashMap<>();
        // Update uid
        dataUsername.put("uid", uid);
        getWorkmatesCollection().document(uid).set(dataUid, SetOptions.merge());
        // Update username
        dataUsername.put("username", username);
        getWorkmatesCollection().document(uid).set(dataUsername, SetOptions.merge());
        // Update urlPicture
        dataUrlPicture.put("urlPicture", urlPicture);
        getWorkmatesCollection().document(uid).set(dataUrlPicture, SetOptions.merge());
    }

    // --- GET ---

    public static Query getAllWorkmates(){
        return WorkmateHelper.getWorkmatesCollection()
                .orderBy("selectedPlace", Query.Direction.DESCENDING)
                .orderBy("username", Query.Direction.DESCENDING);
    }

    public static Task<DocumentSnapshot> getCurrentWorkmate(String uid){
        return WorkmateHelper.getWorkmatesCollection().document(uid).get();
    }

    public void getRestaurantChoice(String uid) {
        DocumentReference docRef = getWorkmatesCollection().document(uid);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    workmate.setSelectedPlace(document.getString("selectedPlace"));
                    Log.i(TAG, "restaurant choice = " + workmate.getSelectedPlace());
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });
    }

    // --- LISTENER ---

    public void listenToRestaurantChoice(String uid) {
        final DocumentReference docRef = getWorkmatesCollection().document(uid);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                // Set the restaurant choice with the data from Firestore
                workmate.setSelectedPlace(snapshot.getString("selectedPlace"));
                Log.d(TAG, "Current data: " + snapshot.getData());
            } else {
                Log.d(TAG, "Current data: null. Create workmate");
            }
        });
    }

    // --- UPDATE ---

    // Update the choice of the workmate's restaurant
    public static Task<Void> updateRestaurant(String uid, String place) {
        return WorkmateHelper.getWorkmatesCollection().document(uid).update("selectedPlace", place);
    }

    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return WorkmateHelper.getWorkmatesCollection().document(uid).delete();
    }

}
