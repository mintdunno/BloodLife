package com.minh.bloodlife.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationDriveOutcome;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportsFragment extends Fragment {

    private static final String TAG = "ReportsFragment";

    private TextInputEditText startDateInput, endDateInput;
    private Button generateReportButton;
    private TextView totalDonorsTextView, totalVolumeTextView, bloodTypeBreakdownTextView;
    private ProgressBar reportProgressBar;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        startDateInput = view.findViewById(R.id.startDateInput);
        endDateInput = view.findViewById(R.id.endDateInput);
        generateReportButton = view.findViewById(R.id.generateReportButton);
        totalDonorsTextView = view.findViewById(R.id.totalDonorsTextView);
        totalVolumeTextView = view.findViewById(R.id.totalVolumeTextView);
        bloodTypeBreakdownTextView = view.findViewById(R.id.bloodTypeBreakdownTextView);
        reportProgressBar = view.findViewById(R.id.reportProgressBar); // Initialize ProgressBar

        startDateInput.setOnClickListener(v -> showDatePicker(true));
        endDateInput.setOnClickListener(v -> showDatePicker(false));
        generateReportButton.setOnClickListener(v -> generateReport());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkIfSuperUser();
    }

    private void checkIfSuperUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Map<String, Object> claims = task.getResult().getClaims();
                            if (claims.containsKey("isSuperUser") && (boolean) claims.get("isSuperUser")) {
                                // User is a Super User, show content
                                // (Enable input fields/buttons if needed)
                            } else {
                                // User is not a Super User, show error message or redirect
                                Toast.makeText(getContext(), "You are not authorized to view this page.", Toast.LENGTH_SHORT).show();
                                // TODO: Redirect to another fragment or handle as needed
                            }
                        } else {
                            // Handle error
                            Log.e(TAG, "Error getting auth claims", task.getException());
                        }
                    });
        } else {
            // User not signed in, handle accordingly (e.g., redirect to login)
        }
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year1);
                    calendar.set(Calendar.MONTH, monthOfYear);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String formattedDate = dateFormat.format(calendar.getTime());

                    if (isStartDate) {
                        startDateInput.setText(formattedDate);
                    } else {
                        endDateInput.setText(formattedDate);
                    }
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void generateReport() {
        reportProgressBar.setVisibility(View.VISIBLE); // Show progress bar

        // 1. Get filter values
        String startDateString = startDateInput.getText().toString();
        String endDateString = endDateInput.getText().toString();

        // 2. Parse dates
        Date startDate = null;
        Date endDate = null;

        try {
            if (!startDateString.isEmpty()) {
                startDate = dateFormat.parse(startDateString);
            }
            if (!endDateString.isEmpty()) {
                endDate = dateFormat.parse(endDateString);
            }
        } catch (ParseException e) {
            // Handle date parsing error
            Toast.makeText(getContext(), "Invalid date format.", Toast.LENGTH_SHORT).show();
            reportProgressBar.setVisibility(View.GONE);
            return;
        }

        // 3. Fetch data from Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("DonationDriveOutcomes");

        // Apply date range filter (if dates are provided)
        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("date", startDate);
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("date", endDate);
        }

        // Use AtomicInteger for variables that need to be updated inside lambdas
        AtomicInteger totalDonors = new AtomicInteger(0);
        AtomicInteger totalVolume = new AtomicInteger(0);
        Map<String, Integer> bloodTypeTotals = new HashMap<>();

        // 4. Execute the query
        final Date finalStartDate = startDate;
        final Date finalEndDate = endDate;
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot outcomeDoc : task.getResult()) {
                    DonationDriveOutcome outcome = outcomeDoc.toObject(DonationDriveOutcome.class);

                    // Fetch registrations for this site and date
                    db.collection("registrations")
                            .whereEqualTo("siteId", outcome.getSiteId())
                            .get()
                            .addOnCompleteListener(registrationTask -> {
                                if (registrationTask.isSuccessful()) {
                                    int numDonors = registrationTask.getResult().size();
                                    totalDonors.addAndGet(numDonors);
                                } else {
                                    // Handle error
                                    Log.e(TAG, "Error fetching registrations", registrationTask.getException());
                                }
                            });

                    // Aggregate total volume
                    try {
                        totalVolume.addAndGet(Integer.parseInt(outcome.getTotalCollected()));
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing totalCollected", e);
                    }

                    // Aggregate blood type breakdown
                    Map<String, String> breakdown = outcome.getBloodTypeBreakdown();
                    if (breakdown != null) {
                        for (Map.Entry<String, String> entry : breakdown.entrySet()) {
                            String bloodType = entry.getKey();
                            int amount = Integer.parseInt(entry.getValue());

                            // Update the total for this blood type safely
                            synchronized (bloodTypeTotals) {
                                bloodTypeTotals.put(bloodType, bloodTypeTotals.getOrDefault(bloodType, 0) + amount);
                            }
                        }
                    }
                }

                // 5. Update the UI (must be done on the main thread)
                requireActivity().runOnUiThread(() -> {
                    totalDonorsTextView.setText("Total Donors: " + totalDonors.get());
                    totalVolumeTextView.setText("Total Volume Collected: " + totalVolume.get() + " ml");

                    // Format and display blood type breakdown
                    StringBuilder breakdownBuilder = new StringBuilder("Blood Type Breakdown:\n");
                    for (Map.Entry<String, Integer> entry : bloodTypeTotals.entrySet()) {
                        breakdownBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ml\n");
                    }
                    bloodTypeBreakdownTextView.setText(breakdownBuilder.toString());

                    reportProgressBar.setVisibility(View.GONE); // Hide progress bar
                });

            } else {
                reportProgressBar.setVisibility(View.GONE); // Hide progress bar
                Log.e(TAG, "Error getting documents: ", task.getException());
                Toast.makeText(getContext(), "Error generating report.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}