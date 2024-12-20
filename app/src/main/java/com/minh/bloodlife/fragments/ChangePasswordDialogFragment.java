package com.minh.bloodlife.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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
                .setPositiveButton("Change", (dialog, which) -> {
                    // Handle password change on positive button click
                    changePassword();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Do nothing, just close the dialog
                });

        return builder.create();
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordLayout.setError("Passwords do not match");
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Re-authenticate the user with their current password
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update the password if re-authentication is successful
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            // Handle re-authentication failure
                            currentPasswordLayout.setError("Incorrect current password");
                        }
                    });
        } else {
            Toast.makeText(getContext(), "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }
}