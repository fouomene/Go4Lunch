package com.jpz.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.ConvertData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkmateHelper {

    private static final String COLLECTION_NAME = "workmates";
    private static final String FIELD_ID = "id";
    public static final String FIELD_USERNAME = "username";
    private static final String FIELD_URLPICTURE = "urlPicture";
    public static final String FIELD_RESTAURANT_ID = "restaurantId";
    private static final String FIELD_RESTAURANT_NAME = "restaurantName";
    private static final String FIELD_RESTAURANT_ADDRESS = "restaurantAddress";
    public static final String FIELD_RESTAURANT_DATE = "restaurantDate";
    private static final String FIELD_RESTAURANTS_LIKED = "restaurantsLikedId";

    private static final String TAG = WorkmateHelper.class.getSimpleName();

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getWorkmatesCollection() {
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createWorkmate(String id, String username, String urlPicture, String restaurantId,
                                            String restaurantName, String restaurantAddress,
                                            String restaurantDate, List<String> restaurantsLikedId) {
        Workmate workmateToCreate = new Workmate(id, username, urlPicture, restaurantId,
                restaurantName, restaurantAddress, restaurantDate, restaurantsLikedId);
        return WorkmateHelper.getWorkmatesCollection().document(id).set(workmateToCreate);
    }

    public void addLike(String id, String restaurantId) {
        // Add a new restaurantId to the "restaurantsLikedId" array field.
        DocumentReference documentReference = getWorkmatesCollection().document(id);
        documentReference.update(FIELD_RESTAURANTS_LIKED, FieldValue.arrayUnion(restaurantId));
    }

    // Method used to update or create a workmate without deleting the selectedPlace
    public void setUsernamePhotoWithMerge(String id, String username, String urlPicture) {
        // Update id, username and urlPicture fields, creating the document if it does not already exist.
        Map<String, Object> dataId = new HashMap<>();
        Map<String, Object> dataUsername = new HashMap<>();
        Map<String, Object> dataUrlPicture = new HashMap<>();
        // Update id
        dataUsername.put(FIELD_ID, id);
        getWorkmatesCollection().document(id).set(dataId, SetOptions.merge());
        // Update username
        dataUsername.put(FIELD_USERNAME, username);
        getWorkmatesCollection().document(id).set(dataUsername, SetOptions.merge());
        // Update urlPicture
        dataUrlPicture.put(FIELD_URLPICTURE, urlPicture);
        getWorkmatesCollection().document(id).set(dataUrlPicture, SetOptions.merge());
    }

    // --- QUERY ---

    // Retrieve all workmates and class them especially by a restaurant choice for WorkmatesFragment
    public static Query getAllWorkmates(String restaurantDate) {
        return getWorkmatesCollection()
                //.whereEqualTo(FIELD_RESTAURANT_DATE, restaurantDate)
                .orderBy(FIELD_RESTAURANT_NAME, Query.Direction.DESCENDING)
                .orderBy(FIELD_USERNAME, Query.Direction.DESCENDING);
    }

    // Retrieve all workmates with the same restaurant choice
    // on the same day for DetailsRestaurantActivity
    public static Query getWorkmatesAtRestaurant(String id, String restaurantDate) {
        return getWorkmatesCollection()
                .whereEqualTo(FIELD_RESTAURANT_ID, id)
                .whereEqualTo(FIELD_RESTAURANT_DATE, restaurantDate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getCurrentWorkmate(String id) {
        return getWorkmatesCollection().document(id).get();
    }

    // --- LISTENER ---

    // --- UPDATE ---

    // Update the choice of the workmate's restaurant
    public static Task<Void> updateRestaurant(String id, String placeId, String placeName,
                                              String placeAddress, String restaurantDate) {
        return getWorkmatesCollection()
                .document(id)
                .update(FIELD_RESTAURANT_ID, placeId, FIELD_RESTAURANT_NAME, placeName,
                        FIELD_RESTAURANT_ADDRESS, placeAddress, FIELD_RESTAURANT_DATE, restaurantDate);
    }

    // --- DELETE ---

    public static Task<Void> deleteWorkmate(String id) {
        return getWorkmatesCollection().document(id).delete();
    }

    public void removeLike(String id, String restaurantId) {
        DocumentReference documentReference = getWorkmatesCollection().document(id);
        // Remove a restaurantId from the "restaurantsLikedId" array field.
        documentReference.update(FIELD_RESTAURANTS_LIKED, FieldValue.arrayRemove(restaurantId));
    }

}
