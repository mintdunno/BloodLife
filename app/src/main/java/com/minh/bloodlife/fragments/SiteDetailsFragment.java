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
import java.util.stream.Collectors;

public class SiteDetailsFragment extends Fragment {

    private static final String TAG = "SiteDetailsFragment";
    private static final String ARG_SITE_ID = "siteId";
    private String siteId;
    private TextView siteNameTextView, siteAddressTextView, siteHoursTextView, requiredBloodTypesTextView, siteDaysTextView;
    private View view;

    public static SiteDetailsFragment newInstance(String siteId) {
        SiteDetailsFragment fragment = new SiteDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITE_ID, siteId);
        fragment.setArguments(args);
        Log.d(TAG, "newInstance - siteId: " + siteId);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            siteId = getArguments().getString(ARG_SITE_ID);
            Log.d(TAG, "onCreate - siteId: " + siteId);
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
        siteDaysTextView = view.findViewById(R.id.siteDaysTextView); // Initialize the new TextView

        // Fetch and display site details
        fetchSiteDetails();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                            // Format and display the time range
                            String timeRange = String.format("From %s to %s", site.getDonationStartTime(), site.getDonationEndTime());
                            siteHoursTextView.setText(timeRange);

                            requiredBloodTypesTextView.setText("Required Blood Types: " + formatBloodTypes(site.getRequiredBloodTypes()));

                            // Format and display the days of the week
                            if (site.getDonationDays() != null && !site.getDonationDays().isEmpty()) {
                                String formattedDays = site.getDonationDays().stream()
                                        .map(day -> day.substring(0, 3)) // Take the first 3 letters of each day
                                        .collect(Collectors.joining(", ")); // Join with a comma and space
                                siteDaysTextView.setText(formattedDays);
                            } else {
                                siteDaysTextView.setText("Days: Not specified");
                            }

                            // Set onClickListeners for buttons
                            Button registerToDonateButton = view.findViewById(R.id.registerToDonateButton);
                            Button registerAsVolunteerButton = view.findViewById(R.id.registerAsVolunteerButton);
                            Button getDirectionsButton = view.findViewById(R.id.getDirectionsButton);

                            registerToDonateButton.setOnClickListener(v -> handleRegisterToDonate(site));
                            registerAsVolunteerButton.setOnClickListener(v -> handleRegisterAsVolunteer(site));
                            getDirectionsButton.setOnClickListener(v -> getDirectionsToSite(site));
                        }
                    } else {
                        Log.e(TAG, "Site not found");
                    }
                })
                .addOnFailureListener(e -> {
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        registerForEvent(userId, site.getSiteId(), false, 1);
    }

    private void handleRegisterAsVolunteer(DonationSite site) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        registerForEvent(userId, site.getSiteId(), true, 0);
    }

    private void registerForEvent(String userId, String siteId, boolean isVolunteer, int numDonors) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> registration = new HashMap<>();
        registration.put("userId", userId);
        registration.put("siteId", siteId);
        registration.put("registrationDate", new Date());
        registration.put("isVolunteer", isVolunteer);
        registration.put("numDonors", numDonors);

        db.collection("registrations")
                .add(registration)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Registration added with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
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
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
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