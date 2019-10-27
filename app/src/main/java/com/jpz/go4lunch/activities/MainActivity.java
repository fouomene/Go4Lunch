package com.jpz.go4lunch.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.jpz.go4lunch.fragments.RestaurantMapFragment;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.fragments.RestaurantListFragment;
import com.jpz.go4lunch.fragments.WorkmatesFragment;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.CurrentPlace;
import com.jpz.go4lunch.utils.FirebaseUtils;
import com.jpz.go4lunch.utils.MyUtilsNavigation;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static com.jpz.go4lunch.api.WorkmateHelper.getCurrentWorkmate;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        EasyPermissions.PermissionCallbacks, RestaurantMapFragment.DeviceLocationListener {

    // Static data for ACCESS_FINE_LOCATION
    public static final String PERMS = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int RC_LOCATION = 123;

    // Utils
    private MyUtilsNavigation myUtilsNavigation = new MyUtilsNavigation();
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    // Declare user
    private FirebaseUser currentUser;

    // Models and Utils
    private Workmate currentWorkmate = new Workmate();

    // For design
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;
    private TextView nameProfile;
    private TextView emailProfile;
    private ImageView photoProfile;

    // For toolbar
    private CardView cardView;
    private EditText editText;
    private ActionBarDrawerToggle toggle;

    // User profile
    private String username;
    private String email;

    // DeviceLatLng data for the list of restaurant
    private Fragment restaurantListFragment = new RestaurantListFragment();
    public static final String LAT_LNG_BUNDLE_KEY = "lat_lng_bundle_key";

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Translucent Status Bar when open the Navigation Drawer
        Window window = getWindow();
        // Enable status bar translucency (requires API 19)
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // Disable status bar translucency (requires API 19)
        window.getAttributes().flags &= (~WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // Set a color (requires API 21)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.setStatusBarColor(Color.TRANSPARENT);

        // Widgets for the toolbar
        cardView = findViewById(R.id.toolbar_card_view);
        editText = findViewById(R.id.toolbar_edit_text);

        bottomNav = findViewById(R.id.bottom_navigation);

        // Initialize FireBase User
        currentUser = firebaseUtils.getCurrentUser();

        // Display views and layouts
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        configureBottomView();

        // Request permission when starting MainActivity which contains Google Maps services
        EasyPermissions.requestPermissions(this,
                getString(R.string.rationale_permission_location_access), RC_LOCATION, PERMS);

        // Open the view with RestaurantMapFragment if permissions were already allowed
        if (EasyPermissions.hasPermissions(this, PERMS))
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RestaurantMapFragment()).commit();
    }

//----------------------------------------------------------------------------------
    // Methods for Menu in Toolbar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu and add it to the Toolbar
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action on menu items
        if (item.getItemId() == R.id.menu_toolbar_search) {
            if (getActionBar() != null) {
                getActionBar().setDisplayShowTitleEnabled(false);
            }
            // Set the search icon item
            item.setVisible(false);
            // Set toggle and cardView
            toggle.setDrawerIndicatorEnabled(false);
            cardView.setVisibility(View.VISIBLE);

            Toast.makeText(this, "click on search in the map", Toast.LENGTH_SHORT).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //----------------------------------------------------------------------------------
    // Private methods to configure design

    private void configureToolbar() {
        // Get the toolbar view inside the activity layout
        toolbar = findViewById(R.id.toolbar);
        // Set the Toolbar
        setSupportActionBar(toolbar);
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(true);
        }
        //setTitle(getString(R.string.hungry));
    }

    private void configureDrawerLayout() {
        drawerLayout = findViewById(R.id.drawer_layout);
        // "Hamburger icon"
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void configureNavigationView() {
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

    private void configureBottomView() {
        bottomNav.setOnNavigationItemSelectedListener((@NonNull MenuItem menuItem) -> {
            Fragment selectedFragment = new Fragment();
            // Check the fragment selected
            switch (menuItem.getItemId()) {
                case R.id.nav_map:
                    selectedFragment = new RestaurantMapFragment();
                    break;
                case R.id.nav_list:
                    selectedFragment = restaurantListFragment;
                    break;
                case R.id.nav_workmates:
                    selectedFragment = new WorkmatesFragment();
                    break;
            }
            // Add it to FrameLayout fragment_container
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
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
        switch (item.getItemId()) {
            case R.id.nav_drawer_lunch:
                // Get the restaurant choice from Firebase
                getCurrentWorkmate(currentUser.getUid())
                        .addOnSuccessListener(documentSnapshot -> {
                            currentWorkmate = documentSnapshot.toObject(Workmate.class);
                            if (currentWorkmate != null) {
                                // Start DetailRestaurantActivity with the restaurant identifier
                                if (currentWorkmate.getRestaurantId() != null) {
                                    myUtilsNavigation.startDetailsRestaurantActivity(this,
                                            currentWorkmate.getRestaurantId());
                                    // If there is no restaurant choice, display a message
                                } else {
                                    Toast.makeText(MainActivity.this,
                                            getString(R.string.you_have_not_chosen), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                break;

            case R.id.nav_drawer_settings:
                // settings
                Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_drawer_logout:
                // Check if the user is logged in with Facebook...
                for (UserInfo user : currentUser.getProviderData()) {
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
        if (currentUser != null) {
            //Get picture URL from Firebase
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(photoProfile);
            }
            //Get username & email from Firebase
            username = TextUtils.isEmpty(currentUser.getDisplayName()) ?
                    getString(R.string.info_no_username_found) : currentUser.getDisplayName();
            Log.i("MainActivity", "username = " + currentUser.getDisplayName());
            email = TextUtils.isEmpty(currentUser.getEmail()) ?
                    getString(R.string.info_no_email_found) : currentUser.getEmail();
            Log.i("MainActivity", "email = " + currentUser.getEmail());
        }
        //Update views with data
        nameProfile.setText(username);
        emailProfile.setText(email);
    }

    //----------------------------------------------------------------------------------

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
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        // If there isn't permission, wait for the user to allow permissions before starting...
        // ...RestaurantMapFragment and geolocate it at the opening with the method in the fragment.
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new RestaurantMapFragment()).commit();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    //----------------------------------------------------------------------------------

    // Use Easy Permissions onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If there isn't permission, wait for the user to allow permissions before starting...
        // ...RestaurantMapFragment and geolocate it at the opening with the method in the fragment.
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new RestaurantMapFragment()).commit();
        }
    }

    //----------------------------------------------------------------------------------

    @Override
    public void onDeviceLocationFetch(LatLng latLng) {
        // Transfer deviceLatLng value in RestaurantListFragment
        Bundle bundle = new Bundle();
        bundle.putParcelable(LAT_LNG_BUNDLE_KEY, latLng);
        restaurantListFragment.setArguments(bundle);
        Log.i(TAG, "latlng = " + latLng);

        // Transfer deviceLatLng value when use autoComplete method
        ImageView iconSearch = findViewById(R.id.toolbar_ic_search);
        iconSearch.setOnClickListener(v ->
                CurrentPlace.getInstance(this).autoComplete(editText.getText().toString(), latLng)
        );
    }

}
