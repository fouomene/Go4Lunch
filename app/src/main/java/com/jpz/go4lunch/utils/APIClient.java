package com.jpz.go4lunch.utils;

import android.util.Log;

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

    private static final String TAG = APIClient.class.getSimpleName();

    // Public method to start fetching the result for Nearby Search request
    private static Observable<NearbySearchResponse> fetchNearbySearch(String latLng){
        // Get a Retrofit instance and the related Observable of the Interface
        Service service = Service.retrofit.create(Service.class);
        // Create the call on Top Stories API
        return service.getNearbySearchResponse(latLng, Service.API_RADIUS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

    // Public method to generify the result list of Nearby Search
    public static Observable<List<FieldRestaurant>> getNearbySearchRestaurants(String latLng){
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
                            Log.i(TAG,"restaurants name = " + fieldRestaurant.name);

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

    //----------------------------------------------------------------------------------

    // Public method to generify the result list of Nearby Search and get the restaurants location
    public static Observable<List<FieldRestaurant>> getNearbySearchRestaurantsOnMap(String latLng){
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
                            //fieldRestaurant.placeId = result.getPlaceId();
                            //Log.i(TAG,"restaurants id = " + fieldRestaurant.placeId);
                            fieldRestaurant.latLng =
                                    new LatLng(result.getGeometry().getLocation().getLat(),
                                            result.getGeometry().getLocation().getLng());
                            Log.i(TAG,"restaurants lng = " + fieldRestaurant.latLng);
                            fieldRestaurantList.add(fieldRestaurant);
                        }
                        return fieldRestaurantList;
                    }
                });
    }

}
