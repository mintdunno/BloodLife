package com.minh.bloodlife.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.DonationDriveOutcome;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostDonationDataFragment extends DialogFragment {

    private static final String TAG = "PostDonationDataFragment";
    private static final String ARG_SITE_ID = "siteId";

    private String siteId;

    private TextInputEditText totalBloodCollectedInput;
    private LinearLayout bloodTypeBreakdownContainer;
    private Button submitButton;
    private Button cancelButton;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    public static PostDonationDataFragment newInstance(String siteId) {
        PostDonationDataFragment fragment = new PostDonationDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SITE_ID, siteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            siteId = getArguments().getString(ARG_SITE_ID);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_post_donation_data, null);

        totalBloodCollectedInput = view.findViewById(R.id.totalBloodCollectedInput);
        bloodTypeBreakdownContainer = view.findViewById(R.id.bloodTypeBreakdownContainer);
        submitButton = view.findViewById(R.id.submitButton);
        cancelButton = view.findViewById(R.id.cancelButton);
        progressBar = view.findViewById(R.id.progressBar);

        addBloodTypeFields();

        submitButton.setOnClickListener(v -> submitData());
        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view)
                .setTitle("Post-Donation Data");

        return builder.create();
    }

    private void addBloodTypeFields() {
        String[] bloodTypes = {"A", "B", "AB", "O"};
        for (String bloodType : bloodTypes) {
            TextInputLayout textInputLayout = new TextInputLayout(getContext());
            TextInputEditText editText = new TextInputEditText(getContext());
            editText.setHint("Amount collected (" + bloodType + ")");
            editText.setInputType(InputType.TYPE_CLASS_NUMBER); // Set input type to number
            editText.setTag(bloodType); // Set tag to identify blood type later
            textInputLayout.addView(editText);
            bloodTypeBreakdownContainer.addView(textInputLayout);
        }
    }

    private void submitData() {
        String totalCollectedString = totalBloodCollectedInput.getText().toString().trim();

        if (totalCollectedString.isEmpty()) {
            Toast.makeText(getContext(), "Please enter the total blood collected.", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalCollected;
        try {
            totalCollected = Integer.parseInt(totalCollectedString);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid total blood collected. Please enter a number.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> bloodTypeBreakdown = new HashMap<>();
        int sumOfBloodTypes = 0;
        for (int i = 0; i < bloodTypeBreakdownContainer.getChildCount(); i++) {
            TextInputLayout textInputLayout = (TextInputLayout) bloodTypeBreakdownContainer.getChildAt(i);
            TextInputEditText editText = (TextInputEditText) textInputLayout.getEditText();
            if (editText != null) {
                String bloodType = (String) editText.getTag();
                String amountString = editText.getText().toString().trim();
                if (!amountString.isEmpty()) {
                    try {
                        int amount = Integer.parseInt(amountString);
                        sumOfBloodTypes += amount;
                        bloodTypeBreakdown.put(bloodType, amountString);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid amount for " + bloodType + ". Please enter a number.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        // Validate the sum of blood types against the total collected
        if (sumOfBloodTypes != totalCollected) {
            Toast.makeText(getContext(), "The sum of blood types does not match the total collected.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create data object using the model
        DonationDriveOutcome outcome = new DonationDriveOutcome(
                siteId,
                new Date(),
                totalCollectedString, // Store as String to avoid potential type issues in Firestore
                bloodTypeBreakdown
        );

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        // Store data in Firestore
        db.collection("DonationDriveOutcomes")
                .add(outcome)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Data submitted successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error submitting data: ", e);
                    Toast.makeText(getContext(), "Failed to submit data.", Toast.LENGTH_SHORT).show();
                });
    }
}