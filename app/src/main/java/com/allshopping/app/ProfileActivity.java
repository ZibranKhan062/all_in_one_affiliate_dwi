package com.allshopping.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class ProfileActivity extends AppCompatActivity {
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
    private ProgressBar progressBar;
    private Uri selectedImageUri;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Find the MaterialToolbar by its ID
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable the back button in the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase components
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance().getReference();
        progressBar = findViewById(R.id.progressBar);
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

        // Fetch user data from the database
        fetchUserData();
    }

    private void fetchUserData() {
        String userId = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String referralCode = dataSnapshot.child("referralCode").getValue(String.class);
                    long referralCount = dataSnapshot.child("referralCount").getValue(Long.class);
                    String isPaymentComplete = dataSnapshot.child("isPaymentComplete").getValue(String.class);
                    String isUserEnabled = dataSnapshot.child("isUserEnabled").getValue(String.class);
                    String imageUrl = dataSnapshot.child("userImage").getValue(String.class);

                    nameEditText.setText(name);
                    emailEditText.setText(email);
                    phoneEditText.setText(phone);
                    referralCodeEditText.setText(referralCode);
                    referralCountEditText.setText(String.valueOf(referralCount));
                    paymentCompleteSwitch.setChecked(isPaymentComplete != null && isPaymentComplete.equals("Yes"));
                    userEnabledSwitch.setChecked(isUserEnabled != null && isUserEnabled.equals("Yes"));

                    if (imageUrl != null) {
                        Glide.with(ProfileActivity.this)
                                .load(imageUrl)
                                .timeout(30000) // Set the timeout to 30 seconds (adjust as needed)
                                .error(R.drawable.ic_person)
                                .into(profileImageView);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProfileActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
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

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Save user data to Realtime Database
        mDatabase.child("Users").child(userId).child("name").setValue(name)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
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
                                                    mDatabase.child("Users").child(userId).child("userImage").setValue(imageUrl)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    // Hide progress
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Toast.makeText(ProfileActivity.this, "User data and image saved successfully", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Hide progress
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Toast.makeText(ProfileActivity.this, "Failed to save image URL", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Hide progress
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(ProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                        else {
                            // Hide progress
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Hide progress
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
