package com.minh.bloodlife.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateSiteFragment extends Fragment {

    private TextInputEditText siteNameEditText, siteAddressEditText, donationHoursEditText;
    private ChipGroup bloodTypesChipGroup;
    private Button createSiteButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_site, container, false);

        siteNameEditText = view.findViewById(R.id.siteNameEditText);
        siteAddressEditText = view.findViewById(R.id.siteAddressEditText);
        donationHoursEditText = view.findViewById(R.id.donationHoursEditText);
        bloodTypesChipGroup = view.findViewById(R.id.bloodTypesChipGroup);
        createSiteButton = view.findViewById(R.id.createSiteButton);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Set up time picker for donation hours
        donationHoursEditText.setOnClickListener(v -> showTimePickerDialog());

        createSiteButton.setOnClickListener(v -> createSite());

        return view;
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        // Handle the selected time here
                        String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                        donationHoursEditText.setText(formattedTime);
                    }
                }, hour, minute, true); // true for 24-hour format

        timePickerDialog.show();
    }

    private void createSite() {
        String siteName = siteNameEditText.getText().toString().trim();
        String siteAddress = siteAddressEditText.getText().toString().trim();
        String donationHours = donationHoursEditText.getText().toString().trim();
        List<String> requiredBloodTypes = getSelectedBloodTypes();

        if (siteName.isEmpty() || siteAddress.isEmpty() || donationHours.isEmpty() || requiredBloodTypes.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }
        String managerId = user.getUid(); // Get the current user's ID

        // Create a new donation site object
        Map<String, Object> site = new HashMap<>();
        site.put("siteName", siteName);
        site.put("address", siteAddress);
        site.put("donationHours", donationHours);
        site.put("requiredBloodTypes", requiredBloodTypes);
        site.put("managerId", managerId);
        site.put("searchableName", siteName.toLowerCase());
        site.put("location", null); // Placeholder for location

        // Add the new site to Firestore
        db.collection("donationSites")
                .add(site)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Donation site created successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, clear the form or navigate the user to a different fragment
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error creating donation site: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private List<String> getSelectedBloodTypes() {
        List<String> selectedTypes = new ArrayList<>();
        for (int i = 0; i < bloodTypesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) bloodTypesChipGroup.getChildAt(i);
            if (chip.isChecked()) {
                selectedTypes.add(chip.getText().toString());
            }
        }
        return selectedTypes;
    }
}