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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.adapter.UserAdapter;
import com.minh.bloodlife.model.DonationSite;
import com.minh.bloodlife.model.Registration;
import com.minh.bloodlife.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SiteStatsFragment extends Fragment {

    private static final String TAG = "SiteStatsFragment";
    private static final String ARG_SITE_ID = "siteId";
    private String siteId;

    private FirebaseFirestore db;

    private TextView totalVolunteersTextView;
    private TextView totalDonorsTextView;
    private RecyclerView volunteersRecyclerView;
    private RecyclerView donorsRecyclerView;
    private UserAdapter volunteersAdapter;
    private UserAdapter donorsAdapter;

    public static SiteStatsFragment newInstance(String siteId) {
        SiteStatsFragment fragment = new SiteStatsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITE_ID, siteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            siteId = getArguments().getString(ARG_SITE_ID);
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

        volunteersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        donorsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchSiteStatistics();

        return view;
    }

    private void fetchSiteStatistics() {
        // Fetch and display total number of volunteers
        db.collection("registrations")
                .whereEqualTo("siteId", siteId)
                .whereEqualTo("isVolunteer", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        totalVolunteersTextView.setText("Total Volunteers: " + count);
                    } else {
                        Log.e(TAG, "Error fetching volunteer count", task.getException());
                    }
                });

        // Fetch and display total number of donors
        db.collection("registrations")
                .whereEqualTo("siteId", siteId)
                .whereEqualTo("isVolunteer", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        totalDonorsTextView.setText("Total Donors: " + count);
                    } else {
                        Log.e(TAG, "Error fetching donor count", task.getException());
                    }
                });

        // Fetch and display the list of volunteers
        fetchUserList(true, volunteersRecyclerView);

        // Fetch and display the list of donors
        fetchUserList(false, donorsRecyclerView);
    }

    private void fetchUserList(boolean isVolunteer, RecyclerView recyclerView) {
        db.collection("registrations")
                .whereEqualTo("siteId", siteId)
                .whereEqualTo("isVolunteer", isVolunteer)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> userIds = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            userIds.add(document.getString("userId"));
                        }
                        if (!userIds.isEmpty()) {
                            fetchUserDetails(userIds, recyclerView);
                        }
                    } else {
                        Log.e(TAG, "Error fetching user list", task.getException());
                    }
                });
    }

    private void fetchUserDetails(List<String> userIds, RecyclerView recyclerView) {
        List<User> users = new ArrayList<>();
        db.collection("users").whereIn("uid", userIds)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            users.add(user);
                        }
                        UserAdapter userAdapter = new UserAdapter(users);
                        recyclerView.setAdapter(userAdapter);
                    } else {
                        Log.e(TAG, "Error fetching user details", task.getException());
                    }
                });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to the previous fragment (SiteDetailsFragment)
            getParentFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}