package com.allshopping.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.allshopping.app.LoginSignup.RegisterModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity implements PaymentResultWithDataListener {


    private Button buttonPay;
    private static final String TAG = "PaymentActivity";

    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogs;
    private String videoUrl; // The YouTube video URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_payment);

        buttonPay = findViewById(R.id.buttonPay);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialize the progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference paymentsRef = FirebaseDatabase.getInstance().getReference("Payments");
        paymentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String razorpayKey = dataSnapshot.child("razorpayKey").getValue(String.class);
                String amount = dataSnapshot.child("amount").getValue(String.class);
                String currency = dataSnapshot.child("currency").getValue(String.class);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("razorpayKey", razorpayKey);
                editor.putString("amount", amount);
                editor.putString("currency", currency);
                editor.apply();

                // Dismiss the progress dialog
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Dismiss the progress dialog
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Toast.makeText(PaymentActivity.this, "Failed to fetch payment related data ", Toast.LENGTH_SHORT).show();
            }
        });


        // Set click listener for the pay button
        buttonPay.setOnClickListener(v ->

                startPayment());


        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.requestFocus();
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);


        // Get a reference to the video node in the Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("video");

// Add a ValueEventListener to the video reference
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get the video ID
                String videoId = dataSnapshot.getValue(String.class);
                Log.e("Video ID", videoId);

                // Load the YouTube video in the WebView
                webView.loadData("<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/" + videoId + "?autoplay=1\" frameborder=\"0\" allowfullscreen></iframe>", "text/html", "utf-8");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
            }
        });


    }


    private void startPayment() {
        // Get the current user's UID
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get a reference to the user's data in the database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        // Retrieve payment-related data from SharedPreferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String razorpayKey = sharedPreferences.getString("razorpayKey", "");
        String amount = sharedPreferences.getString("amount", "");
        String currency = sharedPreferences.getString("currency", "");

        // Retrieve the user's data
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the user's data
                    RegisterModel registerModel = dataSnapshot.getValue(RegisterModel.class);

                    if (registerModel != null) {
                        String name = registerModel.getName();
                        String email = registerModel.getEmail();
                        String contact = registerModel.getPhone();

                        // Create a Razorpay checkout object
                        Checkout checkout = new Checkout();
                        checkout.setKeyID(razorpayKey);

                        // Set the payment details
                        JSONObject options = new JSONObject();
                        try {
                            options.put("name", name);
                            options.put("description", "All In One Shopping Premium Subscription");
                            options.put("currency", currency);
                            options.put("amount", amount); // Amount in paise (e.g., 600 INR = 60000 paise)
                            options.put("prefill.email", email);
                            options.put("prefill.contact", contact);

                            checkout.open(PaymentActivity.this, options);
                        } catch (Exception e) {
                            Log.e(TAG, "Error in starting Razorpay Checkout", e);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error in retrieving user data", databaseError.toException());
            }
        });

    }


    @Override
    public void onPaymentSuccess(String razorpayPaymentID, PaymentData paymentData) {
        String paymentId = paymentData.getPaymentId();
        String signature = paymentData.getSignature();
        String orderId = paymentData.getOrderId();
        // Payment successful, update the user's payment status in the database
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userUid);

        // Get the current date and time
        String paymentDate = getCurrentDateTime();

        // Update the user's payment status and other payment details
        userRef.child("isPaymentComplete").setValue("Yes");
        userRef.child("paymentDate").setValue(paymentDate);
        userRef.child("paymentID").setValue(paymentId);
        userRef.child("signature").setValue(signature);
        userRef.child("orderId").setValue(orderId);
        userRef.child("isUserEnabled").setValue("Yes");
        // Show a success dialog
        showPaymentStatusDialog("Payment Successful", "Your payment was processed successfully.");
        // Show a success message or navigate to the next screen
        Toast.makeText(this, "Payment successful", Toast.LENGTH_SHORT).show();
        // Navigate to the next screen or perform any other desired action
    }


    @Override
    public void onPaymentError(int code, String description, PaymentData paymentData) {
        // Payment failed or canceled, show an error message
        Toast.makeText(this, "Payment failed or canceled: " + description, Toast.LENGTH_SHORT).show();
        // Payment failed or canceled, show an error dialog
        showPaymentStatusDialog("Payment Failed", "Your payment failed or was canceled. Please try again.");
    }


    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void showPaymentStatusDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle OK button click
                    if (title.equals("Payment Successful")) {
                        Intent intent = new Intent(PaymentActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Restart the payment process or perform any other desired action
                        // ...
                    }
                })
                .setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
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


                        startActivity(new Intent(PaymentActivity.this, MainActivity.class));
                        // Dismiss the dialog after the check is complete
                        if (progressDialogs.isShowing()) {
                            progressDialogs.dismiss();
                        }
                        finish();
                    } else {
                        // Payment is not done, start the payment process
                        startPayment();
                    }
                } else {
                    // Payment status is not available, start the payment process
                    startPayment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors
                Log.e(TAG, "Error checking payment status", databaseError.toException());
                // Start the payment process as a fallback
                startPayment();
            }
        });
    }
}