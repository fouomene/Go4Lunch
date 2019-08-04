package com.jpz.go4lunch.utils;

import com.google.android.gms.maps.model.LatLng;
import com.jpz.go4lunch.models.NearbySearchModels.NearbySearchResponse;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Service {
    // Interface for requests of the Google Places API

    //----------------------------------------------------------------------------------
    // Fields to complete requests

    // Base url for the Places API with an output in JSON
    String API_BASE_URL = "https://maps.googleapis.com/maps/api/place/";
    // Google API Key
    String API_KEY = "AIzaSyCjh-PspwJdk3CJ_UQALM0O3Wla-p1a2go";
    // Type of Point Of Interest we are looking for
    String API_TYPE = "restaurant";
    // Defines the distance (in meters) within which to bias place results.
    String API_RADIUS = "200";

    //----------------------------------------------------------------------------------

    // Create a Retrofit Object to do a request network
    Retrofit retrofit = new Retrofit.Builder()
            // Root URL
            .baseUrl(API_BASE_URL)
            // GSON converter
            .addConverterFactory(GsonConverterFactory.create())
            // RxJava Adapter
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

    // GET Nearby Search request with an output in JSON
    @GET("nearbysearch/json?key=" + API_KEY + "&type=" + API_TYPE)
    Observable<NearbySearchResponse>
    getNearbySearchResponse (@Query("location") LatLng latLng, @Query("radius") String radius);

}
