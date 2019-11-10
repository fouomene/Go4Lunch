package com.jpz.go4lunch.models;

import com.google.android.libraries.places.api.model.Place;

public class RestaurantDataToSort {
    // Data to sort the list of restaurants

    private Place place;
    private int proximity;
    private double rating;
    private int numberWorkmates;

    public RestaurantDataToSort(Place place, int proximity, double rating, int numberWorkmates) {
        this.place = place;
        this.proximity = proximity;
        this.rating = rating;
        this.numberWorkmates = numberWorkmates;
    }

    public Place getPlace() {
        return place;
    }

    public int getProximity() {
        return proximity;
    }

    public double getRating() {
        return rating;
    }

    public int getNumberWorkmates() {
        return numberWorkmates;
    }

}
