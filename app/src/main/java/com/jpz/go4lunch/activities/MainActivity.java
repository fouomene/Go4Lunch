package com.jpz.go4lunch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.facebook.login.LoginManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.jpz.go4lunch.fragments.MapFragment;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.fragments.RestaurantListFragment;
import com.jpz.go4lunch.fragments.WorkmatesFragment;
import com.jpz.go4lunch.utils.FirebaseUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // FirebaseUtils class
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    // Declare user
    private FirebaseUser user;

    // For design
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private TextView nameText;
    private TextView emailText;

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

        // Open the view with MapFragment by default
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MapFragment()).commit();

        // Update the user profile in the Nav Drawer
        //updateUserProfile();
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
        nameText = headerView.findViewById(R.id.nav_header_name);
        emailText = headerView.findViewById(R.id.nav_header_email);
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
                        selectedFragment = new MapFragment();
                        break;
                    case R.id.nav_list:
                        selectedFragment = new RestaurantListFragment();
                        break;
                    case R.id.nav_workmates:
                        selectedFragment = new WorkmatesFragment();
                        break;
                    default:
                        selectedFragment = new MapFragment();

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
                // Log out from Firebase & Facebook
                firebaseUtils.userLogout();
                LoginManager.getInstance().logOut();
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
            //Get username & email from Firebase
            username = TextUtils.isEmpty(user.getDisplayName()) ?
                    getString(R.string.info_no_username_found) : user.getDisplayName();
            Log.i("MainActivity", "username = " + user.getDisplayName());

            email = TextUtils.isEmpty(user.getEmail()) ?
                    getString(R.string.info_no_email_found) : user.getEmail();
            Log.i("MainActivity", "email = " + user.getEmail());
        }
        //Update views with data
        nameText.setText(username);
        emailText.setText(email);
    }

    private void startConnectionActivity() {
        Intent intent = new Intent(this, ConnectionActivity.class);
        startActivity(intent);
    }

}
