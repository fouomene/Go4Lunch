package com.jpz.go4lunch.models;

import androidx.annotation.Nullable;

import java.util.List;

public class Workmate {

    private String id;
    private String username;
    @Nullable
    private String urlPicture;
    @Nullable
    private String restaurantId;
    @Nullable
    private String restaurantName;
    @Nullable
    private List<String> restaurantsLikedId;

    /*
    Cloud Firestore will internally convert the objects to supported data types.
    Each custom class must have a public constructor that takes no arguments. In addition,
     the class must include a public getter for each property.
     */

    public Workmate() {

    }

    public Workmate(String id, String username, @Nullable String urlPicture, @Nullable String restaurantId,
                    @Nullable String restaurantName, @Nullable List<String> restaurantsLikedId) {
        this.id = id;
        this.username = username;
        this.restaurantsLikedId = restaurantsLikedId;
        this.urlPicture = urlPicture;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Nullable
    public List<String> getRestaurantsLikedId() {
        return restaurantsLikedId;
    }

    @Nullable
    public String getUrlPicture() {
        return urlPicture;
    }

    @Nullable
    public String getRestaurantId() {
        return restaurantId;
    }

    @Nullable
    public String getRestaurantName() {
        return restaurantName;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRestaurantsLikedId(@Nullable List<String> restaurantsLikedId) {
        this.restaurantsLikedId = restaurantsLikedId;
    }

    public void setUrlPicture(@Nullable String urlPicture) {
        this.urlPicture = urlPicture;
    }

    public void setRestaurantId(@Nullable String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public void setRestaurantName(@Nullable String restaurantName) {
        this.restaurantName = restaurantName;
    }
}
