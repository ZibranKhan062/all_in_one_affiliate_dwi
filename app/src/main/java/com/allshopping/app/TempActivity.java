package com.allshopping.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class TempActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText referralCodeEditText;
    private EditText referralCountEditText;
    private SwitchMaterial paymentCompleteSwitch;
    private SwitchMaterial userEnabledSwitch;
    private Button saveButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        referralCodeEditText = findViewById(R.id.referralCodeEditText);
        referralCountEditText = findViewById(R.id.referralCountEditText);
        paymentCompleteSwitch = findViewById(R.id.paymentCompleteSwitch);
        userEnabledSwitch = findViewById(R.id.userEnabledSwitch);
        saveButton = findViewById(R.id.saveButton);

        // Set click listener for profile image
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Set click listener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
    }

    public void onChangePictureClicked(View view) {
        openImagePicker();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            Glide.with(this).load(selectedImageUri).into(profileImageView);
        }
    }

    private void saveUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String referralCode = referralCodeEditText.getText().toString().trim();
        String referralCount = referralCountEditText.getText().toString().trim();
        boolean paymentComplete = paymentCompleteSwitch.isChecked();
        boolean userEnabled = userEnabledSwitch.isChecked();

        // Save user data to Realtime Database
        mDatabase.child("users").child(userId).child("name").setValue(name);
        mDatabase.child("users").child(userId).child("email").setValue(email);
        mDatabase.child("users").child(userId).child("phone").setValue(phone);
        mDatabase.child("users").child(userId).child("referralCode").setValue(referralCode);
        mDatabase.child("users").child(userId).child("referralCount").setValue(referralCount);
        mDatabase.child("users").child(userId).child("paymentComplete").setValue(paymentComplete);
        mDatabase.child("users").child(userId).child("userEnabled").setValue(userEnabled);

        // Upload user image to Firebase Storage
        if (selectedImageUri != null) {
            StorageReference imageRef = mStorage.child("userImages").child(userId + ".jpg");
            imageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get the download URL of the uploaded image
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    // Save the image URL to Realtime Database
                                    mDatabase.child("users").child(userId).child("userImage").setValue(imageUrl);
                                    Toast.makeText(TempActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(TempActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(TempActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
        }
    }
}
