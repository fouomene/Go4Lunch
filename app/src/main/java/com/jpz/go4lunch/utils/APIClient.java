package com.jpz.go4lunch.utils;

import com.google.android.gms.maps.model.LatLng;
import com.jpz.go4lunch.models.FieldRestaurant;
import com.jpz.go4lunch.models.NearbySearchModels.NearbySearchResponse;
import com.jpz.go4lunch.models.NearbySearchModels.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class APIClient {
    // Class for streams of the the Google Places API with Observables of RxJava

    // Public method to start fetching the result for Nearby Search request
    private static Observable<NearbySearchResponse> fetchNearbySearch(LatLng latLng){
        // Get a Retrofit instance and the related Observable of the Interface
        Service service = Service.retrofit.create(Service.class);
        // Create the call on Top Stories API
        return service.getNearbySearchResponse(latLng, Service.API_RADIUS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    // Public method to generify the result list of Top Stories
    public static Observable<List<FieldRestaurant>> getNearbySearchRestaurants(LatLng latLng){
        return fetchNearbySearch(latLng)
                .map(new Function<NearbySearchResponse, List<Result>>() {
                    @Override
                    public List<Result> apply(NearbySearchResponse response) {
                        return response.getResults();
                    }
                }).map(new Function<List<Result>, List<FieldRestaurant>>() {
                    @Override
                    public List<FieldRestaurant> apply(List<Result> resultList) {

                        List<FieldRestaurant> fieldRestaurantList = new ArrayList<>();

                        for(Result result : resultList){
                            FieldRestaurant fieldRestaurant = new FieldRestaurant();

                            fieldRestaurant.name = result.getName();
                            //fieldRestaurant.type;
                            //fieldRestaurant.distance;
                            //fieldRestaurant.address;
                            //fieldRestaurant.workmates;
                            //fieldRestaurant.hours = result.getOpeningHours().getOpenNow();
                            //fieldRestaurant.opinions;
                            //fieldRestaurant.image;

                            fieldRestaurantList.add(fieldRestaurant);
                        }
                        return fieldRestaurantList;
                    }
                });
    }

}
