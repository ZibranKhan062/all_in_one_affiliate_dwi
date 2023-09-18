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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.allshopping.app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        firebaseAuth = FirebaseAuth.getInstance();

//        if (firebaseAuth.getCurrentUser() != null) {
//            Intent intent = new Intent(RegisterActivity.this, ReferActivity.class);
//            startActivity(intent);
//            finish();
//        }
        setContentView(R.layout.activity_register);
        progressDialog = new ProgressDialog(RegisterActivity.this);
        uName = findViewById(R.id.u_name);
        userEmail = findViewById(R.id.user_email);
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

        progressDialog.setMessage("Registering Please Wait...");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(userEmail.getText().toString().trim(), userPass.getText().toString().trim()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Registration successful, get the UID of the newly registered user
                String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Create a new RegisterModel object to store user data
                RegisterModel registerModel = new RegisterModel(uName.getText().toString().trim(),
                        userEmail.getText().toString().trim(), user_refer.getText().toString().trim());

                // Get a reference to the "Users" node in the database
                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

                // Check if the referral code is present in any other user's data and update the "referralCount" if needed
                updateReferralCountIfPresent(usersRef, userUid, user_refer.getText().toString().trim());


                // Save the user data to the "Users" node with the user UID as the key
                usersRef.child(userUid).setValue(registerModel).addOnCompleteListener(task1 -> {
                    progressDialog.dismiss();

                    if (task1.isSuccessful()) {
                        // Data saved successfully, proceed with other actions
                        uName.setText("");
                        userEmail.setText("");
                        userPass.setText("");
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
                            // Proceed to ReferActivity
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

    private void updateReferralCountIfPresent(DatabaseReference usersRef, String userUid, String referralCode) {
        Query query = usersRef.orderByChild("referralCode").equalTo(referralCode);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String existingUserUid = userSnapshot.getKey();
                        if (!existingUserUid.equals(userUid)) {
                            // Check if the user already has the referralCount field
                            if (userSnapshot.hasChild("referralCount")) {
                                long currentReferralCount = userSnapshot.child("referralCount").getValue(Long.class);
                                long updatedReferralCount = currentReferralCount + 1;
                                userSnapshot.getRef().child("referralCount").setValue(updatedReferralCount);
                            }
//                            else {
//                                // If referralCount field is not present, set it to 1
//                                userSnapshot.getRef().child("referralCount").setValue(1);
//                            }
                        }
                    }
                } else {
                    // No user with the given referral code found, handle this case if needed
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }
}