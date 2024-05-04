package com.allshopping.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.allshopping.app.LoginSignup.LoginActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SplashActivity extends AppCompatActivity {


    ProgressBar progressBar;
    DatabaseReference adsReference, isAdmobEnabledReference;
    public static final String AdMobPREFERENCES = "AdMobPrefs";
    public static final String FacebookPREFERENCES = "FacebookPrefs";
    public static final String isAdEnabledPREFERENCES = "isAdEnabledPrefs";
    SharedPreferences admobPreferences, facebookPreferences, isAdEnabledPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        progressBar = findViewById(R.id.progressBar);
        admobPreferences = getSharedPreferences(AdMobPREFERENCES, Context.MODE_PRIVATE);
        facebookPreferences = getSharedPreferences(FacebookPREFERENCES, Context.MODE_PRIVATE);
        isAdEnabledPreferences = getSharedPreferences(isAdEnabledPREFERENCES, Context.MODE_PRIVATE);
        getAdsStatus();





    }

    private void getAdsStatus() {
//                isAdmobEnabledReference.keepSynced(true);
        isAdmobEnabledReference = FirebaseDatabase.getInstance().getReference("Ads");
        isAdmobEnabledReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    String isAdmobEnabled = dataSnapshot.child("isAdmobEnabled").getValue(String.class);
                    if (isAdmobEnabled != null) {
                        Log.e("isAdmobEnabled", isAdmobEnabled);

                        // Store the value in SharedPreferences
                        SharedPreferences.Editor editor = isAdEnabledPreferences.edit();
                        editor.putString("isAdMobEnabled", isAdmobEnabled);
                        editor.apply();
                        getAdMobAds();
                    } else {
                        // Handle case where data is null
                        Toast.makeText(SplashActivity.this, "Admob status data is null!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle case where data doesn't exist
                    Toast.makeText(SplashActivity.this, "No data available!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(SplashActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getAdMobAds() {
        adsReference = FirebaseDatabase.getInstance().getReference("Ads").child("Admob");
        adsReference.keepSynced(true);
        adsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String appID = dataSnapshot.child("appID").getValue(String.class);
                    String bannerAdsID = dataSnapshot.child("bannerAdsID").getValue(String.class);
                    String interstitialID = dataSnapshot.child("interstitialID").getValue(String.class);

                    if (appID != null && bannerAdsID != null && interstitialID != null) {
                        SharedPreferences.Editor editor = admobPreferences.edit();

                        editor.putString("appID", appID);
                        editor.putString("bannerAdsID", bannerAdsID);
                        editor.putString("interstitialID", interstitialID);
                        editor.apply();
                        getFacebookAds();
                    } else {
                        // Handle case where some data is null
                        Toast.makeText(SplashActivity.this, "Admob data is incomplete!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle case where data doesn't exist
                    Toast.makeText(SplashActivity.this, "No Admob data available!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(SplashActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFacebookAds() {
        adsReference = FirebaseDatabase.getInstance().getReference("Ads").child("Facebook");
        adsReference.keepSynced(true);
        adsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fbBannerID = dataSnapshot.child("fbBannerID").getValue(String.class);
                    String fbInterID = dataSnapshot.child("fbInterID").getValue(String.class);

                    if (fbBannerID != null && fbInterID != null) {
                        SharedPreferences.Editor editor = facebookPreferences.edit();

                        editor.putString("fbBannerID", fbBannerID);
                        editor.putString("fbInterID", fbInterID);
                        editor.apply();

//                        progressBar.setVisibility(View.GONE);
                        Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        SplashActivity.this.startActivity(mainIntent);
                        SplashActivity.this.finish();
                    } else {
                        // Handle case where some data is null
                        Toast.makeText(SplashActivity.this, "Facebook data is incomplete!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Handle case where data doesn't exist
                    Toast.makeText(SplashActivity.this, "No Facebook data available!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(SplashActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}