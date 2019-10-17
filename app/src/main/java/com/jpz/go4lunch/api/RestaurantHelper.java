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
    public void setIdNameWorkmates(String id, String name, String workmate) {
        // Update id, name and workmate fields, creating the document if it does not already exist.
        Map<String, Object> dataId = new HashMap<>();
        // Update id
        dataId.put("id", id);
        getRestaurantsCollection().document(id).set(dataId, SetOptions.merge());
        // Update name
        Map<String, Object> dataName = new HashMap<>();
        dataName.put("name", name);
        getRestaurantsCollection().document(id).set(dataName, SetOptions.merge());
        // Automatically add a new workmate to the "workmateList" array field.
        DocumentReference documentReference = getRestaurantsCollection().document(id);
        documentReference.update("workmateList", FieldValue.arrayUnion(workmate));
    }

    // Method used to create or update a restaurant with an id and a name
    public void setIdName(String id, String name) {
        // Update id and name fields, creating the document if it doesn't already exist.
        // Update id
        Map<String, Object> dataId = new HashMap<>();
        dataId.put("id", id);
        getRestaurantsCollection().document(id).set(dataId, SetOptions.merge());
        // Update name
        Map<String, Object> dataName = new HashMap<>();
        dataName.put("name", name);
        getRestaurantsCollection().document(id).set(dataName, SetOptions.merge());
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

    // Automatically increment the likes of the restaurant by 1
    public static Task<Void> addLike(String id) {
        return RestaurantHelper.getRestaurantsCollection().document(id)
                .update("likes", FieldValue.increment(1));
    }

    // Automatically decrement the likes of the restaurant by 1
    public static Task<Void> removeLike(String id) {
        return RestaurantHelper.getRestaurantsCollection().document(id)
                .update("likes", FieldValue.increment(-1));
    }

    // --- DELETE ---

    public void deleteWorkmate(String id, String workmate) {
        DocumentReference documentReference = getRestaurantsCollection().document(id);
        // Automatically remove a workmate from the "workmateList" array field.
        documentReference.update("workmateList", FieldValue.arrayRemove(workmate));
    }

}
