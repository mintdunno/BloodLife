package com.minh.bloodlife.fragments;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import android.widget.ProgressBar;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";
    private TextInputEditText searchText;
    private ChipGroup filterChipGroup;
    private RecyclerView searchResultsRecyclerView;
    private FirebaseFirestore db;
    private DonationSiteAdapter adapter;
    private List<DonationSite> siteList;
    private TextInputEditText startDateEditText;
    private TextInputEditText endDateEditText;
    private Calendar startCalendar, endCalendar;
    private ProgressBar searchProgressBar;
    private Button resetFiltersButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchText = view.findViewById(R.id.searchText);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        searchProgressBar = view.findViewById(R.id.searchProgressBar);
        resetFiltersButton = view.findViewById(R.id.resetFiltersButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the adapter and set it to the RecyclerView
        siteList = new ArrayList<>();
        adapter = new DonationSiteAdapter(siteList);
        searchResultsRecyclerView.setAdapter(adapter);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        startDateEditText.setOnClickListener(v -> showDatePickerDialog(true));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(false));

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

        // Handle filter chip selections
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            performSearch(searchText.getText().toString().trim());
        });

        resetFiltersButton.setOnClickListener(v -> resetFilters());

        // Load all donation sites initially
        loadAllDonationSites();
        return view;
    }

    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            if (isStartDate) {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel(startDateEditText, startCalendar);
            } else {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateLabel(endDateEditText, endCalendar);
            }
            performSearch(searchText.getText().toString().trim());
        };

        Calendar calendar = isStartDate ? startCalendar : endCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(), dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateLabel(TextInputEditText editText, Calendar calendar) {
        String myFormat = "yyyy-MM-dd"; // Define your format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void loadAllDonationSites() {
        searchProgressBar.setVisibility(View.VISIBLE);
        db.collection("donationSites")
                .get()
                .addOnCompleteListener(task -> {
                    searchProgressBar.setVisibility(View.GONE);
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
        searchProgressBar.setVisibility(View.VISIBLE);

        MapsFragment mapsFragment = (MapsFragment) requireActivity().getSupportFragmentManager().findFragmentByTag("MapsFragment");
        Location userLocation = mapsFragment != null ? mapsFragment.getLastKnownLocation() : null;
        List<String> selectedBloodTypes = getSelectedBloodTypes();
        boolean isNearMeSelected = filterChipGroup.getCheckedChipIds().contains(R.id.chipNearMe);

        // Log filter values for debugging:
        Log.d(TAG, "Selected blood types: " + selectedBloodTypes.toString());
        Log.d(TAG, "Is Near Me selected: " + isNearMeSelected);
        Log.d(TAG, "User location: " + userLocation);
        Log.d(TAG, "Start Date: " + startDateEditText.getText().toString());
        Log.d(TAG, "End Date: " + endDateEditText.getText().toString());

        com.google.firebase.firestore.Query firestoreQuery = db.collection("donationSites");

        // Apply filters based on selected criteria

        // 1. Search Text Filter:
        if (!query.isEmpty()) {
            Log.d(TAG, "Applying search text filter"); // Debug log
            firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("searchableName", query.toLowerCase())
                    .whereLessThanOrEqualTo("searchableName", query.toLowerCase() + "\uf8ff");
        }

        // 2. Blood Type Filter:
        if (!selectedBloodTypes.isEmpty()) {
            Log.d(TAG, "Applying blood type filter"); // Debug log
            firestoreQuery = firestoreQuery.whereIn("requiredBloodTypes", selectedBloodTypes);
        }

        // 3. "Near Me" Filter:
        if (isNearMeSelected && userLocation != null) {
            Log.d(TAG, "Applying 'Near Me' filter"); // Debug log
            GeoPoint geoPoint = new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude());
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

        // 4. Date Range Filter:
        if (!startDateEditText.getText().toString().isEmpty() && !endDateEditText.getText().toString().isEmpty()) {
            Log.d(TAG, "Applying date range filter"); // Debug log
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date startDate = sdf.parse(startDateEditText.getText().toString());
                Date endDate = sdf.parse(endDateEditText.getText().toString());

                firestoreQuery = firestoreQuery
                        .whereGreaterThanOrEqualTo("startDate", sdf.format(startDate))
                        .whereLessThanOrEqualTo("endDate", sdf.format(endDate));
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing date", e);
            }
        }

        // Log the constructed query for debugging
        Log.d(TAG, "Firestore query: " + firestoreQuery.toString());

        // Execute the query
        firestoreQuery.get().addOnCompleteListener(task -> {
            searchProgressBar.setVisibility(View.GONE);
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
                Toast.makeText(getContext(), "Search failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getSelectedBloodTypes() {
        List<String> selectedTypes = new ArrayList<>();
        for (int id : filterChipGroup.getCheckedChipIds()) {
            Chip chip = filterChipGroup.findViewById(id);
            if (!chip.getText().toString().equals("Near Me")) {
                selectedTypes.add(chip.getText().toString());
            }
        }
        return selectedTypes;
    }

    private void resetFilters() {
        searchText.setText(""); // Clear search text

        // Clear date fields
        startDateEditText.setText("");
        endDateEditText.setText("");

        // Reset start and end Calendars
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Uncheck all filter chips
        filterChipGroup.clearCheck(); // This will clear all checked chips

        // Reload all donation sites (or perform a search with empty parameters)
        loadAllDonationSites();
    }
}