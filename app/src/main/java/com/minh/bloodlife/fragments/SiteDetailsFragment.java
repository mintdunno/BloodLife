package com.minh.bloodlife.fragments;

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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;

import java.util.List;

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
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            siteId = getArguments().getString(ARG_SITE_ID);
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
                                // Handle "Register to Donate" button click
                                Toast.makeText(getContext(), "Register to Donate clicked for site: " + site.getSiteName(), Toast.LENGTH_SHORT).show();
                                // Here you can open a new Fragment or Activity to handle the donation registration process
                            });

                            registerAsVolunteerButton.setOnClickListener(v -> {
                                // Handle "Register as Volunteer" button click
                                Toast.makeText(getContext(), "Register as Volunteer clicked for site: " + site.getSiteName(), Toast.LENGTH_SHORT).show();
                                // Similar to above, handle the volunteer registration process
                            });

                            getDirectionsButton.setOnClickListener(v -> {
                                // Handle "Get Directions" button click
                                Toast.makeText(getContext(), "Get Directions clicked for site: " + site.getSiteName(), Toast.LENGTH_SHORT).show();
                                // Implement the logic to show directions to the donation site,
                                // for example, by opening the Google Maps app with directions.
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
}