package com.minh.bloodlife.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
            startDateEditText, endDateEditText, contactPhoneEditText, contactEmailEditText, descriptionEditText;
    private EditText siteAddressEditText;
    private ChipGroup bloodTypesChipGroup, donationDaysChipGroup;
    private Button createSiteButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Calendar startCalendar, endCalendar;
    private LatLng selectedLatLng;
    private Geocoder geocoder;
    private TextView donationDaysTextView;
    private View view;
    private ProgressBar createSiteProgressBar;
    private Spinner statusSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_site, container, false);

        // Initialize UI components
        initializeUIComponents();

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize start and end Calendars
        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();

        // Initialize Places API
        initializePlacesAPI();

        // Set up AutocompleteSupportFragment for place selection
        setupPlaceAutocomplete();

        // Set up date and time pickers
        setupDateTimePickers();

        // Set up ChipGroup listeners
        setupChipGroupListeners();

        // Set up click listener for create site button
        createSiteButton.setOnClickListener(v -> createSite());

        return view;
    }

    private void initializeUIComponents() {
        siteNameEditText = view.findViewById(R.id.siteNameEditText);
        siteAddressEditText = view.findViewById(R.id.siteAddressEditText);
        donationStartTimeEditText = view.findViewById(R.id.donationStartTimeEditText);
        donationEndTimeEditText = view.findViewById(R.id.donationEndTimeEditText);
        bloodTypesChipGroup = view.findViewById(R.id.bloodTypesChipGroup);
        createSiteButton = view.findViewById(R.id.createSiteButton);
        startDateEditText = view.findViewById(R.id.startDateEditText);
        endDateEditText = view.findViewById(R.id.endDateEditText);
        donationDaysChipGroup = view.findViewById(R.id.donationDaysChipGroup);
        donationDaysTextView = view.findViewById(R.id.donationDaysTextView);
        createSiteProgressBar = view.findViewById(R.id.createSiteProgressBar);
        contactPhoneEditText = view.findViewById(R.id.contactPhoneEditText);
        contactEmailEditText = view.findViewById(R.id.contactEmailEditText);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        statusSpinner = view.findViewById(R.id.statusSpinner);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.site_status_options, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(statusAdapter);
    }

    private void initializePlacesAPI() {
        if (!Places.isInitialized()) {
            Places.initialize(getActivity().getApplicationContext(), PLACES_API_KEY);
        }
        geocoder = new Geocoder(getContext(), Locale.getDefault());
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

    private void setupDateTimePickers() {
        startDateEditText.setOnClickListener(v -> showDatePickerDialog(true));
        endDateEditText.setOnClickListener(v -> showDatePickerDialog(false));
        donationStartTimeEditText.setOnClickListener(v -> showTimePickerDialog(true));
        donationEndTimeEditText.setOnClickListener(v -> showTimePickerDialog(false));
    }

    private void setupChipGroupListeners() {
        setupDonationDaysChipGroup();
        setupBloodTypesChipGroup();
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
        String[] daysOfWeek = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        for (String day : daysOfWeek) {
            Chip chip = new Chip(getContext());
            chip.setText(day);
            chip.setCheckable(true);
            donationDaysChipGroup.addView(chip);
        }

        donationDaysChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedDays = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                selectedDays.add(chip.getText().toString());
            }
            String formattedDays = selectedDays.stream()
                    .map(day -> day.substring(0, Math.min(day.length(), 3)))
                    .collect(Collectors.joining(", "));
            donationDaysTextView.setText(getString(R.string.selected_days_text, formattedDays));
        });
    }

    private void setupBloodTypesChipGroup() {
        String[] bloodTypes = new String[]{"A", "B", "AB", "O"};
        for (String bloodType : bloodTypes) {
            Chip chip = new Chip(getContext());
            chip.setText(bloodType);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.chip_background_color)));
            chip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.chip_text_color)));
            bloodTypesChipGroup.addView(chip);
        }
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
        String siteName = siteNameEditText.getText().toString().trim();
        String siteAddress = siteAddressEditText.getText().toString().trim();
        String donationStartTime = donationStartTimeEditText.getText().toString().trim();
        String donationEndTime = donationEndTimeEditText.getText().toString().trim();
        List<String> requiredBloodTypes = getSelectedBloodTypes();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();
        List<String> donationDays = getSelectedDays();
        String contactPhone = contactPhoneEditText.getText().toString().trim();
        String contactEmail = contactEmailEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String status = statusSpinner.getSelectedItem().toString();

        // Validation
        if (!validateInputFields(siteName, siteAddress, donationStartTime, donationEndTime, startDate, endDate, contactPhone, contactEmail, description)) {
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
        GeoPoint location = (selectedLatLng != null) ? new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude) : null;

        // Generate the searchable name
        String searchableName = siteName.toLowerCase(Locale.ROOT);

        // Create a map for the new site
        Map<String, Object> siteData = new HashMap<>();
        siteData.put("siteName", siteName);
        siteData.put("address", siteAddress);
        siteData.put("location", location);
        siteData.put("donationStartTime", donationStartTime);
        siteData.put("donationEndTime", donationEndTime);
        siteData.put("donationDays", donationDays);
        siteData.put("requiredBloodTypes", requiredBloodTypes);
        siteData.put("managerId", managerId);
        siteData.put("startDate", startDate);
        siteData.put("endDate", endDate);
        siteData.put("contactPhone", contactPhone);
        siteData.put("contactEmail", contactEmail);
        siteData.put("description", description);
        siteData.put("status", status);
        siteData.put("searchableName", searchableName); // Add searchable name for easier search functionality

        // Add the new site to Firestore
        db.collection("donationSites")
                .add(siteData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Donation site created successfully", Toast.LENGTH_SHORT).show();
                    createSiteProgressBar.setVisibility(View.GONE);
                    clearForm();
                    showTickConfirmationPopup(siteName);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error creating donation site: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    createSiteProgressBar.setVisibility(View.GONE);
                });
    }


    private boolean validateInputFields(String siteName, String siteAddress, String donationStartTime, String donationEndTime,
                                        String startDate, String endDate, String contactPhone, String contactEmail, String description) {
        if (siteName.isEmpty() || siteAddress.isEmpty() || donationStartTime.isEmpty() ||
                donationEndTime.isEmpty() || startDate.isEmpty() || endDate.isEmpty() ||
                contactPhone.isEmpty() || contactEmail.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
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
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());

        // Check if start date is on a selected day
        String startDayOfWeek = dayFormat.format(start.getTime());
        if (!selectedDays.contains(startDayOfWeek)) {
            return false;
        }

        // Check if end date is on a selected day
        String endDayOfWeek = dayFormat.format(end.getTime());
        if (!selectedDays.contains(endDayOfWeek)) {
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
        contactPhoneEditText.setText("");
        contactEmailEditText.setText("");
        descriptionEditText.setText("");

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
    }

    private void showTickConfirmationPopup(String siteName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(50, 50, 50, 50);

        ImageView tickImage = new ImageView(getContext());
        tickImage.setImageResource(R.drawable.ic_check_circle);
        tickImage.setColorFilter(Color.GREEN);
        layout.addView(tickImage);

        TextView messageText = new TextView(getContext());
        messageText.setText("The site '" + siteName + "' has been successfully created.");
        messageText.setGravity(Gravity.CENTER);
        messageText.setTextSize(18);
        layout.addView(messageText);

        builder.setView(layout);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}