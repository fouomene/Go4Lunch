package com.jpz.go4lunch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.jpz.go4lunch.fragments.RestaurantMapFragment;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.fragments.RestaurantListFragment;
import com.jpz.go4lunch.fragments.WorkmatesFragment;
import com.jpz.go4lunch.utils.FirebaseUtils;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Static data for ACCESS_FINE_LOCATION
    public static final String PERMS = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int RC_LOCATION = 123;

    // FirebaseUtils class
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    // Declare user
    private FirebaseUser user;

    // For design
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private TextView nameProfile;
    private TextView emailProfile;
    private ImageView photoProfile;

    // User profile
    private String username;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Initialize FireBase User
        user = firebaseUtils.getCurrentUser();

        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        configureBottomView();

        // Request permission when starting MainActivity which contains Google Maps services
        EasyPermissions.requestPermissions(this,
                getString(R.string.permission_location_access), RC_LOCATION, PERMS);

        // Open the view with RestaurantMapFragment if permissions were already allowed
        if (EasyPermissions.hasPermissions(this, PERMS))
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RestaurantMapFragment()).commit();
    }

    //----------------------------------------------------------------------------------
    // Private methods to configure design

    private void configureToolbar(){
        // Get the toolbar view inside the activity layout
        toolbar = findViewById(R.id.toolbar);
        // Set the Toolbar
        setSupportActionBar(toolbar);
    }

    private void configureDrawerLayout(){
        drawerLayout = findViewById(R.id.drawer_layout);
        // "Hamburger icon"
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView(){
        NavigationView navigationView = findViewById(R.id.nav_drawer_view);
        // For Menu Item
        navigationView.setNavigationItemSelectedListener(this);
        // For Nav Header
        View headerView = navigationView.getHeaderView(0);
        nameProfile = headerView.findViewById(R.id.nav_header_name);
        emailProfile = headerView.findViewById(R.id.nav_header_email);
        photoProfile = headerView.findViewById(R.id.nav_header_photo);
        updateUserProfile();
    }

    private void configureBottomView(){
        bottomNav.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment;

                // Check the fragment selected
                switch (menuItem.getItemId()) {
                    case R.id.nav_map:
                        selectedFragment = new RestaurantMapFragment();
                        break;
                    case R.id.nav_list:
                        selectedFragment = new RestaurantListFragment();
                        break;
                    case R.id.nav_workmates:
                        selectedFragment = new WorkmatesFragment();
                        break;
                    default:
                        selectedFragment = new RestaurantMapFragment();

                }
                // Add it to FrameLayout fragment_container
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
                return true;
            }
        });
    }

    //----------------------------------------------------------------------------------
    // Methods for NavigationView in NavigationDrawer

    @Override
    public void onBackPressed() {
        // Handle back click to close menu
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle Navigation Item Click
        switch (item.getItemId()){
            case R.id.nav_drawer_lunch:
                // start DetailRestaurantActivity
                Toast.makeText(MainActivity.this, "DetailRestaurant", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_drawer_settings:
                // settings
                Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_drawer_logout:
                // Check if the user is logged in with Facebook...
                for (UserInfo user: user.getProviderData()) {
                    if (user.getProviderId().equals("facebook.com")) {
                        Log.i("Tag", "provider in loop " + user.getProviderId());
                        // ... then, in this case, logout from Facebook
                        LoginManager.getInstance().logOut();
                    }
                }
                // Log out from Firebase
                firebaseUtils.userLogout();
                startConnectionActivity();
                finish();
                break;
            default:
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    //----------------------------------------------------------------------------------

    // Update the user profile in the Nav Drawer Header
    private void updateUserProfile() {
        if (user != null) {

            //Get picture URL from Firebase
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(photoProfile);
            }

            //Get username & email from Firebase
            username = TextUtils.isEmpty(user.getDisplayName()) ?
                    getString(R.string.info_no_username_found) : user.getDisplayName();
            Log.i("MainActivity", "username = " + user.getDisplayName());

            email = TextUtils.isEmpty(user.getEmail()) ?
                    getString(R.string.info_no_email_found) : user.getEmail();
            Log.i("MainActivity", "email = " + user.getEmail());
        }
        //Update views with data
        nameProfile.setText(username);
        emailProfile.setText(email);
    }

    private void startConnectionActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
    }

    //----------------------------------------------------------------------------------
    // Easy Permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);

        // If there isn't permission, wait for the user to allow permissions before starting...
        // ...RestaurantMapFragment and geolocate it at the opening with the method in the fragment
        if (EasyPermissions.hasPermissions(this, PERMS))
            // Open the view with RestaurantMapFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RestaurantMapFragment()).commit();
    }
}
