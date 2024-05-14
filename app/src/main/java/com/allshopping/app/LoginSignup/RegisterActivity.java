package com.allshopping.app.LoginSignup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.allshopping.app.R;
import com.allshopping.app.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText uName;
    TextInputEditText userEmail;
    TextInputEditText userPass;
    TextInputEditText user_refer;
    RelativeLayout loginBtn;
    TextView goToSignIn;
    ProgressDialog progressDialog;
    FirebaseAuth firebaseAuth;
    TextInputEditText userPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        firebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_register);
        progressDialog = new ProgressDialog(RegisterActivity.this);
        uName = findViewById(R.id.u_name);
        userEmail = findViewById(R.id.user_email);
        userPhone = findViewById(R.id.user_phone);

        userPass = findViewById(R.id.user_pass);
        user_refer = findViewById(R.id.user_refer);
        loginBtn = findViewById(R.id.login_btn);
        goToSignIn = findViewById(R.id.goToSignIn);

        goToSignIn.setOnClickListener(v -> {
            Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        });

        loginBtn.setOnClickListener(v -> registerUser());

    }


    private void registerUser() {
        // Checking if email and passwords are empty
        if (TextUtils.isEmpty(uName.getText().toString().trim())) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(userEmail.getText().toString().trim())) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(userPass.getText().toString().trim())) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(userPhone.getText().toString().trim())) {
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();

        // Check the referral code
        String referralCode = user_refer.getText().toString().trim();
        if (!TextUtils.isEmpty(referralCode)) {
            // Referral code is available, check if it is valid
            checkReferralCode(referralCode);
        } else {
            // Referral code is not available, proceed with registration
            proceedWithRegistration();
        }
    }


    private void checkReferralCode(String referralCode) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = usersRef.orderByChild("referralCode").equalTo(referralCode);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Referral code is valid, proceed with registration
                    proceedWithRegistration();
                } else {
                    // Referral code is invalid, show an error message and stop registration
                    progressDialog.dismiss();
                    Toast.makeText(RegisterActivity.this, "Invalid referral code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                progressDialog.dismiss();
                Log.e("ReferralCodeCheck", "Error checking referral code", databaseError.toException());
            }
        });
    }

    private void proceedWithRegistration() {
        firebaseAuth.createUserWithEmailAndPassword(userEmail.getText().toString().trim(), userPass.getText().toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration successful, get the UID of the newly registered user
                        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        // Create a new RegisterModel object to store user data
                        RegisterModel registerModel = new RegisterModel(uName.getText().toString().trim(),
                                userEmail.getText().toString().trim(), userPhone.getText().toString().trim(),
                                ""); // Set referral code as empty for now

                        // Get a reference to the "Users" node in the database
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

                        // Save the user data to the "Users" node with the user UID as the key
                        usersRef.child(userUid).setValue(registerModel).addOnCompleteListener(task1 -> {
                            progressDialog.dismiss();

                            if (task1.isSuccessful()) {
                                // Data saved successfully, update the referral count if a referral code is provided
                                String referralCode = user_refer.getText().toString().trim();
                                if (!TextUtils.isEmpty(referralCode)) {
                                    updateReferralCount(referralCode, userUid);
                                }
                                // Proceed with other actions
//                                uName.setText("");
//                                userEmail.setText("");
//                                userPass.setText("");
                                sendVerificationEmail();
                            } else {
                                Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void updateReferralCount(String referralCode, String referredUserUid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = usersRef.orderByChild("referralCode").equalTo(referralCode);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Referral code is valid, update the referral count
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String referringUserUid = userSnapshot.getKey();
                        DatabaseReference referralsRef = usersRef.child(referringUserUid).child("Referrals");

                        // Store the referred user's details under the "Referrals" node
                        DatabaseReference referredUserRef = referralsRef.child(referredUserUid);
                        referredUserRef.child("name").setValue(uName.getText().toString().trim());
                        referredUserRef.child("email").setValue(userEmail.getText().toString().trim());
                        referredUserRef.child("rewardPoints").setValue(10); // Example: Add 10 reward points
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e("ReferralCountUpdate", "Error updating referral count", databaseError.toException());
            }
        });
    }


    private void rewardReferringUser(String referringUserUid) {
        // Implement your reward logic here
        // For example, you can update the referring user's rewards points or balance in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(referringUserUid);

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                User user = mutableData.getValue(User.class);
                if (user == null) {
                    return Transaction.success(mutableData);
                }

                // Update the user's rewards points or balance
                int currentRewardsPoints = user.getRewardsPoints();
                user.setRewardsPoints(currentRewardsPoints + 10); // Example: Add 10 rewards points

                mutableData.setValue(user);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("ReferralReward", "Error updating rewards points", databaseError.toException());
                } else if (committed) {
                    Log.d("ReferralReward", "Rewards points updated successfully");
                }
            }
        });
    }

    private void sendVerificationEmail() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("Email Sent").setMessage("A Verification email has been sent. Kindly check your email for the verification link")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).setCancelable(false).show();
                } else {
                    Log.e("Email", "Failed to send.");
                }
            }
        });
    }


}
