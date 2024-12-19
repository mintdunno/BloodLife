package com.minh.bloodlife.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.minh.bloodlife.R;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToMainActivity();
            return; // Stop further initialization
        }

        setContentView(R.layout.activity_login);

        // Initialize UI components
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Handle login button click
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firebase authentication for login
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                            navigateToMainActivity();
                        } else {
                            handleFirebaseAuthError(task.getException());
                        }
                    });
        });

        // Handle navigation to RegistrationActivity
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close LoginActivity
    }

    private void handleFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            switch (authException.getErrorCode()) {
                case "ERROR_INVALID_EMAIL":
                    Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
                    break;
                case "ERROR_WRONG_PASSWORD":
                    Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show();
                    break;
                case "ERROR_USER_NOT_FOUND":
                    Toast.makeText(this, "No account found with this email.", Toast.LENGTH_SHORT).show();
                    break;
                case "ERROR_USER_DISABLED":
                    Toast.makeText(this, "This account has been disabled.", Toast.LENGTH_SHORT).show();
                    break;
                case "ERROR_TOO_MANY_REQUESTS":
                    Toast.makeText(this, "Too many login attempts. Please try again later.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Login failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "An unexpected error occurred: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}

