package com.minh.bloodlife.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.User;
import com.minh.bloodlife.utils.FirebaseErrorHandler;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword, editTextPhone;
    private Spinner spinnerUserType;
    private Button buttonRegister;
    private TextView textViewLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();
        setupListeners();
    }

    private void initializeUI() {
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone); // Add this field to your layout
        spinnerUserType = findViewById(R.id.spinnerUserType);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
    }

    private void setupListeners() {
        buttonRegister.setOnClickListener(v -> {
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String phone = editTextPhone.getText().toString().trim();
            String userType = spinnerUserType.getSelectedItem().toString();

            if (!validateInputs(firstName, lastName, email, password, phone)) return;

            checkEmailAndPhoneExistence(email, phone, firstName, lastName, password, userType);
        });

        textViewLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInputs(String firstName, String lastName, String email, String password, String phone) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.PHONE.matcher(phone).matches()) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkEmailAndPhoneExistence(String email, String phone, String firstName, String lastName, String password, String userType) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(this, "Email is already registered.", Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("users")
                                .whereEqualTo("phoneNumber", phone)
                                .get()
                                .addOnCompleteListener(phoneTask -> {
                                    if (phoneTask.isSuccessful() && !phoneTask.getResult().isEmpty()) {
                                        Toast.makeText(this, "Phone number is already registered.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        registerUser(firstName, lastName, email, password, phone, userType);
                                    }
                                });
                    }
                });
    }

    private void registerUser(String firstName, String lastName, String email, String password, String phone, String userType) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user.getUid(), firstName, lastName, email, phone, userType);
                        }
                    } else {
                        String errorMessage = FirebaseErrorHandler.getAuthErrorMessage(task.getException());
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String firstName, String lastName, String email, String phone, String userType) {
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("uid", userId);
        newUser.put("firstName", firstName);
        newUser.put("lastName", lastName);
        newUser.put("email", email);
        newUser.put("phoneNumber", phone);
        newUser.put("userType", userType);

        db.collection("users").document(userId)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    navigateToLogin();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
