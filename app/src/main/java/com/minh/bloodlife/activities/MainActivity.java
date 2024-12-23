package com.minh.bloodlife.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.fragments.CreateSiteFragment;
import com.minh.bloodlife.fragments.MapsFragment;
import com.minh.bloodlife.fragments.ProfileFragment;
import com.minh.bloodlife.fragments.ReportsFragment;
import com.minh.bloodlife.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private MenuItem reportsMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        reportsMenuItem = bottomNavigationView.getMenu().findItem(R.id.menu_reports);
        setupBottomNavigationView();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new MapsFragment())
                    .commit();
        }

        centerActionBarTitle();
    }

    private void setupBottomNavigationView() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment;

            if (item.getItemId() == R.id.menu_map) {
                selectedFragment = new MapsFragment();
            } else if (item.getItemId() == R.id.menu_search) {
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.menu_profile) {
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.menu_create_site) {
                selectedFragment = new CreateSiteFragment();
            } else if (item.getItemId() == R.id.menu_reports) {
                selectedFragment = new ReportsFragment();
            }
            else {
                return false;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commit();

            return true;
        });
    }

    private void centerActionBarTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            View customView = getLayoutInflater().inflate(R.layout.action_bar_title, null);
            TextView title = customView.findViewById(R.id.action_bar_title);
            title.setText("BloodLife");
            ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
            );
            actionBar.setCustomView(customView, layoutParams);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
        } else {
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
                            updateNavigationBar(null);
                        }
                    } else {
                        Log.e(TAG, "Error fetching user data", task.getException());
                        updateNavigationBar(null);
                    }
                });
    }

    private void updateNavigationBar(String userType) {
        Menu menu = bottomNavigationView.getMenu();
        MenuItem createSiteItem = menu.findItem(R.id.menu_create_site);
        if (createSiteItem != null) {
            createSiteItem.setVisible("Site Manager".equals(userType));
        }
    }
    private void checkIfSuperUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> claims = task.getResult().getClaims();
                            if (reportsMenuItem != null) {
                                if (claims.containsKey("isSuperUser") && (boolean) claims.get("isSuperUser")) {
                                    // User is a Super User, show the reports menu item
                                    reportsMenuItem.setVisible(true);
                                } else {
                                    // User is not a Super User, hide the reports menu item
                                    reportsMenuItem.setVisible(false);
                                }
                            }
                        } else {
                            // Handle error
                            Log.e("MainActivity", "Error getting auth claims", task.getException());
                        }
                    });
        } else {
            // User not signed in
            if (reportsMenuItem != null) {
                reportsMenuItem.setVisible(false); // Hide the menu item
            }
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Find the menu item for Reports (if it exists) after inflating the menu
        reportsMenuItem = menu.findItem(R.id.menu_reports);

        // Call checkIfSuperUser to set the initial visibility based on user's role
        checkIfSuperUser();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
            return true;
        } else if (item.getItemId() == R.id.menu_logout) {
            signOut();
            return true;
        } else if (item.getItemId() == R.id.menu_reports) {
            ReportsFragment reportsFragment = new ReportsFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, reportsFragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void signOut() {
        mAuth.signOut();
        redirectToLogin();
    }
}