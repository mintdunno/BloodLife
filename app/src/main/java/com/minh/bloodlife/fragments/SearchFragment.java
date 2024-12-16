package com.minh.bloodlife.fragments;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.activities.MainActivity;
import com.minh.bloodlife.adapter.DonationSiteAdapter;
import com.minh.bloodlife.model.DonationSite;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";

    private TextInputEditText searchText;
    private ChipGroup filterChipGroup;
    private RecyclerView searchResultsRecyclerView;
    private FirebaseFirestore db;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchText = view.findViewById(R.id.searchText);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the adapter and set it to the RecyclerView
        siteList = new ArrayList<>();
        adapter = new DonationSiteAdapter(siteList);
        searchResultsRecyclerView.setAdapter(adapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up item click listener for the RecyclerView
        adapter.setOnItemClickListener(new DonationSiteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DonationSite site) {
                // Open SiteDetailsFragment and pass the siteId
                SiteDetailsFragment siteDetailsFragment = SiteDetailsFragment.newInstance(site.getSiteId());
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragmentContainer, siteDetailsFragment);
                transaction.addToBackStack(null); // Optional: Add to back stack for navigation
                transaction.commit();
            }
        });

        // Add a TextWatcher to the search bar
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString().trim();
                performSearch(searchQuery);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Handle filter chip selections (add logic in the next step)
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            performSearch(searchText.getText().toString().trim());
        });

        // Load all donation sites initially
        loadAllDonationSites();

        return view;
    }

    private void loadAllDonationSites() {
        db.collection("donationSites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        siteList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DonationSite site = document.toObject(DonationSite.class);
                            site.setSiteId(document.getId());
                            siteList.add(site);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                        Toast.makeText(getContext(), "Failed to load donation sites", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performSearch(String query) {
        MapsFragment mapsFragment = ((MainActivity) getActivity()).getMapsFragment();
        Location userLocation = mapsFragment != null ? mapsFragment.getLastKnownLocation() : null;

        List<String> selectedBloodTypes = getSelectedBloodTypes();
        boolean isNearMeSelected = filterChipGroup.getCheckedChipIds().contains(R.id.chipNearMe);

        com.google.firebase.firestore.Query firestoreQuery = db.collection("donationSites");

        // Apply the site name filter if the query is not empty
        if (!query.isEmpty()) {
            firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("searchableName", query.toLowerCase())
                    .whereLessThanOrEqualTo("searchableName", query.toLowerCase() + "\uf8ff");
        }

        // Apply blood type filter if there are selected blood types
        if (!selectedBloodTypes.isEmpty()) {
            firestoreQuery = firestoreQuery.whereArrayContainsAny("requiredBloodTypes", selectedBloodTypes);
        }

        // Apply "Near Me" filter if selected and user location is available
        if (isNearMeSelected && userLocation != null) {
            // Convert user's location to GeoPoint
            GeoPoint geoPoint = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());

            // Bounding box calculation (approximation for simplicity)
            double radius = 20; // Radius in kilometers
            double lat = geoPoint.getLatitude();
            double lon = geoPoint.getLongitude();

            double latOffset = radius / 111.12; // Approximate km to degrees latitude
            double lonOffset = radius / (111.12 * Math.cos(Math.toRadians(lat)));

            GeoPoint southWest = new GeoPoint(lat - latOffset, lon - lonOffset);
            GeoPoint northEast = new GeoPoint(lat + latOffset, lon + lonOffset);

            firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("location", southWest)
                    .whereLessThanOrEqualTo("location", northEast);
        }

        // Execute the query
        firestoreQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                siteList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    DonationSite site = document.toObject(DonationSite.class);
                    site.setSiteId(document.getId());
                    siteList.add(site);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "Error getting documents.", task.getException());
                Toast.makeText(getContext(), "Search failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getSelectedBloodTypes() {
        List<String> selectedTypes = new ArrayList<>();
        for (int id : filterChipGroup.getCheckedChipIds()) {
            Chip chip = filterChipGroup.findViewById(id);
            selectedTypes.add(chip.getText().toString());
        }
        return selectedTypes;
    }
}