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
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateSiteFragment extends Fragment {

    private static final String TAG = "CreateSiteFragment";
    private static final String PLACES_API_KEY = "AIzaSyAmYG0ewlmb4zaJAkC6pBsFjqi0NBQu-Po";

    private TextInputEditText siteNameEditText, donationStartTimeEditText, donationEndTimeEditText,
            startDateEditText, endDateEditText;
    private EditText siteAddressEditText;
    private ChipGroup bloodTypesChipGroup, donationDaysChipGroup;
    private Button createSiteButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Calendar startCalendar, endCalendar;
    private LatLng selectedLatLng;
    private Geocoder geocoder;
    private TextView donationDaysTextView; // TextView to display selected days
    private View view;

    private ProgressBar createSiteProgressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_site, container, false);

        // Initialize UI components
        siteNameEditText = view.findViewById(R.id.siteNameEditText);
        siteAddressEditText = view.findViewById(R.id.siteAddressEditText);
        donationStartTimeEditText = view.findViewById(R.id.donationStartTimeEditText);
        donationEndTimeEditText = view.findViewById(R.id.donationEndTimeEditText);
        bloodTypesChipGroup = view.findViewById(R.id.bloodTypesChipGroup);
        createSiteButton = view.findViewById(R.id.createSiteButton);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        donationDaysChipGroup = view.findViewById(R.id.donationDaysChipGroup);
        donationDaysTextView = view.findViewById(R.id.donationDaysTextView); // TextView for selected days
        createSiteProgressBar = view.findViewById(R.id.createSiteProgressBar);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize start and end Calendars
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), PLACES_API_KEY);
        }
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        // Set up AutocompleteSupportFragment for place selection
        setupPlaceAutocomplete();

        // Set up date and time pickers
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(true));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(false));
        donationStartTimeEditText.setOnClickListener(v -> showTimePickerDialog(true));
        donationEndTimeEditText.setOnClickListener(v -> showTimePickerDialog(false));

        // Set up ChipGroup listener for donation days
        setupDonationDaysChipGroup();

        // Set up click listener for create site button
        createSiteButton.setOnClickListener(v -> createSite());

        return view;
    }

    private void setupPlaceAutocomplete() {
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.ADDRESS));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                siteAddressEditText.setText(place.getAddress());
                selectedLatLng = place.getLatLng();
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.e(TAG, "An error occurred: " + status);
                Toast.makeText(getContext(), "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(boolean isStartDate) {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            Calendar calendar = isStartDate ? startCalendar : endCalendar;
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateLabel(isStartDate ? startDateEditText : endDateEditText, calendar);
        };

        Calendar calendar = isStartDate ? startCalendar : endCalendar;
        new DatePickerDialog(
                getContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void updateDateLabel(EditText editText, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        editText.setText(sdf.format(calendar.getTime()));
    }

    private void showTimePickerDialog(boolean isStartTime) {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            if (isStartTime) {
                donationStartTimeEditText.setText(formattedTime);
            } else {
                donationEndTimeEditText.setText(formattedTime);
            }
        };

        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(
                getContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void setupDonationDaysChipGroup() {
        donationDaysChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedDays = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                selectedDays.add(chip.getText().toString());
            }

            // Update the TextView with the selected days
            String formattedDays = selectedDays.stream()
                    .map(day -> day.substring(0, Math.min(day.length(), 3))) // Get first 3 letters
                    .collect(Collectors.joining(", ")); // Join with comma and space

            donationDaysTextView.setText(getString(R.string.selected_days_text, formattedDays));
        });
    }

    private List<String> getSelectedDays() {
        List<String> selectedDays = new ArrayList<>();
        for (int checkedId : donationDaysChipGroup.getCheckedChipIds()) {
            Chip chip = donationDaysChipGroup.findViewById(checkedId);
            selectedDays.add(chip.getText().toString());
        }
        return selectedDays;
    }

    private void createSite() {
        createSiteProgressBar.setVisibility(View.VISIBLE);
        // Collect data from input fields
        String siteName = siteNameEditText.getText().toString().trim();
        String siteAddress = siteAddressEditText.getText().toString().trim();
        String donationStartTime = donationStartTimeEditText.getText().toString().trim();
        String donationEndTime = donationEndTimeEditText.getText().toString().trim();
        List<String> requiredBloodTypes = getSelectedBloodTypes();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();
        List<String> donationDays = getSelectedDays();

        // Validation
        if (!validateInputFields(siteName, siteAddress, donationStartTime, donationEndTime, startDate, endDate)) {
            createSiteProgressBar.setVisibility(View.GONE);
            return;
        }

        // Check if start and end dates are within selected donation days
        if (!isWithinSelectedDays(donationDays, startCalendar, endCalendar)) {
            Toast.makeText(getContext(), "Start and end dates must be on selected donation days", Toast.LENGTH_SHORT).show();
            createSiteProgressBar.setVisibility(View.GONE);
            return;
        }

        // Get the current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "User not authenticated.", Toast.LENGTH_SHORT).show();
            createSiteProgressBar.setVisibility(View.GONE);
            return;
        }
        String managerId = user.getUid();

        // Convert the selected LatLng to a GeoPoint
        GeoPoint location = (selectedLatLng != null) ?
                new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude) :
                null;

        // Create a new donation site object
        Map<String, Object> site = new HashMap<>();
        site.put("siteName", siteName);
        site.put("address", siteAddress);
        site.put("donationStartTime", donationStartTime);
        site.put("donationEndTime", donationEndTime);
        site.put("requiredBloodTypes", requiredBloodTypes);
        site.put("managerId", managerId);
        site.put("searchableName", siteName.toLowerCase());
        site.put("location", location);
        site.put("startDate", startDate);
        site.put("endDate", endDate);
        site.put("donationDays", donationDays);

        // Add the new site to Firestore
        db.collection("donationSites")
                .add(site)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Donation site created successfully", Toast.LENGTH_SHORT).show();
                    createSiteProgressBar.setVisibility(View.GONE);
                    clearForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error creating donation site: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    createSiteProgressBar.setVisibility(View.GONE);
                });
    }

    private boolean validateInputFields(String siteName, String siteAddress, String donationStartTime,
                                        String donationEndTime, String startDate, String endDate) {
        if (siteName.isEmpty() || siteAddress.isEmpty() || donationStartTime.isEmpty() ||
                donationEndTime.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!donationStartTime.matches("\\d{2}:\\d{2}") || !donationEndTime.matches("\\d{2}:\\d{2}")) {
            Toast.makeText(getContext(), "Invalid time format", Toast.LENGTH_SHORT).show();
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date startDateObj = dateFormat.parse(startDate);
            Date endDateObj = dateFormat.parse(endDate);
            if (!endDateObj.after(startDateObj)) {
                Toast.makeText(getContext(), "End date must be after start date", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isWithinSelectedDays(List<String> selectedDays, Calendar start, Calendar end) {
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        // Check if start date is on a selected day
        String startDayOfWeek = dayFormat.format(start.getTime());
        boolean startFound = false;
        for (String selectedDay : selectedDays) {
            if (selectedDay.toLowerCase().startsWith(startDayOfWeek.toLowerCase())) {
                startFound = true;
                break;
            }
        }
        if (!startFound) {
            return false;
        }

        // Check if end date is on a selected day
        String endDayOfWeek = dayFormat.format(end.getTime());
        boolean endFound = false;
        for (String selectedDay : selectedDays) {
            if (selectedDay.toLowerCase().startsWith(endDayOfWeek.toLowerCase())) {
                endFound = true;
                break;
            }
        }
        if (!endFound) {
            return false;
        }

        return true;
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

    private void clearForm() {
        siteNameEditText.setText("");
        siteAddressEditText.setText("");
        donationStartTimeEditText.setText("");
        donationEndTimeEditText.setText("");
        startDateEditText.setText("");
        endDateEditText.setText("");

        // Clear selected blood types
        bloodTypesChipGroup.clearCheck();

        // Clear selected donation days and reset the TextView
        donationDaysChipGroup.clearCheck();
        donationDaysTextView.setText(getString(R.string.selected_days_text, ""));

        // Reset start and end calendars
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Reset selected location (if applicable)
        selectedLatLng = null;

        // You might need to reset the AutocompleteSupportFragment as well,
        // depending on how it's implemented
    }
}