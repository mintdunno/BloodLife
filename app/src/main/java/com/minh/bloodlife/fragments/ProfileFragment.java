package com.minh.bloodlife.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.minh.bloodlife.R;
import com.minh.bloodlife.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private CircleImageView profileImageView;
    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText;
    private AutoCompleteTextView bloodTypeDropdown;
    private TextView userTypeText;
    private Button changeAvatarButton, changePasswordButton, saveProfileButton, logoutButton, editProfileButton, cancelButton;
    private SwitchMaterial enableNotificationsSwitch, enablePushNotificationsSwitch;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Uri selectedImageUri;
    private String userId;
    private User user;
    private ActivityResultLauncher<String> mGetContent;
    private ActivityResultLauncher<Intent> mTakePhoto;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        // Initialize the ActivityResultLauncher for picking images
        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        profileImageView.setImageURI(uri);
                        saveProfileButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                    }
                });

        // Initialize the ActivityResultLauncher for taking photos
        mTakePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // The image capture was successful, you can now use selectedImageUri
                        // For example, you can set the image to the ImageView
                        profileImageView.setImageURI(selectedImageUri);
                        saveProfileButton.setVisibility(View.VISIBLE);
                        cancelButton.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profile_image);
        firstNameEditText = view.findViewById(R.id.first_name_edit_text);
        lastNameEditText = view.findViewById(R.id.last_name_edit_text);
        emailEditText = view.findViewById(R.id.email_edit_text);
        phoneEditText = view.findViewById(R.id.phone_edit_text);
        bloodTypeDropdown = view.findViewById(R.id.blood_type_dropdown);
        changeAvatarButton = view.findViewById(R.id.change_avatar_button);
        changePasswordButton = view.findViewById(R.id.change_password_button);
        saveProfileButton = view.findViewById(R.id.save_profile_button);
        logoutButton = view.findViewById(R.id.logout_button);
        enableNotificationsSwitch = view.findViewById(R.id.enable_notifications_switch);
        enablePushNotificationsSwitch = view.findViewById(R.id.enable_push_notifications_switch);
        userTypeText = view.findViewById(R.id.user_type_text);
        editProfileButton = view.findViewById(R.id.edit_profile_button);
        cancelButton = view.findViewById(R.id.cancel_button);

        if (currentUser != null) {
            userId = currentUser.getUid();
            loadUserProfile(userId);
        }

        // Set up the blood type dropdown
        String[] bloodTypes = new String[]{"Unknown","A", "B", "AB", "O"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.dropdown_menu_popup_item, bloodTypes);
        bloodTypeDropdown.setAdapter(adapter);

        // Initially, the "Save Changes" and "Cancel" buttons should be hidden
        saveProfileButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        // Set up listeners for buttons
        setupButtonClickListeners();

        return view;
    }

    private void setupButtonClickListeners() {
        changeAvatarButton.setOnClickListener(v -> showImagePickerOptions());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        saveProfileButton.setOnClickListener(v -> saveProfileChanges());
        logoutButton.setOnClickListener(v -> signOut());
        editProfileButton.setOnClickListener(v -> enableEditMode());
        cancelButton.setOnClickListener(v -> {
            disableEditMode();
            loadUserProfile(userId); // Reload the original data
        });
    }

    private void showImagePickerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Image Source");
        builder.setItems(new String[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        openGallery();
                        break;
                    case 1:
                        openCamera();
                        break;
                }
            }
        });
        builder.show();
    }

    private void openGallery() {
        mGetContent.launch("image/*");
    }

    private void openCamera() {
        selectedImageUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new android.content.ContentValues());
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri);
        mTakePhoto.launch(takePictureIntent);
    }

    private void showChangePasswordDialog() {
        DialogFragment changePasswordDialog = new ChangePasswordDialogFragment();
        changePasswordDialog.show(getChildFragmentManager(), "ChangePasswordDialogFragment");
    }

    private void loadUserProfile(String userId) {
        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        user = document.toObject(User.class);
                        if (user != null) {
                            firstNameEditText.setText(user.getFirstName());
                            lastNameEditText.setText(user.getLastName());
                            emailEditText.setText(user.getEmail());
                            phoneEditText.setText(user.getPhoneNumber());
                            // Set the blood type in the dropdown
                            String bloodType = user.getBloodType();
                            if (bloodType != null && !bloodType.isEmpty()) {
                                bloodTypeDropdown.setText(bloodType, false);
                            } else {
                                bloodTypeDropdown.setText("Unknown", false);
                            }
                            enableNotificationsSwitch.setChecked(user.isNotificationEnabled());
                            enablePushNotificationsSwitch.setChecked(user.isPushNotificationsEnabled());
                            userTypeText.setText("User Type: " + user.getUserType());

                            // Load the profile image if available
                            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
                                try {
                                    byte[] decodedString = Base64.decode(user.getProfilePictureUrl(), Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    profileImageView.setImageBitmap(decodedByte);
                                } catch (Exception e) {
                                    Log.e("ProfileFragment", "Error decoding Base64 string", e);
                                    profileImageView.setImageResource(R.drawable.ic_default_avatar);
                                }
                            } else {
                                profileImageView.setImageResource(R.drawable.ic_default_avatar);
                            }
                        }
                    } else {
                        Log.d("ProfileFragment", "No such document");
                    }
                } else {
                    Log.d("ProfileFragment", "get failed with ", task.getException());
                }
            }
        });
    }

    private void saveProfileChanges() {
        String base64Image = null;
        if (selectedImageUri != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                base64Image = ImageHelper.bitmapToBase64(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        updateUserData(base64Image);
    }

    private void updateUserData(String base64Image) {
        Map<String, Object> updatedFields = new HashMap<>();

        if (!firstNameEditText.getText().toString().equals(user.getFirstName())) {
            updatedFields.put("firstName", firstNameEditText.getText().toString());
        }
        if (!lastNameEditText.getText().toString().equals(user.getLastName())) {
            updatedFields.put("lastName", lastNameEditText.getText().toString());
        }
        if (!phoneEditText.getText().toString().equals(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")) {
            updatedFields.put("phoneNumber", phoneEditText.getText().toString());
        }
        if (!bloodTypeDropdown.getText().toString().equals(user.getBloodType() != null ? user.getBloodType() : "")) {
            updatedFields.put("bloodType", bloodTypeDropdown.getText().toString());
        }
        if (enableNotificationsSwitch.isChecked() != user.isNotificationEnabled()) {
            updatedFields.put("notificationEnabled", enableNotificationsSwitch.isChecked());
        }
        if (enablePushNotificationsSwitch.isChecked() != user.isPushNotificationsEnabled()) {
            updatedFields.put("pushNotificationsEnabled", enablePushNotificationsSwitch.isChecked());
        }
        if (base64Image != null) {
            updatedFields.put("profilePictureUrl", base64Image);
        }

        if (!updatedFields.isEmpty()) {
            db.collection("users").document(userId)
                    .update(updatedFields)
                    .addOnSuccessListener(aVoid -> {
                        showSuccessDialog();
                        loadUserProfile(userId);
                        disableEditMode();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            disableEditMode();
        }
    }

    private void enableEditMode() {
        // Make fields editable
        firstNameEditText.setFocusableInTouchMode(true);
        lastNameEditText.setFocusableInTouchMode(true);
        phoneEditText.setFocusableInTouchMode(true);
        bloodTypeDropdown.setEnabled(true);

        // Show/hide buttons
        editProfileButton.setVisibility(View.GONE);
        saveProfileButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);
        changeAvatarButton.setVisibility(View.VISIBLE);
    }

    private void disableEditMode() {
        // Make fields non-editable
        firstNameEditText.setFocusableInTouchMode(false);
        lastNameEditText.setFocusableInTouchMode(false);
        phoneEditText.setFocusableInTouchMode(false);
        bloodTypeDropdown.setEnabled(false);

        // Show/hide buttons
        editProfileButton.setVisibility(View.VISIBLE);
        saveProfileButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        changeAvatarButton.setVisibility(View.GONE);
    }

    private void signOut() {
        mAuth.signOut();
        getActivity().finish();
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Success");
        builder.setMessage("Profile updated successfully!");

        // Set the dialog to not be cancelable
        builder.setCancelable(false);

        // Create the LinearLayout for the custom view
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
        textView.setText("Profile updated successfully!");
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(18);
        textView.setTextColor(Color.BLACK);
        layout.addView(textView);

        // Set the custom view to the dialog
        builder.setView(layout);

        // Set up the OK button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
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

    // TextWatcher to monitor changes in EditText fields
    private TextWatcher profileTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Not used
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Not used
        }

        @Override
        public void afterTextChanged(Editable s) {
            saveProfileButton.setVisibility(View.VISIBLE);
        }
    };
}