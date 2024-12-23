package com.minh.bloodlife.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;
import com.minh.bloodlife.model.Registration;
import com.minh.bloodlife.model.User;

import java.io.IOException;
import java.io.OutputStream;
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
    private static final int CREATE_FILE = 1;
    private String siteId;
    private TextView siteNameTextView, siteAddressTextView, siteHoursTextView, requiredBloodTypesTextView,
            siteDaysTextView, siteStartEndDateTextView, contactPhoneTextView, contactEmailTextView, descriptionTextView,
            statusTextView;
    private LinearLayout buttonLayout;
    private View view;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressBar searchProgressBar;
    private List<User> donors;

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
            Log.d("SiteDetailsFragment", "onCreate - siteId: " + siteId);
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
        searchProgressBar = view.findViewById(R.id.searchProgressBar);

        // Fetch site details first to check if the user is a manager of this site
        fetchSiteDetails();

        return view;
    }

    private void checkUserRoleAndAddButtons(String managerId) {
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Assuming `User` data doesn't change frequently, fetch it once and store locally
            db.collection("users").document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult().toObject(User.class);
                    if (user != null) {
                        if ("Site Manager".equals(user.getUserType())) {
                            if (managerId.equals(userId)) {
                                addSiteManagerButtons();
                                addEditAndDeleteButtons();
                            } else {
                                addRegisterAsVolunteerButton();
                            }
                        } else {
                            addDonorButtons();
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to fetch user role", task.getException());
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

        Button downloadDonorsButton = createStyledButton("Download Donors List");
        downloadDonorsButton.setOnClickListener(v -> {
            handleButtonAnimation(downloadDonorsButton);
            downloadDonorList();
        });

        Button postDonationDataButton = createStyledButton("Post-Donation Data");
        postDonationDataButton.setOnClickListener(v -> {
            handleButtonAnimation(postDonationDataButton);
            openPostDonationDataDialog();
        });

        buttonLayout.addView(registerAsVolunteerButton);
        buttonLayout.addView(getDirectionsButton);
        buttonLayout.addView(viewStatisticsButton);
        buttonLayout.addView(downloadDonorsButton);
        buttonLayout.addView(postDonationDataButton);

        checkIfUserIsRegistered();
    }

    private void openPostDonationDataDialog() {
        PostDonationDataFragment dialogFragment = PostDonationDataFragment.newInstance(siteId);
        dialogFragment.show(getChildFragmentManager(), "PostDonationDataFragment");
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
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            Registration existingRegistration = document.toObject(Registration.class);
                            Log.d("SiteDetailsFragment", "Existing Registration: " + existingRegistration);
                            if (existingRegistration != null) {
                                String existingSiteId = existingRegistration.getSiteId();
                                Log.d("SiteDetailsFragment", "Existing siteId: " + existingSiteId);
                                if (!existingSiteId.equals(siteId)) {
                                    showVolunteerConfirmationDialog(userId, existingSiteId, document.getId());
                                } else {
                                    Toast.makeText(getContext(), "You are already registered as a volunteer for this site.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            handleRegisterAsVolunteer(); // Proceed with new registration
                        }
                    } else {
                        Log.e("SiteDetailsFragment", "Error checking for existing volunteer registration", task.getException());
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

    private void updateExistingRegistration(String registrationId, String siteId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("siteId", siteId);
        updates.put("registrationDate", new Date());

        db.collection("registrations").document(registrationId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("SiteDetailsFragment", "Registration updated successfully - siteId: " + siteId);
                    Toast.makeText(getContext(), "Your volunteer registration has been updated to this site.",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("SiteDetailsFragment", "Error updating registration", e);
                    Toast.makeText(getContext(), "Failed to update registration: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void handleRegisterAsVolunteer() {
        String userId = mAuth.getCurrentUser().getUid();
        Log.d("SiteDetailsFragment", "Registering new volunteer - userId: " + userId + ", siteId: " + siteId);
        if (userId != null) {
            registerForEvent(userId, siteId, true, 0);
        } else {
            Toast.makeText(getContext(), "User not signed in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerForEvent(String userId, String siteId, boolean isVolunteer, int numDonors) {
        Log.d("SiteDetailsFragment", "Registering volunteer - userId: " + userId + ", siteId: " + siteId);
        Map<String, Object> registration = new HashMap<>();
        registration.put("userId", userId); // Save the userId
        registration.put("siteId", siteId); // Save the siteId
        registration.put("registrationDate", new Date());
        registration.put("isVolunteer", isVolunteer);
        registration.put("numDonors", numDonors);

        db.collection("registrations")
                .add(registration)
                .addOnSuccessListener(documentReference -> {
                    Log.d("SiteDetailsFragment", "Registration successful: " + documentReference.getId());
                    Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    disableRegistrationButtons();
                })
                .addOnFailureListener(e -> {
                    Log.e("SiteDetailsFragment", "Error adding registration", e);
                    Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Button createStyledButton(String text) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                R.color.button_background_color)));
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
        Log.d(TAG, "Fetching site details for siteId: " + siteId);
        if (view == null) {
            Log.e(TAG, "View is null, cannot fetch site details.");
            return;
        }

        db.collection("donationSites").document(siteId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DonationSite site = documentSnapshot.toObject(DonationSite.class);
                        if (site != null) {
                            displaySiteDetails(site);
                        }
                    } else {
                        showRetryDialog("Site not found", "Retry fetching site details?");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching site details", e);
                    showRetryDialog("Error fetching site details", "Would you like to try again?");
                });
    }

    private void showRetryDialog(String title, String message) {
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Retry", (dialog, which) -> fetchSiteDetails())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void displaySiteDetails(DonationSite site) {
        siteNameTextView.setText(site.getSiteName());
        siteAddressTextView.setText(site.getAddress());
        // Format and set the start and end date
        String startEndDate = String.format("%s - %s", site.getStartDate(), site.getEndDate());
        siteStartEndDateTextView.setText(startEndDate);
        // Format and set the donation hours
        String timeRange = String.format("From %s to %s", site.getDonationStartTime(), site.getDonationEndTime());
        siteHoursTextView.setText(timeRange);
        // Format and set operating days
        siteDaysTextView.setText(formatOperatingDays(site.getDonationDays()));
        // Format and set required blood types
        requiredBloodTypesTextView.setText(formatBloodTypes(site.getRequiredBloodTypes()));
        // Set contact details
        contactPhoneTextView.setText("Phone: " + (site.getContactPhone() != null ? site.getContactPhone() : "N/A"));
        contactEmailTextView.setText("Email: " + (site.getContactEmail() != null ? site.getContactEmail() : "N/A"));
        // Set the description
        descriptionTextView.setText("Description: " + (site.getDescription() != null ? site.getDescription() : "N/A"));
        // Set the site status
        statusTextView.setText("Status: " + (site.getStatus() != null ? site.getStatus() : "Unknown"));
        // Handle roles and button setup
        checkUserRoleAndAddButtons(site.getManagerId());
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

    private void downloadDonorList() {
        fetchDonorDataAndDownload();
    }

    private void showProgressBar() {
        if (searchProgressBar != null) {
            searchProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (searchProgressBar != null) {
            searchProgressBar.setVisibility(View.GONE);
        }
    }
    private void fetchDonorDataAndDownload() {
        showProgressBar();

        db.collection("registrations")
                .whereEqualTo("siteId", siteId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> donorIds = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            List<Map<String, Object>> registrants = (List<Map<String, Object>>) doc.get("registrants");
                            if (registrants != null) {
                                for (Map<String, Object> registrant : registrants) {
                                    String userId = (String) registrant.get("userId");
                                    if (userId != null) {
                                        donorIds.add(userId);
                                    }
                                }
                            }
                        }

                        if (!donorIds.isEmpty()) {
                            fetchDonorDetailsAndGenerateFile(donorIds);
                        } else {
                            hideProgressBar();
                            Toast.makeText(getContext(), "No donors found for this site.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideProgressBar();
                        Log.e(TAG, "Error getting registrations: ", task.getException());
                        Toast.makeText(getContext(), "Failed to fetch donor data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchDonorDetailsAndGenerateFile(List<String> donorIds) {
        db.collection("users")
                .whereIn("uid", donorIds)
                .get()
                .addOnCompleteListener(task -> {
                    hideProgressBar();
                    if (task.isSuccessful()) {
                        donors = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            donors.add(document.toObject(User.class));
                        }

                        if (!donors.isEmpty()) {
                            generateAndDownloadFile(donors);
                        } else {
                            Toast.makeText(getContext(), "No donor details found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error getting user details: ", task.getException());
                        Toast.makeText(getContext(), "Failed to fetch donor details.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generateAndDownloadFile(List<User> donors) {
        // Choose CSV or JSON format (I'll demonstrate CSV here)
        String fileContent = generateCsvContent(donors);
        String filename = "donors_" + siteId + "_" + System.currentTimeMillis() + ".csv";

        // Use Storage Access Framework to let the user choose the download location
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, filename);

        startActivityForResult(intent, CREATE_FILE);
    }

    private String generateCsvContent(List<User> donors) {
        StringBuilder csvBuilder = new StringBuilder();
        // Add CSV header
        csvBuilder.append("First Name,Last Name,Email,Phone Number,Blood Type\n");

        // Add donor data
        for (User donor : donors) {
            csvBuilder.append(donor.getFirstName()).append(",");
            csvBuilder.append(donor.getLastName()).append(",");
            csvBuilder.append(donor.getEmail() != null ? donor.getEmail() : "").append(",");
            csvBuilder.append(donor.getPhoneNumber() != null ? donor.getPhoneNumber() : "").append(",");
            csvBuilder.append(donor.getBloodType() != null ? donor.getBloodType() : "").append("\n");
        }

        return csvBuilder.toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CREATE_FILE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                // Write the file content to the selected URI
                writeCsvToFile(uri, generateCsvContent(donors)); // donors should be accessible here
            }
        }
    }

    private void writeCsvToFile(Uri uri, String content) {
        try {
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(content.getBytes());
                outputStream.close();
                Toast.makeText(getContext(), "File saved successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to save file.", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error writing to file: ", e);
            Toast.makeText(getContext(), "Failed to save file.", Toast.LENGTH_SHORT).show();
        }
    }
}