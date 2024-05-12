package com.allshopping.app.LoginSignup;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.allshopping.app.GoogleUser;
import com.allshopping.app.MainActivity;
import com.allshopping.app.PaymentActivity;
import com.allshopping.app.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText user_pass;
    TextInputEditText user_email;
    TextInputEditText user_refer;
    RelativeLayout login_btn;

    EditText editTextEmail;
    EditText editTextPassword;
    Button btn_login;
    TextView goToRegister;
    TextView gotoSignUp;
    //firebase auth object
    private FirebaseAuth firebaseAuth;
    //progress dialog
    private ProgressDialog progressDialog;


    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            // Email is verified, check payment status
            checkPaymentStatus();
        }
        setContentView(R.layout.activity_login);
//        if (firebaseAuth.getCurrentUser() != null) {
//            Intent i = new Intent(LoginActivity.this, ReferActivity.class);
//            startActivity(i);
//            finish();
//        }
        user_email = findViewById(R.id.user_email);
        user_pass = findViewById(R.id.user_pass);
        login_btn = findViewById(R.id.login_btn);
        gotoSignUp = findViewById(R.id.gotoSignUp);
        user_refer = findViewById(R.id.user_refer);
        progressDialog = new ProgressDialog(this);
        login_btn.setOnClickListener(v -> doLogin());


        gotoSignUp.setOnClickListener(v -> doSignUp());

        // Initialize Firebase Auth and Realtime Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("2329287643-vq1kmdog5h09s9odt8dko7f1mni918r9.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up Google Sign-In button
        RelativeLayout googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    private void doSignUp() {

        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }


    private void doLogin() {
        String email = user_email.getText().toString().trim();
        String password = user_pass.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Logging In ...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        // Check if the user's email is verified
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // Email is verified, check the referral code
                            String referralCode = user_refer.getText().toString().trim();


                            // No referral code provided, proceed to the PaymentActivity
                            Intent intent = new Intent(LoginActivity.this, PaymentActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            // Email is not verified, show a message and ask to verify
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("Email Not Verified")
                                    .setMessage("Your email is not verified. Please verify your email to log in.")
                                    .setPositiveButton("Verify Email", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Send verification email
                                            sendVerificationEmail();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Dismiss the dialog
                                            dialog.dismiss();
                                        }
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                    } else {
                        // Login failed, show an error message
                        Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle("Email Sent")
                            .setMessage("A verification email has been sent. Please check your email and verify your account.")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Dismiss the dialog
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    Log.e("Email", "Failed to send verification email.");
                }
            }
        });
    }

    private void checkPaymentStatus() {
        // Get the current user's UID
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get a reference to the user's data in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        // Retrieve the user's payment status
        userRef.child("isPaymentComplete").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String paymentStatus = dataSnapshot.getValue(String.class);
                    if (paymentStatus != null && paymentStatus.equals("Yes")) {
                        // Payment is done, proceed to MainActivity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // Payment is not done, proceed to PaymentActivity
                        Intent intent = new Intent(LoginActivity.this, PaymentActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    // Payment status is not available, proceed to PaymentActivity
                    Intent intent = new Intent(LoginActivity.this, PaymentActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e("TAG", "Error checking payment status", databaseError.toException());
                // Proceed to PaymentActivity as a fallback
                Intent intent = new Intent(LoginActivity.this, PaymentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();
                            String name = user.getDisplayName();
                            String email = user.getEmail();
                            String mobileNumber = user.getPhoneNumber();

                            if (mobileNumber != null) {
                                saveUserData(userId, name, email, mobileNumber);
                            } else {
                                showMobileNumberDialog(userId, name, email);
                            }

                            Toast.makeText(LoginActivity.this, "Google sign in success", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void showMobileNumberDialog(final String userId, final String name, final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Mobile Number");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String mobileNumber = input.getText().toString();
                saveUserData(userId, name, email, mobileNumber);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveUserData(String userId, String name, String email, String mobileNumber) {
        GoogleUser user = new GoogleUser(name, email, mobileNumber);
        mDatabase.child("users/googleUsers").child(userId).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //show toast that task is successful
                            Toast.makeText(LoginActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                            // User data saved successfully
                            Log.d("TAG", "User data saved successfully");
                            //also show toast
                            Toast.makeText(LoginActivity.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                            // Navigate to the next activity or show a success message
                        } else {
                            // Failed to save user data
                            Log.e("TAG", "Failed to save user data", task.getException());
                            Toast.makeText(LoginActivity.this, "User data saved failed", Toast.LENGTH_SHORT).show();

                            // Show an error message
                        }
                    }
                });
    }


}