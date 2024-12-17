package com.minh.bloodlife.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.fragments.CreateSiteFragment;
import com.minh.bloodlife.fragments.MapsFragment;
import com.minh.bloodlife.fragments.ProfileFragment;
import com.minh.bloodlife.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.menu_map) {
                selectedFragment = new MapsFragment();
            } else if (itemId == R.id.menu_search) {
                selectedFragment = new SearchFragment();
            } else if (itemId == R.id.menu_profile) {
                selectedFragment = new ProfileFragment();
            } else if (itemId == R.id.menu_create_site) {
                selectedFragment = new CreateSiteFragment();
            }
            // ... other menu items

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }
            return true;
        });

        // Set the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new MapsFragment())
                    .commit();
        }

        centerActionBarTitle();
    }

    private void centerActionBarTitle() {
        getSupportActionBar().setDisplayOptions(androidx.appcompat.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        View customView = getLayoutInflater().inflate(R.layout.action_bar_title, null);

        // Set the title text if you want to change it dynamically
        TextView title = customView.findViewById(R.id.action_bar_title);
        title.setText("BloodLife");

        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );

        getSupportActionBar().setCustomView(customView, layoutParams);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
            // Fetch user role and update the menu
            fetchUserRoleAndUpdateMenu(currentUser.getUid());
        }
    }

    private void fetchUserRoleAndUpdateMenu(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String userType = document.getString("userType");
                            Log.d(TAG, "User type: " + userType);
                            updateNavigationBar(userType);
                        } else {
                            Log.d(TAG, "User document not found");
                            // Handle case where user document doesn't exist (maybe redirect to login or show an error)
                            updateNavigationBar(null); // Set a default menu
                        }
                    } else {
                        Log.e(TAG, "Error fetching user data", task.getException());
                        // Handle error fetching user data
                        updateNavigationBar(null); // Set a default menu
                    }
                });
    }

    private void updateNavigationBar(String userType) {
        Menu menu = bottomNavigationView.getMenu();
        MenuItem createSiteItem = menu.findItem(R.id.menu_create_site);

        if (createSiteItem != null) {
            if ("Site Manager".equals(userType)) {
                createSiteItem.setVisible(true);
            } else {
                createSiteItem.setVisible(false);
            }
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Finish MainActivity
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.menu_logout) {
//            signOut();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void signOut() {
        mAuth.signOut();
        // Redirect to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity
    }
    public MapsFragment getMapsFragment() {
        Fragment mapsFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mapsFragment instanceof MapsFragment) {
            return (MapsFragment) mapsFragment;
        } else {
            return null;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the back button behavior
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish(); // Or some other behavior to exit the app
            }
            return true;
        }
        // Handle the logout option
        else if (item.getItemId() == R.id.menu_logout) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}