package com.minh.bloodlife.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.minh.bloodlife.R;
import com.minh.bloodlife.adapter.UserAdapter;
import com.minh.bloodlife.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SiteStatsFragment extends Fragment {
    private static final String TAG = "SiteStatsFragment";

    private String siteId;
    private FirebaseFirestore db;

    private TextView totalVolunteersTextView, totalDonorsTextView;
    private RecyclerView volunteersRecyclerView, donorsRecyclerView;

    public static SiteStatsFragment newInstance(String siteId) {
        SiteStatsFragment fragment = new SiteStatsFragment();
        Bundle args = new Bundle();
        args.putString("siteId", siteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            siteId = getArguments().getString("siteId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_site_stats, container, false);

        totalVolunteersTextView = view.findViewById(R.id.totalVolunteersTextView);
        totalDonorsTextView = view.findViewById(R.id.totalDonorsTextView);
        volunteersRecyclerView = view.findViewById(R.id.volunteersRecyclerView);
        donorsRecyclerView = view.findViewById(R.id.donorsRecyclerView);

        setupRecyclerView(volunteersRecyclerView);
        setupRecyclerView(donorsRecyclerView);

        fetchStatistics();

        return view;
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
    }

    private void fetchStatistics() {
        fetchVolunteers();
        fetchDonors();
    }

    private void fetchVolunteers() {
        db.collection("registrations")
                .whereEqualTo("siteId", siteId) // Match the siteId for this site
                .whereEqualTo("isVolunteer", true) // Only fetch volunteers
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> userIds = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Log.d("SiteStatsFragment", "Registration document: " + doc.getData()); // Log each registration document

                            String userId = doc.getString("userId");
                            if (userId != null) {
                                userIds.add(userId); // Add userId to the list if it exists
                            }
                        }
                        Log.d("SiteStatsFragment", "Fetched User IDs: " + userIds); // Log all fetched userIds

                        // Proceed to fetch user details if we have any userIds
                        if (!userIds.isEmpty()) {
                            fetchVolunteerDetails(userIds);
                        } else {
                            Log.d("SiteStatsFragment", "No volunteers found.");
                            totalVolunteersTextView.setText("Total Volunteers: 0");
                            volunteersRecyclerView.setAdapter(null);
                        }
                    } else {
                        Log.e("SiteStatsFragment", "Error fetching registrations", task.getException());
                        totalVolunteersTextView.setText("Total Volunteers: 0");
                        volunteersRecyclerView.setAdapter(null);
                    }
                });
    }


    private void fetchVolunteerDetails(List<String> userIds) {
        Log.d("SiteStatsFragment", "Fetching user details for User IDs: " + userIds); // Log userIds being fetched
        db.collection("users")
                .whereIn("uid", userIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> volunteers = new ArrayList<>();
                        for (DocumentSnapshot userDoc : task.getResult()) {
                            Log.d("SiteStatsFragment", "User document: " + userDoc.getData()); // Log each fetched user document
                            User user = userDoc.toObject(User.class);
                            volunteers.add(user);
                        }

                        Log.d("SiteStatsFragment", "Fetched users count: " + volunteers.size()); // Log total users fetched
                        totalVolunteersTextView.setText("Total Volunteers: " + volunteers.size());
                        UserAdapter adapter = new UserAdapter(volunteers);
                        volunteersRecyclerView.setAdapter(adapter); // Attach the adapter here
                    } else {
                        Log.e("SiteStatsFragment", "Error fetching user details", task.getException());
                        totalVolunteersTextView.setText("Total Volunteers: 0");
                        volunteersRecyclerView.setAdapter(null);
                    }
                });
    }

    private void fetchDonors() {
        db.collection("registrations")
                .whereEqualTo("siteId", siteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> donors = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            List<Map<String, Object>> registrants = (List<Map<String, Object>>) doc.get("registrants");
                            if (registrants != null) {
                                for (Map<String, Object> registrant : registrants) {
                                    User user = new User();
                                    user.setFirstName((String) registrant.get("firstName"));
                                    user.setLastName((String) registrant.get("lastName"));
                                    user.setEmail((String) registrant.get("email"));
                                    user.setPhoneNumber((String) registrant.get("phone"));
                                    donors.add(user);
                                }
                            }
                        }
                        totalDonorsTextView.setText("Total Donors: " + donors.size());
                        UserAdapter adapter = new UserAdapter(donors);
                        donorsRecyclerView.setAdapter(adapter);
                    } else {
                        Log.e(TAG, "Error fetching donors", task.getException());
                        totalDonorsTextView.setText("Total Donors: 0");
                        donorsRecyclerView.setAdapter(null);
                    }
                });
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
}
