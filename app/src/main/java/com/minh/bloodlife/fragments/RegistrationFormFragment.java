package com.minh.bloodlife.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationSite;
import com.minh.bloodlife.model.Registration;

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
        for (int i = 0; i < numDonors; i++) {
            View registrantView = LayoutInflater.from(getContext()).inflate(R.layout.registrant_form_item, registrantsContainer, false);
            TextInputLayout firstNameLayout = registrantView.findViewById(R.id.firstNameLayout);
            TextInputLayout lastNameLayout = registrantView.findViewById(R.id.lastNameLayout);
            TextInputLayout phoneLayout = registrantView.findViewById(R.id.phoneLayout);
            TextInputLayout emailLayout = registrantView.findViewById(R.id.emailLayout);
            TextInputLayout bloodTypeLayout = registrantView.findViewById(R.id.bloodTypeLayout);

            // Set hints for each field
            firstNameLayout.setHint("First Name " + (i + 1));
            lastNameLayout.setHint("Last Name " + (i + 1));
            phoneLayout.setHint("Phone (Required)" + (i + 1));
            emailLayout.setHint("Email (Optional)" + (i + 1));
            bloodTypeLayout.setHint("Blood Type (Optional)" + (i + 1));

            registrantsContainer.addView(registrantView);
        }
    }

    private void registerDonors() {
        String userId = mAuth.getCurrentUser().getUid();
        List<Map<String, Object>> registrants = new ArrayList<>();

        for (int i = 0; i < registrantsContainer.getChildCount(); i++) {
            View registrantView = registrantsContainer.getChildAt(i);
            TextInputEditText firstNameInput = registrantView.findViewById(R.id.firstNameInput);
            TextInputEditText lastNameInput = registrantView.findViewById(R.id.lastNameInput);
            TextInputEditText phoneInput = registrantView.findViewById(R.id.phoneInput);
            TextInputEditText emailInput = registrantView.findViewById(R.id.emailInput);
            AutoCompleteTextView bloodTypeInput = registrantView.findViewById(R.id.bloodTypeInput);

            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();
            String phone = phoneInput.getText().toString();
            String email = emailInput.getText().toString();
            String bloodType = bloodTypeInput.getText().toString();

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
                    // Navigate back or to another appropriate screen
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
}