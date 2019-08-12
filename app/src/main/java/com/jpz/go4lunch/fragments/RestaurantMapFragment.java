package com.jpz.go4lunch.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.activities.DetailsRestaurantActivity;
import com.jpz.go4lunch.models.FieldRestaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.jpz.go4lunch.activities.MainActivity.PERMS;
import static com.jpz.go4lunch.activities.MainActivity.RC_LOCATION;


/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantMapFragment extends Fragment implements OnMapReadyCallback {

    // Google Mobile Services Objects
    private MapView mMapView;
    private GoogleMap googleMap;
    private CameraPosition cameraPosition;

    // Keys for storing activity state
    private static final String MAPVIEW_BUNDLE_KEY = "map_view_bundle_key";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";

    // Key for Intent
    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";

    // Places
    private PlacesClient placesClient;
    private FindCurrentPlaceRequest request;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private OnListIdListener mCallback;

    private FieldRestaurant fieldRestaurant = new FieldRestaurant("id");
    private ArrayList<FieldRestaurant> fieldRestaurantList = new ArrayList<>();
    private String restaurantId;
    private LatLng restaurantLatLng;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 17;

    private static final String TAG = RestaurantMapFragment.class.getSimpleName();

    public RestaurantMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get layout of this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant_map, container, false);

        mMapView = view.findViewById(R.id.map_view);
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        if (getActivity() != null) {
            // Construct a FusedLocationProviderClient
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

            // Initialize the SDK
            Places.initialize(getActivity(), getString(R.string.google_api_key));

            // Create a new Places client instance
            placesClient = Places.createClient(getActivity());
        }

        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.TYPES, Place.Field.LAT_LNG, Place.Field.ID);

        // Use the builder to create a FindCurrentPlaceRequest.
        request = FindCurrentPlaceRequest.newInstance(placeFields);

        // Declare FloatingActionButton and its behavior
        FloatingActionButton floatingActionButton = view.findViewById(R.id.fragment_restaurant_map_fab);
        floatingActionButton.setOnClickListener((View v) -> {
            // Get the current location of the device and set the position of the map
            getDeviceLocation();
        });

        return view;
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Prevent to show the My Location button, the MapToolbar and the Compass
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);

        // If permissions are granted, turn on My Location and the related control on the map
        updateLocationUI();

        // Hide POI of business on the map
        hideBusinessPOI();

        // Show the restaurants near the user location
        findCurrentPlace();

        mCallback.onListId(fieldRestaurantList);

        if (googleMap != null)
            googleMap.setOnMarkerClickListener((Marker marker) -> {
                // Retrieve the data from the marker.
                restaurantId = (String) marker.getTag();
                startDetailsRestaurantActivity();
                // Return false to indicate that we have not consumed the event and that we wish
                // for the default behavior to occur.
                return false;
            });
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);

        // Saves the state of the map when the activity is paused
        if (googleMap != null) {
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
            outState.putParcelable(KEY_CAMERA_POSITION, googleMap.getCameraPosition());
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //----------------------------------------------------------------------------------

    @AfterPermissionGranted(RC_LOCATION)
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (getActivity() != null)
            try {
                if (EasyPermissions.hasPermissions(getActivity(), PERMS)) {
                    Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                    locationResult.addOnCompleteListener(getActivity(), (@NonNull Task<Location> task) -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                // Set the map's camera position to the current location of the device.
                                lastKnownLocation = task.getResult();
                                final LatLng currentLocation =
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude());
                                // Construct a CameraPosition focusing on the current location...
                                // ...and animate the camera to that position.
                                cameraPosition = new CameraPosition.Builder()
                                        .target(currentLocation)
                                        .zoom(DEFAULT_ZOOM)
                                        .build();
                                if (googleMap != null)
                                    googleMap.animateCamera(CameraUpdateFactory
                                        .newCameraPosition(cameraPosition));
                            } else {
                                Log.i(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                if (googleMap != null)
                                    googleMap.moveCamera(CameraUpdateFactory
                                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            }
                    });
                } else {
                    if (getActivity() != null)
                        EasyPermissions.requestPermissions(getActivity(),
                                getString(R.string.rationale_permission_location_access),
                                RC_LOCATION, PERMS);
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
    }

    @AfterPermissionGranted(RC_LOCATION)
    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (getActivity() != null)
                if (EasyPermissions.hasPermissions(getActivity(), PERMS)) {
                    // Go to My Location and give the related control on the map
                    googleMap.setMyLocationEnabled(true);
                    getDeviceLocation();
                } else {
                    googleMap.setMyLocationEnabled(false);
                    googleMap = null;
                    EasyPermissions.requestPermissions(getActivity(),
                                    getString(R.string.rationale_permission_location_access),
                                    RC_LOCATION, PERMS);
                }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void hideBusinessPOI() {
        if (getActivity() != null)
            try {
                if (googleMap != null) {
                    // Customise the styling of the base map using a JSON object defined
                    // in a raw resource file.
                    boolean success = googleMap.setMapStyle(
                            MapStyleOptions.loadRawResourceStyle(
                                    getActivity(), R.raw.style_json));
                    if (!success) {
                        Log.e(TAG, "Style parsing failed.");
                    }
                }
            } catch (Resources.NotFoundException e) {
                Log.e(TAG, "Can't find style. Error: ", e);
            }
    }

    @AfterPermissionGranted(RC_LOCATION)
    private void findCurrentPlace() {
        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (getActivity() != null)
            try {
                if (EasyPermissions.hasPermissions(getActivity(), PERMS)) {
                    Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
                    placeResponse.addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            FindCurrentPlaceResponse response = task.getResult();

                            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {

                                if (placeLikelihood.getPlace().getTypes() != null
                                        && placeLikelihood.getPlace().getLatLng() != null)
                                    if (placeLikelihood.getPlace().getTypes()
                                            .contains(Place.Type.RESTAURANT)) {

                                        // Collect the LatLng of the places likelihood
                                        restaurantLatLng = new LatLng(placeLikelihood
                                                        .getPlace().getLatLng().latitude,
                                                        placeLikelihood
                                                                .getPlace().getLatLng().longitude);

                                        // Collect the identities of the places likelihood
                                        fieldRestaurant.id = placeLikelihood.getPlace().getId();

                                        saveListId(fieldRestaurant);

                                        Log.i(TAG, "Place has id = " + fieldRestaurant.id);

                                        if (googleMap != null) {
                                            addMarkers();
                                        }
                                    }
                            }
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof ApiException) {
                                ApiException apiException = (ApiException) exception;
                                Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                            }
                        }
                    });
                } else {
                    EasyPermissions.requestPermissions(getActivity(),
                            getString(R.string.rationale_permission_location_access),
                            RC_LOCATION, PERMS);
                }
            } catch (SecurityException e) {
                Log.e("Exception: %s", e.getMessage());
            }
    }

    //----------------------------------------------------------------------------------

    private void startDetailsRestaurantActivity() {
        Intent intent = new Intent(getActivity(), DetailsRestaurantActivity.class);
        intent.putExtra(KEY_RESTAURANT_ID, restaurantId);
        startActivity(intent);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {

        // Create background
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_map_pin);
        if (background == null) {
            Log.e(TAG, "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }
        background.setBounds(0, 0,
                background.getIntrinsicWidth(), background.getIntrinsicHeight());

        // Create vector
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            Log.e(TAG, "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }
        int left = (background.getIntrinsicWidth() - vectorDrawable.getIntrinsicWidth()) / 2;
        int top = (background.getIntrinsicHeight() - vectorDrawable.getIntrinsicHeight()) / 3;
        vectorDrawable.setBounds(left, top, left + vectorDrawable.getIntrinsicWidth(),
                top + vectorDrawable.getIntrinsicHeight());

        // Create Bitmap with background and vector then draw them
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(),
                background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void saveListId(FieldRestaurant restaurant) {
        fieldRestaurantList.add(restaurant);
        Log.i(TAG, "la list = " + fieldRestaurantList);
    }


    private void addMarkers() {
        Marker marker = googleMap.addMarker(new MarkerOptions()
                .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.ic_restaurant))
                .position(restaurantLatLng));
        marker.setTag(fieldRestaurant.id);
    }

    //---------------------------------------------------------------
    // Callback Interface with methods

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Call the method that creating callback after being attached to parent activity
        this.createCallbackToParentActivity();
    }

    private void createCallbackToParentActivity() {
        try {
            // Parent activity will automatically subscribe to callback
            mCallback = (OnListIdListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnListIdListener");
        }
    }

    // Declare interface that will be implemented by any container activity
    public interface OnListIdListener {
        void onListId(ArrayList<FieldRestaurant> fieldRestaurantArrayList);
    }

}
