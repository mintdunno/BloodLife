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
import com.minh.bloodlife.R;
import com.minh.bloodlife.fragments.MapsFragment;
import com.minh.bloodlife.fragments.ProfileFragment;
import com.minh.bloodlife.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

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
            }
            // TODO: Add conditional logic for other menu items based on user type

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Set the default fragment to load when the activity starts
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new MapsFragment())
                    .commit();
        }
        // Set the custom ActionBar title view
        centerActionBarTitle();
    }

    private void centerActionBarTitle() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
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
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // No user is signed in, redirect to LoginActivity
            redirectToLogin();
        } else {
            // User is signed in, check custom claims
            currentUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                Log.d(TAG, "User token: " + idToken);
                                // Send token to your backend via HTTPS
                                // ...
                                String userType = (String) task.getResult().getClaims().get("userType");
                                if (userType != null) {
                                    Log.d(TAG, "User type: " + userType);
                                    // Update UI based on user type
                                    updateNavigationBar(userType);
                                } else {
                                    Log.e(TAG, "User type is null");
                                    // Handle null user type, perhaps redirect to login or show an error
                                }
                            } else {
                                // Handle error
                                Log.e(TAG, "Error getting ID token", task.getException());
                            }
                        }
                    });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_logout) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        mAuth.signOut();
        // Redirect to LoginActivity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    private void updateNavigationBar(String userType) {
        Menu menu = bottomNavigationView.getMenu();
        // Example: Only show 'Create Site' if the user is a Site Manager
//        MenuItem createSiteItem = menu.findItem(R.id.menu_create_site);
//        if (createSiteItem != null) {
//            createSiteItem.setVisible("siteManager".equals(userType));
//        }
    }

    public MapsFragment getMapsFragment() {
        Fragment mapsFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mapsFragment instanceof MapsFragment) {
            return (MapsFragment) mapsFragment;
        } else {
            return null;
        }
    }
}