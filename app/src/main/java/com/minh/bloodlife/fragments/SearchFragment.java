package com.minh.bloodlife.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.minh.bloodlife.R;

public class SearchFragment extends Fragment {

    private TextInputEditText searchText;
    private ChipGroup filterChipGroup;
    private RecyclerView searchResultsRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchText = view.findViewById(R.id.searchText);
        filterChipGroup = view.findViewById(R.id.filterChipGroup);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);

        // Add a TextWatcher to the search bar
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("SearchFragment", "Search text changed: " + s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Handle filter chip selections (add logic in the next step)
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            // Handle filter selections
        });

        return view;
    }
}