package com.minh.bloodlife.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationDriveOutcome;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private Button generateReportButton, exportReportButton;
    private TextView totalDonorsTextView, totalVolumeTextView;
    private LinearLayout bloodTypeBreakdownContainer;
    private ProgressBar reportProgressBar;
    private TableLayout reportTable;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reports, container, false);

        startDateInput = view.findViewById(R.id.startDateInput);
        endDateInput = view.findViewById(R.id.endDateInput);
        generateReportButton = view.findViewById(R.id.generateReportButton);
        exportReportButton = view.findViewById(R.id.exportReportButton);
        totalDonorsTextView = view.findViewById(R.id.totalDonorsTextView);
        totalVolumeTextView = view.findViewById(R.id.totalVolumeTextView);
        bloodTypeBreakdownContainer = view.findViewById(R.id.bloodTypeBreakdownContainer);
        reportProgressBar = view.findViewById(R.id.reportProgressBar);
        reportTable = view.findViewById(R.id.reportTable);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

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
            // Fetch the user document from Firestore
            db.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Get the userType field
                                String userType = document.getString("userType");

                                // Check if the user is a Super User
                                if ("Super User".equals(userType)) {
                                    // User is a Super User, enable report generation and export
                                    generateReportButton.setEnabled(true);
                                    exportReportButton.setEnabled(true);
                                } else {
                                    // User is not a Super User, show error message and disable buttons
                                    Toast.makeText(getContext(), "You are not authorized to view this page.", Toast.LENGTH_SHORT).show();
                                    generateReportButton.setEnabled(false);
                                    exportReportButton.setEnabled(false);
                                }
                            } else {
                                Log.d(TAG, "User document not found");
                                generateReportButton.setEnabled(false);
                                exportReportButton.setEnabled(false);
                            }
                        } else {
                            // Handle error
                            Log.e(TAG, "Error getting user data", task.getException());
                            generateReportButton.setEnabled(false);
                            exportReportButton.setEnabled(false);
                        }
                    });
        } else {
            // User not signed in
            generateReportButton.setEnabled(false);
            exportReportButton.setEnabled(false);
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
        reportProgressBar.setVisibility(View.VISIBLE);

        String startDateString = startDateInput.getText().toString();
        String endDateString = endDateInput.getText().toString();

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
            Toast.makeText(getContext(), "Invalid date format.", Toast.LENGTH_SHORT).show();
            reportProgressBar.setVisibility(View.GONE);
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("DonationDriveOutcomes");

        if (startDate != null) {
            query = query.whereGreaterThanOrEqualTo("date", startDate);
        }
        if (endDate != null) {
            query = query.whereLessThanOrEqualTo("date", endDate);
        }

        AtomicInteger totalDonors = new AtomicInteger(0);
        AtomicInteger totalVolume = new AtomicInteger(0);
        Map<String, Integer> bloodTypeTotals = new HashMap<>();

        final Date finalStartDate = startDate;
        final Date finalEndDate = endDate;

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int outcomeCount = task.getResult().size();
                if (outcomeCount == 0) {
                    updateUI(totalDonors.get(), totalVolume.get(), bloodTypeTotals);
                    return;
                }

                AtomicInteger completedCount = new AtomicInteger(0);
                for (QueryDocumentSnapshot outcomeDoc : task.getResult()) {
                    DonationDriveOutcome outcome = outcomeDoc.toObject(DonationDriveOutcome.class);

                    db.collection("registrations")
                            .whereEqualTo("siteId", outcome.getSiteId())
                            .get()
                            .addOnCompleteListener(registrationTask -> {
                                if (registrationTask.isSuccessful()) {
                                    int numDonors = registrationTask.getResult().size();
                                    totalDonors.addAndGet(numDonors);
                                } else {
                                    Log.e(TAG, "Error fetching registrations", registrationTask.getException());
                                }

                                try {
                                    totalVolume.addAndGet(Integer.parseInt(outcome.getTotalCollected()));
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Error parsing totalCollected", e);
                                }

                                Map<String, String> breakdown = outcome.getBloodTypeBreakdown();
                                if (breakdown != null) {
                                    for (Map.Entry<String, String> entry : breakdown.entrySet()) {
                                        String bloodType = entry.getKey();
                                        int amount = Integer.parseInt(entry.getValue());
                                        synchronized (bloodTypeTotals) {
                                            bloodTypeTotals.put(bloodType, bloodTypeTotals.getOrDefault(bloodType, 0) + amount);
                                        }
                                    }
                                }

                                if (completedCount.incrementAndGet() == outcomeCount) {
                                    requireActivity().runOnUiThread(() -> {
                                        updateUI(totalDonors.get(), totalVolume.get(), bloodTypeTotals);
                                    });
                                }
                            });
                }
            } else {
                reportProgressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error getting documents: ", task.getException());
                Toast.makeText(getContext(), "Error generating report.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(int totalDonors, int totalVolume, Map<String, Integer> bloodTypeTotals) {
        totalDonorsTextView.setText("Total Donors: " + totalDonors);
        totalVolumeTextView.setText("Total Volume Collected: " + totalVolume + " ml");

        // Update the table with blood type breakdown
        reportTable.removeAllViews(); // Clear previous data

        // Add table header
        TableRow headerRow = new TableRow(getContext());
        TextView typeHeader = new TextView(getContext());
        typeHeader.setText("Blood Type");
        typeHeader.setPadding(10, 10, 10, 10);
        headerRow.addView(typeHeader);

        TextView amountHeader = new TextView(getContext());
        amountHeader.setText("Amount (ml)");
        amountHeader.setPadding(10, 10, 10, 10);
        headerRow.addView(amountHeader);

        reportTable.addView(headerRow);

        // Add data rows
        for (Map.Entry<String, Integer> entry : bloodTypeTotals.entrySet()) {
            TableRow row = new TableRow(getContext());
            TextView typeView = new TextView(getContext());
            typeView.setText(entry.getKey());
            typeView.setPadding(10, 10, 10, 10);
            row.addView(typeView);

            TextView amountView = new TextView(getContext());
            amountView.setText(String.valueOf(entry.getValue()));
            amountView.setPadding(10, 10, 10, 10);
            row.addView(amountView);

            reportTable.addView(row);
        }

        reportProgressBar.setVisibility(View.GONE);
    }

    // Method to generate and download CSV (add this to ReportsFragment)
    private void downloadReportAsCsv(int totalDonors, int totalVolume, Map<String, Integer> bloodTypeTotals) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Total Donors,Total Volume Collected (ml)\n");
        csvBuilder.append(totalDonors).append(",").append(totalVolume).append("\n\n");
        csvBuilder.append("Blood Type,Amount (ml)\n");

        for (Map.Entry<String, Integer> entry : bloodTypeTotals.entrySet()) {
            csvBuilder.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }

        String csvContent = csvBuilder.toString();
        String filename = "BloodDonationReport_" + System.currentTimeMillis() + ".csv";

        try {
            // Create a file in the app's external storage directory
            File file = new File(requireContext().getExternalFilesDir(null), filename);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(csvContent.getBytes());
            fos.close();

            // Get the content URI using FileProvider
            Uri fileUri = FileProvider.getUriForFile(requireContext(), "com.minh.bloodlife.fileprovider", file);

            // Create an intent to send the email
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/csv");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Blood Donation Report");
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, "Send Report via"));
            } else {
                Toast.makeText(getContext(), "No suitable app found to send the report.", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            Log.e(TAG, "Error generating or sending CSV: ", e);
            Toast.makeText(getContext(), "Error generating or sending CSV.", Toast.LENGTH_SHORT).show();
        }
    }
}