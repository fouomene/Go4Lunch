package com.jpz.go4lunch.fragments;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.activities.MainActivity;


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


    // Static data for ACCESS_FINE_LOCATION
    private static final int MY_LOCATION_REQUEST_CODE = 100;

    // Request code for location permission request
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private FusedLocationProviderClient fusedLocationProviderClient;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 16;

    private static final String TAG = MainActivity.class.getSimpleName();

    public RestaurantMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        // Construct a FusedLocationProviderClient
        if(getActivity() != null)
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Declare FloatingActionButton and its behavior
        FloatingActionButton floatingActionButton = view.findViewById(R.id.fragment_restaurant_map_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current location of the device and set the position of the map
                getDeviceLocation();
            }
        });

        return view;
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Prevent the My Location button from appearing by calling
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (getActivity() != null)
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationPermissionGranted = true;
            else
                requestLocationPermission();

        // Turn on the My Location layer and the related control on the map
        updateLocationUI();

        // Get the current location of the device and set the position of the map
        //getDeviceLocation();
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

    /*
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
    /*
        if (getActivity() != null)
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                locationPermissionGranted = true;
            else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }
    */


    private void requestLocationPermission() {
        if (getActivity() != null)
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Permission needed")
                        .setMessage(getString(R.string.popup_title_permission_files_access))
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (getActivity() != null)
                                    ActivityCompat.requestPermissions(getActivity(),
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            MY_LOCATION_REQUEST_CODE);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_LOCATION_REQUEST_CODE);
            }
    }

/*
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);
            } else {
                // Permission was denied. Display an error message.
                Toast.makeText(getActivity(), getString(R.string.popup_title_permission_files_access), Toast.LENGTH_SHORT).show();
            }
        }
    }
*/

    //----------------------------------------------------------------------------------

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (getActivity() != null)
            try {
                if (locationPermissionGranted) {
                    Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                    locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                // Set the map's camera position to the current location of the device.
                                lastKnownLocation = task.getResult();

                                final LatLng actualLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                                cameraPosition = new CameraPosition.Builder()
                                        .target(actualLocation)      // Sets the center of the map to the actual position
                                        .zoom(DEFAULT_ZOOM)          // Sets the zoom
                                        .build();                   // Creates a CameraPosition from the builder
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                /*
                                if (googleMap != null)
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                */
                            } else {
                                Log.i(TAG, "Current location is null. Using defaults.");
                                Log.e(TAG, "Exception: %s", task.getException());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        }
                    });
                }
            } catch(SecurityException e)  {
                Log.e("Exception: %s", e.getMessage());
            }
        }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                getDeviceLocation();
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap = null;
                requestLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}
