package com.minh.bloodlife.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.adapter.DonationSiteAdapter;
import com.minh.bloodlife.model.DonationSite;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

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

        return view;
    }

    private void performSearch(String query) {
        List<String> selectedBloodTypes = getSelectedBloodTypes();

        // Create a Firestore query reference
        com.google.firebase.firestore.Query queryRef = db.collection("donationSites");

        // If there's a search query, filter by site name
        if (!query.isEmpty()) {
            // Convert query to lowercase for case-insensitive matching
            String queryLower = query.toLowerCase();

            // Use whereArrayContains for partial matching (if needed)
            queryRef = queryRef.whereGreaterThanOrEqualTo("searchableName", queryLower)
                    .whereLessThanOrEqualTo("searchableName", queryLower + "\uf8ff");
        }

        // If there are selected blood types, filter by them
        if (!selectedBloodTypes.isEmpty()) {
            queryRef = queryRef.whereIn("requiredBloodTypes", selectedBloodTypes);
        }

        // Execute the query
        queryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                siteList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    DonationSite site = document.toObject(DonationSite.class);
                    siteList.add(site);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w("SearchFragment", "Error getting documents.", task.getException());
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