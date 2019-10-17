package com.jpz.go4lunch.models;

import androidx.annotation.Nullable;

import java.util.List;

public class Restaurant {

    private String id;
    private String name;
    @Nullable private List<String> workmateList;
    private int likes;

    /*
    Cloud Firestore will internally convert the objects to supported data types.
    Each custom class must have a public constructor that takes no arguments. In addition,
     the class must include a public getter for each property.
     */

    public Restaurant() {

    }

    public Restaurant(String id, String name, @Nullable List<String> workmateList, int likes) {
        this.id = id;
        this.name = name;
        this.workmateList = workmateList;
        this.likes = likes;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public List<String> getWorkmateList() {
        return workmateList;
    }

    public int getLikes() {
        return likes;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWorkmateList(@Nullable List<String> workmateList) {
        this.workmateList = workmateList;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
