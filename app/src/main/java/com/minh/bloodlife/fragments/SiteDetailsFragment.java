package com.minh.bloodlife.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;
import com.minh.bloodlife.model.Registration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteDetailsFragment extends Fragment {

    private static final String TAG = "SiteDetailsFragment";
    private static final String ARG_SITE_ID = "siteId";

    private String siteId;
    private TextView siteNameTextView, siteAddressTextView, siteHoursTextView, requiredBloodTypesTextView;
    private View view;

    public static SiteDetailsFragment newInstance(String siteId) {
        SiteDetailsFragment fragment = new SiteDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITE_ID, siteId);
        fragment.setArguments(args);
        Log.d(TAG, "newInstance - siteId: " + siteId); // Add this log
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            siteId = getArguments().getString(ARG_SITE_ID);
            Log.d(TAG, "onCreate - siteId: " + siteId); // Add this log
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_site_details, container, false);

        siteNameTextView = view.findViewById(R.id.siteNameTextView);
        siteAddressTextView = view.findViewById(R.id.siteAddressTextView);
        siteHoursTextView = view.findViewById(R.id.siteHoursTextView);
        requiredBloodTypesTextView = view.findViewById(R.id.requiredBloodTypesTextView);

        // Fetch and display site details
        fetchSiteDetails();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Show back button in the ActionBar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Hide back button in the ActionBar
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        }
    }

    private void fetchSiteDetails() {
        Log.d(TAG, "fetchSiteDetails called for siteId: " + siteId);
        if (view == null) {
            Log.e(TAG, "View is null in fetchSiteDetails");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("donationSites").document(siteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DonationSite site = documentSnapshot.toObject(DonationSite.class);
                        if (site != null) {
                            siteNameTextView.setText(site.getSiteName());
                            siteAddressTextView.setText(site.getAddress());
                            siteHoursTextView.setText(site.getDonationHours());
                            requiredBloodTypesTextView.setText("Required Blood Types: " + formatBloodTypes(site.getRequiredBloodTypes()));
                            // Set onClickListeners for buttons
                            Button registerToDonateButton = view.findViewById(R.id.registerToDonateButton);
                            Button registerAsVolunteerButton = view.findViewById(R.id.registerAsVolunteerButton);
                            Button getDirectionsButton = view.findViewById(R.id.getDirectionsButton);

                            registerToDonateButton.setOnClickListener(v -> {
                                handleRegisterToDonate(site);
                            });

                            registerAsVolunteerButton.setOnClickListener(v -> {
                                handleRegisterAsVolunteer(site);
                            });

                            getDirectionsButton.setOnClickListener(v -> {
                                // Handle "Get Directions" button click
                                getDirectionsToSite(site);
                            });

                        }
                    } else {
                        // Handle the case where the document does not exist
                        Log.e(TAG, "Site not found");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here
                    Log.e(TAG, "Error fetching site details", e);
                });
    }
    private String formatBloodTypes(List<String> bloodTypes) {
        if (bloodTypes == null || bloodTypes.isEmpty()) {
            return "Not specified";
        }
        return String.join(", ", bloodTypes);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleRegisterToDonate(DonationSite site) {
        // Assuming you have a way to get the current user's ID, e.g., from Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        registerForEvent(userId, site.getSiteId(), false, 1); // Assuming 1 person for simplicity
    }

    private void handleRegisterAsVolunteer(DonationSite site) {
        // Assuming you have a way to get the current user's ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        registerForEvent(userId, site.getSiteId(), true, 0); // 0 for numDonors as it's a volunteer
    }

    private void registerForEvent(String userId, String siteId, boolean isVolunteer, int numDonors) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new registration
        Map<String, Object> registration = new HashMap<>();
        registration.put("userId", userId);
        registration.put("siteId", siteId);
        registration.put("registrationDate", new Date()); // Current date
        registration.put("isVolunteer", isVolunteer);
        registration.put("numDonors", numDonors); // This can be adjusted based on your app's requirements

        // Add the registration to Firestore
        db.collection("registrations")
                .add(registration)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Registration added with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    // Optionally navigate the user or update UI
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding registration", e);
                    Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void getDirectionsToSite(DonationSite site) {
        if (site != null && site.getLocation() != null) {
            double latitude = site.getLocation().getLatitude();
            double longitude = site.getLocation().getLongitude();

            // Create a Uri for the Google Maps app
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if the Google Maps app is installed
            if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Toast.makeText(getContext(), "Google Maps app not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Site location is not available.", Toast.LENGTH_SHORT).show();
        }
    }
}