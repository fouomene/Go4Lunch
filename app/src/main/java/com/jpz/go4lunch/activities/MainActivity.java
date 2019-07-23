package com.jpz.go4lunch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jpz.go4lunch.fragments.MapFragment;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.fragments.RestaurantListFragment;
import com.jpz.go4lunch.fragments.WorkmatesFragment;

public class MainActivity extends AppCompatActivity {

    // For design
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        configureToolbar();
        configureDrawerLayout();

        configureBottomView();

        // Open the view with MapFragment by default
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MapFragment()).commit();

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

}
