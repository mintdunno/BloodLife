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
import android.view.MenuItem;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.firestore.DocumentSnapshot;
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

public class EditSiteFragment extends Fragment {
    private static final String ARG_SITE_ID = "siteId";
    private String siteId;
    private DonationSite siteToEdit;

    private static final String TAG = "EditSiteFragment";
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

    public static EditSiteFragment newInstance(String siteId) {
        EditSiteFragment fragment = new EditSiteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITE_ID, siteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            siteId = getArguments().getString(ARG_SITE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_site, container, false);

        // Initialize UI components
        initializeUIComponents(view);

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

        createSiteButton.setText("Save Change");

        // Set up click listener for create site button
        createSiteButton.setOnClickListener(v -> updateSite());

        fetchSiteData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            androidx.appcompat.app.ActionBar actionBar = activity.getSupportActionBar();
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
            androidx.appcompat.app.ActionBar actionBar = activity.getSupportActionBar();
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
    private void initializeUIComponents(View view) {
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

        // Create a new donation site object
        DonationSite site = new DonationSite(siteName, siteAddress, location, donationStartTime, donationEndTime, donationDays,
                requiredBloodTypes, managerId, startDate, endDate, contactPhone, contactEmail, description, status);

        // Add the new site to Firestore
        db.collection("donationSites")
                .add(site)
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

    private void fetchSiteData() {
        db.collection("donationSites").document(siteId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        siteToEdit = documentSnapshot.toObject(DonationSite.class);
                        if (siteToEdit != null) {
                            populateFields(siteToEdit);
                        }
                    } else {
                        Toast.makeText(getContext(), "Site not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching site data", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateFields(DonationSite site) {
        siteNameEditText.setText(site.getSiteName());
        siteAddressEditText.setText(site.getAddress());
        donationStartTimeEditText.setText(site.getDonationStartTime());
        donationEndTimeEditText.setText(site.getDonationEndTime());
        startDateEditText.setText(site.getStartDate());
        endDateEditText.setText(site.getEndDate());
        contactPhoneEditText.setText(site.getContactPhone());
        contactEmailEditText.setText(site.getContactEmail());
        descriptionEditText.setText(site.getDescription());

        // Set the selected days in the donationDaysChipGroup
        List<String> selectedDays = site.getDonationDays();
        for (int i = 0; i < donationDaysChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) donationDaysChipGroup.getChildAt(i);
            if (selectedDays.contains(chip.getText().toString())) {
                chip.setChecked(true);
            }
        }

        // Set the selected blood types in the bloodTypesChipGroup
        List<String> requiredBloodTypes = site.getRequiredBloodTypes();
        for (int i = 0; i < bloodTypesChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) bloodTypesChipGroup.getChildAt(i);
            if (requiredBloodTypes.contains(chip.getText().toString())) {
                chip.setChecked(true);
            }
        }

        // Set the status in the statusSpinner
        String status = site.getStatus();
        for (int i = 0; i < statusSpinner.getCount(); i++) {
            if (statusSpinner.getItemAtPosition(i).equals(status)) {
                statusSpinner.setSelection(i);
                break;
            }
        }
    }

    private void updateSite() {
        createSiteProgressBar.setVisibility(View.VISIBLE);

        // Collect the updated data from the input fields
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

        // Update the site in Firestore
        if (siteId != null) {
            DonationSite updatedSite = new DonationSite(siteName, siteAddress, selectedLatLng != null ? new GeoPoint(selectedLatLng.latitude, selectedLatLng.longitude) : null,
                    donationStartTime, donationEndTime, donationDays, requiredBloodTypes, siteToEdit.getManagerId(),
                    startDate, endDate, contactPhone, contactEmail, description, status);

            db.collection("donationSites").document(siteId)
                    .set(updatedSite)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Site updated successfully", Toast.LENGTH_SHORT).show();
                        createSiteProgressBar.setVisibility(View.GONE);
                        // Navigate back to the previous screen or refresh the site details
                        if (getActivity() != null) {
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating site", e);
                        Toast.makeText(getContext(), "Error updating site: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        createSiteProgressBar.setVisibility(View.GONE);
                    });
        } else {
            Toast.makeText(getContext(), "Site ID is null", Toast.LENGTH_SHORT).show();
            createSiteProgressBar.setVisibility(View.GONE);
        }
    }
}