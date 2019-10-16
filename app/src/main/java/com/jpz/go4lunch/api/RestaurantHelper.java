package com.jpz.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.jpz.go4lunch.models.Restaurant;

import java.util.HashMap;
import java.util.Map;

public class RestaurantHelper {

    private static final String COLLECTION_NAME_RESTAURANTS = "restaurants";

    private Restaurant restaurant = new Restaurant();

    private static final String TAG = RestaurantHelper.class.getSimpleName();

    // --- COLLECTION REFERENCE ---

    private static CollectionReference getRestaurantsCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME_RESTAURANTS);
    }

    // --- CREATE ---

    // Method used to update or create a restaurant without deleting the list of workmate
    public void setIdNameWorkmatesWithMerge(String id, String name, String workmate) {
        // Update id, name and workmate fields, creating the document if it does not already exist.
        Map<String, Object> dataId = new HashMap<>();
        Map<String, Object> dataName = new HashMap<>();
        // Update id
        dataName.put("id", id);
        getRestaurantsCollection().document(id).set(dataId, SetOptions.merge());
        // Update name
        dataName.put("name", name);
        getRestaurantsCollection().document(id).set(dataName, SetOptions.merge());
        // Automatically add a new workmate to the "workmateList" array field.
        DocumentReference documentReference = getRestaurantsCollection().document(id);
        documentReference.update("workmateList", FieldValue.arrayUnion(workmate));
    }

    // --- QUERY ---

    public static Query getAllRestaurants(){
        return RestaurantHelper.getRestaurantsCollection()
                .orderBy("name");
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getCurrentRestaurant(String id){
        return RestaurantHelper.getRestaurantsCollection().document(id).get();
    }

    // --- LISTENER ---


    // --- UPDATE ---


    // --- DELETE ---

    public void deleteWorkmate(String id, String workmate) {
        DocumentReference washingtonRef = getRestaurantsCollection().document(id);
        // Automatically remove a workmate from the "workmateList" array field.
        washingtonRef.update("workmateList", FieldValue.arrayRemove(workmate));
    }

}
