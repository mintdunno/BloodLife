package com.minh.bloodlife.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateSiteFragment extends Fragment {

    private static final String TAG = "CreateSiteFragment";
    private TextInputEditText siteNameEditText, donationHoursEditText, startDateEditText, endDateEditText;
    private EditText siteAddressEditText;
    private ChipGroup bloodTypesChipGroup;
    private Button createSiteButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Calendar startCalendar, endCalendar;
    private LatLng selectedLatLng;
    private Geocoder geocoder;
    private static final String PLACES_API_KEY = "YOUR_API_KEY";

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
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize the start and end Calendars
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Set up date picker for start date
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(true));

        // Set up date picker for end date
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(false));

        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(requireActivity().getApplicationContext(), PLACES_API_KEY);
        }
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        // Create a new Places client instance
        PlacesClient placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // Get info about the selected place
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());
                siteAddressEditText.setText(place.getAddress());

                selectedLatLng = place.getLatLng();
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle the error
                Log.e(TAG, "An error occurred: " + status);
                Toast.makeText(getContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up time picker for donation hours
        donationHoursEditText.setOnClickListener(v -> showTimePickerDialog());

        createSiteButton.setOnClickListener(v -> createSite());

        return view;
    }

    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if (isStartDate) {
                    startCalendar.set(Calendar.YEAR, year);
                    startCalendar.set(Calendar.MONTH, month);
                    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                } else {
                    endCalendar.set(Calendar.YEAR, year);
                    endCalendar.set(Calendar.MONTH, month);
                    endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                }
                updateDateLabel(isStartDate);
            }
        };

        Calendar calendar = isStartDate ? startCalendar : endCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateLabel(boolean isStartDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (isStartDate) {
            startDateEditText.setText(sdf.format(startCalendar.getTime()));
        } else {
            endDateEditText.setText(sdf.format(endCalendar.getTime()));
        }
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
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();

        // Check if any field is empty
        if (siteName.isEmpty() || siteAddress.isEmpty() || donationHours.isEmpty() ||
                requiredBloodTypes.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate dates
        if (startCalendar.after(endCalendar)) {
            Toast.makeText(getContext(), "End date must be after start date", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            return;
        }

        String managerId = user.getUid();

        // Convert the selected LatLng to a GeoPoint
        GeoPoint location = null;
        if (selectedLatLng != null) {
            location = new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude);
        } else {
            Toast.makeText(getContext(), "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new donation site object
        Map<String, Object> site = new HashMap<>();
        site.put("siteName", siteName);
        site.put("address", siteAddress);
        site.put("donationHours", donationHours);
        site.put("requiredBloodTypes", requiredBloodTypes);
        site.put("managerId", managerId);
        site.put("searchableName", siteName.toLowerCase());
        site.put("location", location);
        site.put("startDate", startDate);
        site.put("endDate", endDate);

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