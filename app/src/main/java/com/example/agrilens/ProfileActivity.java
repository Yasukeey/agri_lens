package com.example.agrilens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.agrilens.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private ShapeableImageView profileImage;
    private TextView userEmail;
    private EditText userName, userPassword;
    private Button changeProfilePicture, updateProfileButton;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        profileImage = findViewById(R.id.profile_image);
        userEmail = findViewById(R.id.user_email);
        userName = findViewById(R.id.user_name);
        userPassword = findViewById(R.id.user_password);
        changeProfilePicture = findViewById(R.id.change_profile_picture);
        updateProfileButton = findViewById(R.id.update_profile_button);

        // Load current user info
        if (user != null) {
            userEmail.setText(user.getEmail());

            // Load profile image if available
            databaseReference.child(user.getUid()).child("profilePicture").get().addOnSuccessListener(snapshot -> {
                String base64Image = snapshot.getValue(String.class);
                if (base64Image != null) {
                    // Decode the Base64 string to Bitmap and load it into the ImageView
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    profileImage.setImageBitmap(decodedByte);
                } else {
                    profileImage.setImageResource(R.drawable.baseline_account_circle_24); // Placeholder
                }
            });
        }

        // Change profile picture
        changeProfilePicture.setOnClickListener(v -> openGallery());

        // Update profile info
        updateProfileButton.setOnClickListener(v -> updateProfile());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);  // Set the selected image URI
        }
    }

    private void updateProfile() {
        if (user == null) return;

        String newName = userName.getText().toString().trim();
        String newPassword = userPassword.getText().toString().trim();

        // Update username if it is not empty
        if (!newName.isEmpty()) {
            user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(newName).build())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Update the username in the Firebase Realtime Database
                            updateDatabaseName(newName);
                            user.reload().addOnCompleteListener(reloadTask -> {
                                if (reloadTask.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Error reloading user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(ProfileActivity.this, "Error updating name", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Update password if it is not empty
        if (!newPassword.isEmpty()) {
            user.updatePassword(newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this, "Error updating password", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Upload profile picture if a new image is selected
        if (imageUri != null) {
            uploadProfilePicture();
        }
    }

    private void updateDatabaseName(String newName) {
        // Update the user's name in the Realtime Database
        databaseReference.child(user.getUid()).child("userName").setValue(newName)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Name updated in database", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error updating name in database", Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadProfilePicture() {
        if (imageUri != null) {
            try {
                // Convert image to Bitmap
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));

                // Convert Bitmap to Base64 string
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] byteArray = baos.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

                // Save Base64 string to Firebase Realtime Database
                databaseReference.child(user.getUid()).child("profilePicture").setValue(encodedImage)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                            Glide.with(ProfileActivity.this).load(bitmap).into(profileImage);
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Error uploading profile picture", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error processing the image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
