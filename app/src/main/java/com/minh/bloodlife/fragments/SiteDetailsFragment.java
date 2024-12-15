package com.minh.bloodlife.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.minh.bloodlife.R;

public class SiteDetailsFragment extends Fragment {

    private static final String ARG_SITE_ID = "siteId";

    private String siteId;
    private TextView siteNameTextView;
    // Add more TextViews for other site details

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
        View view = inflater.inflate(R.layout.fragment_site_details, container, false);

        siteNameTextView = view.findViewById(R.id.siteNameTextView);
        // Initialize other TextViews

        // Fetch and display site details based on siteId
        // implement this later

        return view;
    }
}