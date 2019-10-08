package com.jpz.go4lunch.models;

import androidx.annotation.Nullable;

public class Workmate {

    private String uid;
    private String username;
    @Nullable private String urlPicture;
    @Nullable private String selectedPlace;

    public Workmate() {

    }

    public Workmate(String uid, String username, @Nullable String urlPicture, @Nullable String selectedPlace) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.selectedPlace = selectedPlace;
    }

    // Getters
    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    @Nullable public String getUrlPicture() {
        return urlPicture;
    }

    @Nullable public String getSelectedPlace() {
        return selectedPlace;
    }

    // Setters


    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUrlPicture(@Nullable String urlPicture) {
        this.urlPicture = urlPicture;
    }

    public void setSelectedPlace(@Nullable String selectedPlace) {
        this.selectedPlace = selectedPlace;
    }
}
