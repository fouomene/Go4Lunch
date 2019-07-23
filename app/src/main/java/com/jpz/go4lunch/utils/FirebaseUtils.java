package com.jpz.go4lunch.utils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseUtils {

    // Method to recover the user currently connected
    @Nullable
    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    // Method to know the user is connected
    public Boolean isCurrentUserLogged(){
        return (getCurrentUser() != null);
    }


}
