package com.jpz.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.jpz.go4lunch.models.Workmate;

public class WorkmateHelper {

    private static final String COLLECTION_NAME = "workmates";

    // --- COLLECTION REFERENCE ---

    private static CollectionReference getWorkmatesCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createWorkmate(String uid, String username, String urlPicture, String place) {
        Workmate workmateToCreate = new Workmate(uid, username, urlPicture, place);
        return WorkmateHelper.getWorkmatesCollection().document(uid).set(workmateToCreate);
    }

    // --- GET ---

    public static Task<DocumentSnapshot> getAWorkmate(String uid){
        return WorkmateHelper.getWorkmatesCollection().document(uid).get();
    }



    public static Query getAllWorkmates(){
        return WorkmateHelper.getWorkmatesCollection()
                .orderBy("uid");
    }


    /*
    public static Task<DocumentSnapshot> getWorkmateUsername(String username){
        return WorkmateHelper.getWorkmatesCollection().document(username).get();
    }

    public static Task<DocumentSnapshot> getWorkmatePhoto(String urlPicture){
        return WorkmateHelper.getWorkmatesCollection().document(urlPicture).get();
    }

    public static Task<DocumentSnapshot> getWorkmateRestaurant(String place){
        return WorkmateHelper.getWorkmatesCollection().document(place).get();
    }

     */

    // --- UPDATE ---

    public static Task<Void> updateRestaurant(String uid, String place) {
        return WorkmateHelper.getWorkmatesCollection().document(uid).update("restaurant", place);
    }

    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return WorkmateHelper.getWorkmatesCollection().document(uid).delete();
    }

}
