package com.minh.bloodlife.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegistrationFormFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String siteId;
    private LinearLayout registrantsContainer;
    private AutoCompleteTextView numDonorsDropdown;
    private TextInputEditText donationDateInput;
    private Calendar selectedDate = Calendar.getInstance();
    private DonationSite currentSite;
    private List<String> operatingDays;

    public static RegistrationFormFragment newInstance(String siteId) {
        RegistrationFormFragment fragment = new RegistrationFormFragment();
        Bundle args = new Bundle();
        args.putString("siteId", siteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (getArguments() != null) {
            siteId = getArguments().getString("siteId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_form, container, false);
        setHasOptionsMenu(true);

        numDonorsDropdown = view.findViewById(R.id.numDonorsDropdown);
        registrantsContainer = view.findViewById(R.id.registrantsContainer);
        donationDateInput = view.findViewById(R.id.donationDateInput);
        Button registerButton = view.findViewById(R.id.registerButton);

        fetchSiteDetails();

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, new Integer[]{1, 2, 3, 4, 5});
        numDonorsDropdown.setAdapter(adapter);
        numDonorsDropdown.setOnItemClickListener((parent, view1, position, id) -> {
            int numDonors = Integer.parseInt(numDonorsDropdown.getText().toString());
            updateRegistrantFields(numDonors);
        });

        donationDateInput.setOnClickListener(v -> showDatePicker(view));

        registerButton.setOnClickListener(v -> registerDonors());

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

    private void showDatePicker(View view) {
        if (currentSite == null) {
            Toast.makeText(getContext(), "Site details not loaded yet. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        int year = selectedDate.get(Calendar.YEAR);
        int month = selectedDate.get(Calendar.MONTH);
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year1, monthOfYear, dayOfMonth) -> {
            selectedDate.set(Calendar.YEAR, year1);
            selectedDate.set(Calendar.MONTH, monthOfYear);
            selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String selectedDateString = sdf.format(selectedDate.getTime());
            if (isValidDonationDate(selectedDateString)) {
                donationDateInput.setText(selectedDateString);
            } else {
                Toast.makeText(getContext(), "Selected date is not available for donation.", Toast.LENGTH_SHORT).show();
            }
        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(parseDate(currentSite.getStartDate()).getTime());
        datePickerDialog.getDatePicker().setMaxDate(parseDate(currentSite.getEndDate()).getTime());

        datePickerDialog.show();
    }

    private void fetchSiteDetails() {
        db.collection("donationSites").document(siteId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot siteSnapshot = task.getResult();
                currentSite = siteSnapshot.toObject(DonationSite.class);
                operatingDays = currentSite.getDonationDays();
            } else {
                Log.e("RegistrationFormFragment", "Error fetching site details", task.getException());
                Toast.makeText(getContext(), "Failed to load site details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidDonationDate(String selectedDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date date = sdf.parse(selectedDate);
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(date);
            String dayOfWeek = getDayOfWeek(selectedCal.get(Calendar.DAY_OF_WEEK));
            return operatingDays.contains(dayOfWeek);
        } catch (ParseException e) {
            Log.e("RegistrationFormFragment", "Error parsing selected date", e);
            return false;
        }
    }

    private String getDayOfWeek(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
            default:
                return "";
        }
    }
    private void updateRegistrantFields(int numDonors) {
        registrantsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < numDonors; i++) {
            View registrantView = inflater.inflate(R.layout.registrant_form_item, registrantsContainer, false);
            TextInputLayout firstNameLayout = registrantView.findViewById(R.id.firstNameLayout);
            TextInputLayout lastNameLayout = registrantView.findViewById(R.id.lastNameLayout);
            TextInputLayout phoneLayout = registrantView.findViewById(R.id.phoneLayout);
            TextInputLayout emailLayout = registrantView.findViewById(R.id.emailLayout);

            // Find the TextInputEditTexts within each TextInputLayout
            TextInputEditText firstNameInput = firstNameLayout.findViewById(R.id.firstNameInput);
            TextInputEditText lastNameInput = lastNameLayout.findViewById(R.id.lastNameInput);
            TextInputEditText phoneInput = phoneLayout.findViewById(R.id.phoneInput);
            TextInputEditText emailInput = emailLayout.findViewById(R.id.emailInput);

            // Set hints for each field
            firstNameLayout.setHint("First Name " + (i + 1));
            lastNameLayout.setHint("Last Name " + (i + 1));
            phoneLayout.setHint("Phone (Required)" + (i + 1));
            emailLayout.setHint("Email (Optional)" + (i + 1));

            // Set up blood type ChipGroup
            ChipGroup bloodTypeChipGroup = registrantView.findViewById(R.id.bloodTypeChipGroup);
            setupBloodTypeChips(bloodTypeChipGroup);

            registrantsContainer.addView(registrantView);

            // Add a divider between each registrant except the last one
            if (i < numDonors - 1) {
                View divider = new View(getContext());
                divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                registrantsContainer.addView(divider);
            }
        }
    }

    private void setupBloodTypeChips(ChipGroup chipGroup) {
        String[] bloodTypes = new String[]{"A","B","AB","O"};
        for (String bloodType : bloodTypes) {
            Chip chip = new Chip(getContext());
            chip.setText(bloodType);
            chip.setCheckable(true);
            chip.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.chip_background_color)));
            chip.setTextColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.chip_text_color)));
            chipGroup.addView(chip);
        }
    }
    private void registerDonors() {
        String userId = mAuth.getCurrentUser().getUid();
        List<Map<String, Object>> registrants = new ArrayList<>();

        for (int i = 0; i < registrantsContainer.getChildCount(); i++) {
            View registrantView = registrantsContainer.getChildAt(i);
            // Check if this view is a registrant form item
            if (registrantView.getId() == R.id.registrantItem) {
                TextInputEditText firstNameInput = registrantView.findViewById(R.id.firstNameInput);
                TextInputEditText lastNameInput = registrantView.findViewById(R.id.lastNameInput);
                TextInputEditText phoneInput = registrantView.findViewById(R.id.phoneInput);
                TextInputEditText emailInput = registrantView.findViewById(R.id.emailInput);
                ChipGroup bloodTypeChipGroup = registrantView.findViewById(R.id.bloodTypeChipGroup);

                String firstName = firstNameInput.getText().toString();
                String lastName = lastNameInput.getText().toString();
                String phone = phoneInput.getText().toString();
                String email = emailInput.getText().toString();
                String bloodType = getSelectedBloodType(bloodTypeChipGroup);

                if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> registrant = new HashMap<>();
                registrant.put("firstName", firstName);
                registrant.put("lastName", lastName);
                registrant.put("phone", phone);
                registrant.put("email", email);
                registrant.put("bloodType", bloodType);
                registrant.put("isDonor", true);

                registrants.add(registrant);
            }
        }

        String selectedDateString = donationDateInput.getText().toString();
        if (selectedDateString.isEmpty()) {
            Toast.makeText(getContext(), "Please select a donation date.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDonationDate(selectedDateString)) {
            Toast.makeText(getContext(), "Selected date is not available for donation.", Toast.LENGTH_SHORT).show();
            return;
        }

        int numDonors = registrants.size();

        Registration registration = new Registration(userId, siteId, new Date(), false, numDonors, registrants);

        db.collection("registrations")
                .add(registration)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    if (isAdded() && getActivity() != null) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            Log.e("RegistrationFormFragment", "Error parsing date", e);
            return new Date(); // Return current date as default
        }
    }

    private String getSelectedBloodType(ChipGroup chipGroup) {
        int checkedChipId = chipGroup.getCheckedChipId();
        if (checkedChipId != View.NO_ID) {
            Chip checkedChip = chipGroup.findViewById(checkedChipId);
            return checkedChip.getText().toString();
        }
        return ""; // Return empty if nothing is selected
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

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Success");

        // Create a LinearLayout for the custom view
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);

        // Create and add the ImageView
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.ic_check_circle);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(imageView);

        // Create and add the TextView
        TextView textView = new TextView(getContext());
        textView.setText(R.string.register_success);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        layout.addView(textView);

        builder.setView(layout);

        // Set up the OK button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // Navigate back to the Site Details Fragment
                if (isAdded() && getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();

        // Optionally, change properties of the button after it's shown
        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(R.color.black));
        }
    }
}