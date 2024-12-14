package com.minh.bloodlife.ui.registration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.minh.bloodlife.MainActivity;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.User;
import com.minh.bloodlife.ui.login.LoginActivity;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private TextInputLayout textInputLayoutFirstName;
    private TextInputLayout textInputLayoutLastName;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Spinner spinnerUserType;
    private Button buttonRegister;
    private TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth and Database Reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        spinnerUserType = findViewById(R.id.spinnerUserType);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String firstName = editTextFirstName.getText().toString();
                String lastName = editTextLastName.getText().toString();
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String userType = spinnerUserType.getSelectedItem().toString();

                if (validateInputs(firstName, lastName, email, password)) {
                    registerUser(firstName, lastName, email, password, userType);
                }
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginActivity
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void registerUser(String firstName, String lastName, String email, String password, String userType) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Save additional user information to the database
                            if (user != null) {
                                saveUserToDatabase(user.getUid(), firstName, lastName, email, userType);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateInputs(String firstName, String lastName, String email, String password) {
        textInputLayoutFirstName = findViewById(R.id.textInputLayoutFirstName);
        textInputLayoutLastName = findViewById(R.id.textInputLayoutLastName);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        boolean isValid = true;

        if (firstName.isEmpty()) {
            textInputLayoutFirstName.setError("First name is required");
            isValid = false;
        } else {
            textInputLayoutFirstName.setError(null);
        }

        if (lastName.isEmpty()) {
            textInputLayoutLastName.setError("Last name is required");
            isValid = false;
        } else {
            textInputLayoutLastName.setError(null);
        }

        if (email.isEmpty()) {
            textInputLayoutEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textInputLayoutEmail.setError("Invalid email format");
            isValid = false;
        } else {
            textInputLayoutEmail.setError(null);
        }

        if (password.isEmpty()) {
            textInputLayoutPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            textInputLayoutPassword.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            textInputLayoutPassword.setError(null);
        }

        return isValid;
    }

    private void saveUserToDatabase(String userId, String firstName, String lastName, String email, String userType) {
        User user = new User(email, null, email, userType, firstName, lastName);

        mDatabase.child("users").child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User data saved successfully");
                            // Update UI or navigate to the next activity
                            updateUI(mAuth.getCurrentUser());
                        } else {
                            Log.e(TAG, "Failed to save user data", task.getException());
                            Toast.makeText(RegistrationActivity.this, "Failed to save user data.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in, navigate to MainActivity
            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close RegistrationActivity
        } else {
            // User is not signed in, stay on RegistrationActivity
            Toast.makeText(RegistrationActivity.this, "Registration is Failed.", Toast.LENGTH_SHORT).show();
        }
    }
}