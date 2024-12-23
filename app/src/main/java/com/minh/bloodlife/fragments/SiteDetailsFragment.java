package com.minh.bloodlife.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;
import com.minh.bloodlife.model.Registration;
import com.minh.bloodlife.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class SiteDetailsFragment extends Fragment {
    private static final String TAG = "SiteDetailsFragment";
    private static final String ARG_SITE_ID = "siteId";
    private String siteId;
    private TextView siteNameTextView, siteAddressTextView, siteHoursTextView, requiredBloodTypesTextView, siteDaysTextView, siteStartEndDateTextView, contactPhoneTextView, contactEmailTextView, descriptionTextView, statusTextView;
    private LinearLayout buttonLayout;
    private View view;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

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
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
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
        siteStartEndDateTextView = view.findViewById(R.id.siteStartEndDateTextView);
        siteHoursTextView = view.findViewById(R.id.siteHoursTextView);
        requiredBloodTypesTextView = view.findViewById(R.id.requiredBloodTypesTextView);
        siteDaysTextView = view.findViewById(R.id.siteDaysTextView);
        buttonLayout = view.findViewById(R.id.buttonLayout);
        contactPhoneTextView = view.findViewById(R.id.contactPhoneTextView);
        contactEmailTextView = view.findViewById(R.id.contactEmailTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        statusTextView = view.findViewById(R.id.statusTextView);

        // Fetch site details first to check if the user is a manager of this site
        fetchSiteDetails();

        return view;
    }

    private void checkUserRoleAndAddButtons(String managerId) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot userSnapshot = task.getResult();
                    User user = userSnapshot.toObject(User.class);
                    if (user != null) {
                        if ("Site Manager".equals(user.getUserType())) {
                            // Check if the current user is the manager of this site
                            if (managerId.equals(userId)) {
                                addSiteManagerButtons();
                                addEditAndDeleteButtons();
                            } else {
                                // Allow any site manager to register as a volunteer
                                addRegisterAsVolunteerButton();
                            }
                        } else {
                            // User is a donor
                            addDonorButtons();
                        }
                    }
                } else {
                    Log.e(TAG, "Error fetching user role", task.getException());
                }
            });
        }
    }

    private void addSiteManagerButtons() {
        Button registerAsVolunteerButton = createStyledButton("Register as Volunteer");
        registerAsVolunteerButton.setOnClickListener(v -> {
            handleButtonAnimation(registerAsVolunteerButton);
            checkIfAlreadyVolunteering(currentUser.getUid());
        });

        Button getDirectionsButton = createStyledButton("Get Directions");
        getDirectionsButton.setOnClickListener(v -> {
            handleButtonAnimation(getDirectionsButton);
            getDirectionsToSite();
        });

        Button viewStatisticsButton = createStyledButton("View Statistics");
        viewStatisticsButton.setOnClickListener(v -> {
            handleButtonAnimation(viewStatisticsButton);
            openSiteStatsFragment();
        });

        buttonLayout.addView(registerAsVolunteerButton);
        buttonLayout.addView(getDirectionsButton);
        buttonLayout.addView(viewStatisticsButton);

        checkIfUserIsRegistered();
    }

    private void openSiteStatsFragment() {
        SiteStatsFragment siteStatsFragment = SiteStatsFragment.newInstance(siteId);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, siteStatsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void addEditAndDeleteButtons() {
        Button editSiteButton = createStyledButton("Edit Site");
        editSiteButton.setOnClickListener(v -> {
            handleButtonAnimation(editSiteButton);
            openEditSiteFragment();
        });

        Button deleteSiteButton = createStyledButton("Delete Site");
        deleteSiteButton.setOnClickListener(v -> {
            handleButtonAnimation(deleteSiteButton);
            showDeleteConfirmationDialog();
        });

        buttonLayout.addView(editSiteButton);
        buttonLayout.addView(deleteSiteButton);
    }

    private void openEditSiteFragment() {
        EditSiteFragment editSiteFragment = EditSiteFragment.newInstance(siteId);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, editSiteFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Site")
                .setMessage("Are you sure you want to delete this site?")
                .setPositiveButton("Delete", (dialog, which) -> deleteSite())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteSite() {
        db.collection("donationSites").document(siteId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Site deleted successfully", Toast.LENGTH_SHORT).show();
                    // Navigate back to the previous fragment
                    if (getFragmentManager() != null) {
                        getFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting site", e);
                    Toast.makeText(getContext(), "Error deleting site", Toast.LENGTH_SHORT).show();
                });
    }

    private void addDonorButtons() {
        Button registerToDonateButton = createStyledButton("Register to Donate");
        registerToDonateButton.setOnClickListener(v -> {
            handleButtonAnimation(registerToDonateButton);
            // Directly call handleRegisterToDonate with numDonors set to 1
            openRegistrationFormFragment();
        });

        Button getDirectionsButton = createStyledButton("Get Directions");
        getDirectionsButton.setOnClickListener(v -> {
            handleButtonAnimation(getDirectionsButton);
            getDirectionsToSite();
        });

        buttonLayout.addView(registerToDonateButton);
        buttonLayout.addView(getDirectionsButton);

        checkIfUserIsRegistered();
    }
    private void addRegisterAsVolunteerButton() {
        Button registerAsVolunteerButton = createStyledButton("Register as Volunteer");
        registerAsVolunteerButton.setOnClickListener(v -> {
            handleButtonAnimation(registerAsVolunteerButton);
            checkIfAlreadyVolunteering(currentUser.getUid());
        });

        buttonLayout.addView(registerAsVolunteerButton);
        checkIfUserIsRegistered();
    }

    private void checkIfAlreadyVolunteering(String userId) {
        db.collection("registrations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isVolunteer", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // User is already a volunteer at a site
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            Registration existingRegistration = document.toObject(Registration.class);
                            if (existingRegistration != null) {
                                String existingSiteId = existingRegistration.getSiteId();
                                if (!existingSiteId.equals(siteId)) {
                                    // User is volunteering at a different site, ask for confirmation to switch
                                    showVolunteerConfirmationDialog(userId, existingSiteId, document.getId());
                                } else {
                                    // User is already registered at this site, show a message
                                    Toast.makeText(getContext(), "You are already registered as a volunteer for this site.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            // User is not registered as a volunteer at any site, proceed with registration
                            handleRegisterAsVolunteer();
                        }
                    } else {
                        Log.e(TAG, "Error checking for existing volunteer registration", task.getException());
                        Toast.makeText(getContext(), "Error checking registration status.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showVolunteerConfirmationDialog(String userId, String existingSiteId, String existingRegistrationId) {
        db.collection("donationSites").document(existingSiteId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String existingSiteName = "Unknown Site";
                    if (documentSnapshot.exists()) {
                        DonationSite existingSite = documentSnapshot.toObject(DonationSite.class);
                        if (existingSite != null) {
                            existingSiteName = existingSite.getSiteName();
                        }
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("Change Volunteer Site")
                            .setMessage("You are already registered as a volunteer at " + existingSiteName + ". Do you want to switch to volunteering at this site instead?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                // Update the existing registration to mark as not a volunteer and register for the new site
                                updateExistingRegistration(existingRegistrationId, userId);
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching existing site details", e);
                    Toast.makeText(getContext(), "Error fetching existing site details.", Toast.LENGTH_SHORT).show();
                });
    }


    private void updateExistingRegistration(String existingRegistrationId, String userId) {
        // Update the existing registration to set isVolunteer to false
        db.collection("registrations").document(existingRegistrationId)
                .update("isVolunteer", false)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Existing registration updated successfully");
                    // Register as a volunteer for the current site
                    registerForEvent(userId, siteId, true, 0);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating existing registration", e));
    }

    private void handleRegisterAsVolunteer() {
        String userId = mAuth.getCurrentUser().getUid();
        if (userId != null) {
            registerForEvent(userId, siteId, true, 0);
        } else {
            Toast.makeText(getContext(), "User not signed in.", Toast.LENGTH_SHORT).show();
        }
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
                    Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    disableRegistrationButtons();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding registration", e);
                    Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Button createStyledButton(String text) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.button_background_color)));
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.white)); // Set text color to white

        // Set button width to match parent (fill the width of buttonLayout)
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 16, 0, 0);
        button.setLayoutParams(params);

        // Increase padding
        int paddingInPixels = (int) (16 * getResources().getDisplayMetrics().density); // Convert dp to px
        button.setPadding(paddingInPixels, paddingInPixels, paddingInPixels, paddingInPixels);

        // Increase text size
        button.setTextSize(18); // Increase text size

        return button;
    }

    private void handleButtonAnimation(Button button) {
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.button_click_anim);
        button.startAnimation(animation);
    }

    private void checkIfUserIsRegistered() {
        String userId = mAuth.getCurrentUser().getUid();
        if (userId != null) {
            db.collection("registrations")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("siteId", siteId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    // User is registered, disable the buttons
                                    disableRegistrationButtons();
                                }
                            } else {
                                Log.e(TAG, "Error checking user registration", task.getException());
                            }
                        }
                    });
        }
    }

    private void disableRegistrationButtons() {
        for (int i = 0; i < buttonLayout.getChildCount(); i++) {
            View child = buttonLayout.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                if (button.getText().equals("Register to Donate") ||
                        button.getText().equals("Register as Volunteer")) {
                    button.setEnabled(false);
                }
            }
        }
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

        db.collection("donationSites").document(siteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DonationSite site = documentSnapshot.toObject(DonationSite.class);
                        if (site != null) {
                            siteNameTextView.setText(site.getSiteName());
                            siteAddressTextView.setText(site.getAddress());
                            String startEndDate = String.format("%s - %s", site.getStartDate(), site.getEndDate());
                            siteStartEndDateTextView.setText(startEndDate);
                            String timeRange = String.format("From %s to %s", site.getDonationStartTime(), site.getDonationEndTime());
                            siteHoursTextView.setText(timeRange);
                            siteDaysTextView.setText(formatOperatingDays(site.getDonationDays()));
                            requiredBloodTypesTextView.setText(formatBloodTypes(site.getRequiredBloodTypes()));
                            contactPhoneTextView.setText("Contact Phone: " + site.getContactPhone());
                            contactEmailTextView.setText("Contact Email: " + site.getContactEmail());
                            descriptionTextView.setText("Description: " + site.getDescription());
                            statusTextView.setText("Status: " + site.getStatus());
                            checkUserRoleAndAddButtons(site.getManagerId());
                        }
                    } else {
                        Log.e(TAG, "Site not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching site details", e);
                });
    }

    private String formatOperatingDays(List<String> donationDays) {
        if (donationDays == null || donationDays.isEmpty()) {
            return "Days: Not specified";
        }
        return "Days: " + String.join(", ", donationDays);
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

    private void openRegistrationFormFragment() {
        RegistrationFormFragment registrationFormFragment = RegistrationFormFragment.newInstance(siteId);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, registrationFormFragment)
                .addToBackStack(null)
                .commit();
    }

    private void getDirectionsToSite() {
        if (siteId != null) {
            db.collection("donationSites").document(siteId).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    DonationSite site = documentSnapshot.toObject(DonationSite.class);
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
                } else {
                    Toast.makeText(getContext(), "Site not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error fetching site details for directions", e);
                Toast.makeText(getContext(), "Error fetching site details.", Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(getContext(), "Site ID is not available.", Toast.LENGTH_SHORT).show();
        }
    }
}