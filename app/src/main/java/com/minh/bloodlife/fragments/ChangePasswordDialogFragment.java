package com.minh.bloodlife.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.minh.bloodlife.R;

public class ChangePasswordDialogFragment extends DialogFragment {

    private TextInputEditText currentPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private TextInputLayout currentPasswordLayout, newPasswordLayout, confirmNewPasswordLayout;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_change_password_dialog, null);

        currentPasswordEditText = view.findViewById(R.id.current_password_edit_text);
        newPasswordEditText = view.findViewById(R.id.new_password_edit_text);
        confirmNewPasswordEditText = view.findViewById(R.id.confirm_new_password_edit_text);

        currentPasswordLayout = view.findViewById(R.id.current_password_input_layout);
        newPasswordLayout = view.findViewById(R.id.new_password_input_layout);
        confirmNewPasswordLayout = view.findViewById(R.id.confirm_new_password_input_layout);

        builder.setView(view)
                .setTitle("Change Password")
                .setPositiveButton("Change", null) // We'll set the listener later
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Do nothing, just close the dialog
                });

        AlertDialog dialog = builder.create();

        // Override the positive button's onClick listener after the dialog is created
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                changePassword();
            });
        });

        return dialog;
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        // Validation
        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmNewPasswordLayout.setError(null);

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordLayout.setError("Passwords do not match");
            return;
        }

        if (newPassword.length() < 6) {
            newPasswordLayout.setError("Password must be at least 6 characters long");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            newPasswordLayout.setError("New password must be different from the current password");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            showSuccessDialog();
                                            dismiss(); // Close the dialog
                                        } else {
                                            Toast.makeText(getContext(), "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            currentPasswordLayout.setError("Incorrect current password");
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Success");
        builder.setMessage("Password updated successfully!");
        builder.setCancelable(false);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);

        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.ic_check_circle);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(imageView);

        TextView textView = new TextView(getContext());
        textView.setText("Password updated successfully!");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        layout.addView(textView);

        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}