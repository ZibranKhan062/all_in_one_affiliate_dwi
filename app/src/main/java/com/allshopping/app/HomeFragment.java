package com.allshopping.app;

import static android.content.Context.MODE_PRIVATE;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.amrdeveloper.lottiedialog.LottieDialog;
import com.allshopping.NotificationActivity;
import com.allshopping.app.Favorite.BookMarksActivity;
import com.allshopping.app.homeelements.HomeAdapter;
import com.allshopping.app.homeelements.HomeModel;
import com.allshopping.app.models.DealsModel;
import com.allshopping.app.models.TempModel;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    final int PERMISSION_REQUEST_CODE = 112;
    List<SliderItem> sliderItemList;
    List<HomeModel> homeModelList;

    RecyclerView recyclerView;

    HomeAdapter homeAdapter;
    DatabaseReference sliderreference;
    private AdView adView;
    RelativeLayout parent;

    SliderAdapterExample sliderAdapter;
    Context mContext;
    ImageView notif_icon;
    SliderView sliderView;

    LinearLayout adContainer;
    //    com.google.android.gms.ads.AdView AdmobView;
    EditText search;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    String ref = "HomeItems";
    ImageView menu_icon;

    public static final String AdMobPREFERENCES = "AdMobPrefs";
    public static final String FacebookPREFERENCES = "FacebookPrefs";
    public static final String isAdEnabledPREFERENCES = "isAdEnabledPrefs";
    String admobAppID;
    String adMobBannerAdsID;
    String admobInterstitialID;
    String facebookBannerAds;
    String facebookInterAds;
    String isAdMobEnabled;
    LinearLayout adViewNew;


    FirebaseRecyclerOptions<TempModel> options;
    FirebaseRecyclerOptions<DealsModel> dealsOptions;
    TempAdapter tempAdapter;
    DatabaseReference databaseReference;

    RecyclerView couponRecyclerView;
    FirebaseRecyclerOptions<CouponChildModel> couponOptions;
    CouponChildAdapter couponChildAdapter;
    TextView couponSeeAllLabel;
    RecyclerView dealsRecyclerView;
    TextView seeAllDeals;
    DealsAdapter dealsAdapter;
    private LinearLayoutManager mLayoutManager;
    LottieDialog lottieDialog;
    Config config;
    private LottieDialog loadingDialog;

    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_home, null);
        getActivity().setTitle("Home");

        config = new Config();
        AudienceNetworkAds.initialize(mContext);

        if (Build.VERSION.SDK_INT > 32) {
            if (!shouldShowRequestPermissionRationale("112")) {
                getNotificationPermission();
            }
        }

        drawerLayout = view.findViewById(R.id.drawer_layout);
        navigationView = view.findViewById(R.id.nav_view);
        adViewNew = view.findViewById(R.id.adViewNew);
        dealsRecyclerView = view.findViewById(R.id.dealsRecyclerView);
        seeAllDeals = view.findViewById(R.id.seeAllDeals);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_bookmarks);

        parent = (RelativeLayout) view.findViewById(R.id.parent);
        search = view.findViewById(R.id.search);
        couponRecyclerView = view.findViewById(R.id.couponRecyclerView);
//        couponRecyclerView .setHasFixedSize(true);
        couponRecyclerView.setNestedScrollingEnabled(false);
        dealsRecyclerView.setNestedScrollingEnabled(false);
        SnapHelper snapHelper = new PagerSnapHelper();
//        snapHelper.attachToRecyclerView(couponRecyclerView);
//        snapHelper.attachToRecyclerView(dealsRecyclerView);


        adContainer = (LinearLayout) view.findViewById(R.id.banner_container);
//        AdmobView = view.findViewById(R.id.AdmobView);
        notif_icon = view.findViewById(R.id.notif_icon);
        menu_icon = view.findViewById(R.id.menu_icon);
        couponSeeAllLabel = view.findViewById(R.id.couponSeeAllLabel);
        notif_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), NotificationActivity.class);
                startActivity(i);
            }
        });

        seeAllDeals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AllDeals.class);
                startActivity(i);
            }
        });

        menu_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT); //Edit Gravity.START need API 14
            }
        });

        readAdsStatus();
        readAdmobAds();
        readFacebookAds();


//        AdmobView.setAdUnitId();


        couponSeeAllLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, CouponCodes.class);
                startActivity(i);

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, SearchActivity.class);
                i.putExtra("HomeSearch", "HOME");
                startActivity(i);
            }
        });


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* text methods */
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                /* text methods */

            }

            @Override
            public void afterTextChanged(Editable editable) {


                if (TextUtils.isEmpty(search.getText().toString())) {
                    loadResources();
                } else {
                    databaseReference = FirebaseDatabase.getInstance().getReference(ref);
                    databaseReference.keepSynced(true);


                    options = new FirebaseRecyclerOptions.Builder<TempModel>()
                            .setQuery(FirebaseDatabase.getInstance().getReference(ref).orderByChild("name").startAt(String.valueOf(editable)).endAt(editable + "\uf8ff"), TempModel.class)
                            .build();

                    recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
                    tempAdapter = new TempAdapter(options, mContext);
                    homeAdapter.startListening();

                    recyclerView.setAdapter(homeAdapter);
                }

            }

        });


        if (!isNetworkAvailable()) {
            Toast.makeText(mContext, "No Internet Connection Available", Toast.LENGTH_LONG).show();

        }


        sliderView = view.findViewById(R.id.imageSlider);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setNestedScrollingEnabled(false);
//        recyclerView.setHasFixedSize(true);


        sliderItemList = new ArrayList<>();
        sliderreference = FirebaseDatabase.getInstance().getReference("SlidingImages");
        sliderreference.keepSynced(true);
        sliderreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.e("Resources", String.valueOf(dataSnapshot));

                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {

                        SliderItem sliderItem = dataSnapshot1.getValue(SliderItem.class);
                        sliderItemList.add(sliderItem);
                    }

                    sliderAdapter = new SliderAdapterExample(getContext(), sliderItemList);

                    sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
                    sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                    sliderView.startAutoCycle();


                    sliderView.setSliderAdapter(new SliderAdapterExample(getContext(), sliderItemList));

                } else {
                    Toast.makeText(getContext(), "No data available !", Toast.LENGTH_SHORT).show();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });


        if (isAdMobEnabled.equalsIgnoreCase("true")) {
            showAdmobAds();
        } else {
            showFbAds();
        }

        loadResources();
        loadCouponItems();
        loadDealsOffers();

        readCurrency();
        return view;
    }

    private void loadCouponItems() {
        couponOptions = new FirebaseRecyclerOptions.Builder<CouponChildModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference("Coupons").limitToFirst(3), CouponChildModel.class)
                .build();
        couponRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        couponChildAdapter = new CouponChildAdapter(couponOptions, mContext);
        couponChildAdapter.startListening();
        couponRecyclerView.setAdapter(couponChildAdapter);
    }


    public void readAdsStatus() {
        SharedPreferences sh = getActivity().getSharedPreferences(isAdEnabledPREFERENCES, MODE_PRIVATE);
        isAdMobEnabled = sh.getString("isAdMobEnabled", "defaultValue");
        Log.e("Ads Status Inside", isAdMobEnabled);
    }

    public void readAdmobAds() {
        SharedPreferences sh = getActivity().getSharedPreferences(AdMobPREFERENCES, MODE_PRIVATE);
        admobAppID = sh.getString("appID", "defaultValue");
        adMobBannerAdsID = sh.getString("bannerAdsID", "defaultValue");
        admobInterstitialID = sh.getString("interstitialID", "defaultValue");
    }

    public void readFacebookAds() {
        SharedPreferences sh = getActivity().getSharedPreferences(FacebookPREFERENCES, MODE_PRIVATE);
        facebookBannerAds = sh.getString("fbBannerID", "defaultValue");
        Log.e("FB Banner", facebookBannerAds);
        facebookInterAds = sh.getString("fbInterID", "defaultValue");
        Log.e("FB Ads", facebookBannerAds + " " + facebookInterAds);

    }


    public void loadResources() {
        databaseReference = FirebaseDatabase.getInstance().getReference("HomeItems");
        databaseReference.keepSynced(true);


        options = new FirebaseRecyclerOptions.Builder<TempModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference("HomeItems"), TempModel.class)
                .build();

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        tempAdapter = new TempAdapter(options, mContext);
        tempAdapter.startListening();
        recyclerView.setAdapter(tempAdapter);
    }


    public void loadDealsOffers() {

        DatabaseReference dealsReference = FirebaseDatabase.getInstance().getReference("Deals");
        dealsReference.keepSynced(true);


        dealsOptions = new FirebaseRecyclerOptions.Builder<DealsModel>()
                .setQuery(FirebaseDatabase.getInstance().getReference("Deals"), DealsModel.class)
                .build();

        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        dealsRecyclerView.setLayoutManager(mLayoutManager);
        dealsAdapter = new DealsAdapter(dealsOptions, mContext);
        dealsAdapter.startListening();
        dealsRecyclerView.setAdapter(dealsAdapter);
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showFbAds() {
        Log.e("fb ads enabled", "true");
        adView = new AdView(mContext, facebookBannerAds, AdSize.BANNER_HEIGHT_50);


        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        adView.loadAd();
        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                /*
                error handling
                 */
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        };

        // Request an ad
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());
    }

    public void showAdmobAds() {
        com.google.android.gms.ads.AdView mAdView = new com.google.android.gms.ads.AdView(getActivity());
        mAdView.setAdSize(com.google.android.gms.ads.AdSize.BANNER);
        mAdView.setAdUnitId(adMobBannerAdsID);
        Log.e("Ad Unit is", adMobBannerAdsID);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        if (mAdView.getAdSize() != null || mAdView.getAdUnitId() != null)
            mAdView.loadAd(adRequest);
        // else Log state of adsize/adunit
        adViewNew.addView(mAdView);
//        ((LinearLayout)findViewById(R.id.adView)).addView(mAdView);
    }

    @Override
    public void onStart() {
        super.onStart();
        tempAdapter.startListening();
        couponChildAdapter.startListening();
        dealsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        tempAdapter.stopListening();
        couponChildAdapter.stopListening();
        dealsAdapter.stopListening();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_bookmarks) {
            Intent intents = new Intent(getActivity(), BookMarksActivity.class);
            startActivity(intents);
            drawerLayout.closeDrawers();
        }


        if (id == R.id.nav_videos) {
            Intent intents = new Intent(getActivity(), VideoActivity.class);
            startActivity(intents);
            drawerLayout.closeDrawers();
        }


        if (id == R.id.nav_coupon) {
            Intent intents = new Intent(getActivity(), CouponCodes.class);
            startActivity(intents);
            drawerLayout.closeDrawers();
        }

        if (id == R.id.nav_promote) {
            showCustomDialog();
            drawerLayout.closeDrawers();
        }


        if (id == R.id.nav_referEarn) {
            // Inflate the custom dialog layout
            View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_refer_earn_dialog, null);

            // Create the custom dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialogView);
            AlertDialog dialog = builder.create();
            // Set click listener for the OK button
            Button okButton = dialogView.findViewById(R.id.okButton);
            okButton.setOnClickListener(v -> dialog.dismiss());
            // Show the custom dialog
            dialog.show();

            drawerLayout.closeDrawers();
        }


        if (id == R.id.nav_rateApp) {
            Intent rateInt = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName()));
            startActivity(rateInt);
            drawerLayout.closeDrawers();

        }

        if (id == R.id.nav_shareApp) {

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

            drawerLayout.closeDrawers();
        }

        if (id == R.id.nav_more_apps) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:" + getResources().getString(R.string.more_apps))));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=" + getResources().getString(R.string.more_apps))));
            }
            drawerLayout.closeDrawers();
        }

        if (id == R.id.instagram) {
            Uri uri = Uri.parse("http://instagram.com/_u/" + getResources().getString(R.string.instagram));
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/" + getResources().getString(R.string.instagram))));
            }

            drawerLayout.closeDrawers();
        }

        if (id == R.id.twitter) {

            Intent intent = null;
            try {
                // get the Twitter app if possible
                getActivity().getPackageManager().getPackageInfo("com.twitter.android", 0);
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=" + getResources().getString(R.string.twitter)));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } catch (Exception e) {
                // no Twitter app, revert to browser
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + getResources().getString(R.string.twitter)));
            }
            startActivity(intent);
            drawerLayout.closeDrawers();

        }
        if (id == R.id.facebook) {
            Uri uri = Uri.parse("https://www.facebook.com/" + getResources().getString(R.string.facebook_page));
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            drawerLayout.closeDrawers();
        }
        if (id == R.id.email_us) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.email)});
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.emailSubject));
            intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.emailBodyText));
            try {
                startActivity(Intent.createChooser(intent, "Send mail"));
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers();
        }
        return true;
    }


    private void showCustomDialog() {
        ViewGroup viewGroup = getView().findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.my_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);

        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button buttonOk = alertDialog.findViewById(R.id.buttonOk);

        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + getResources().getString(R.string.email)));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Promote My App");

                try {
                    startActivity(Intent.createChooser(emailIntent, "Send email using..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "No email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // allow
                    Log.e("Permission", "allowed");

                } else {
                    //deny
                    Log.e("Permission", "denied");

                }

        }

    }

    private void readCurrency() {

        Config.showLoadingDialog(mContext);
//        LottieDialog loadingDialog = config.showLoadingDialog(mContext);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("currencies");
        // Initialize SharedPreferences
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("currency", MODE_PRIVATE);
        // Read the selected currency from Firebase Realtime Database
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Config.hideLoadingDialog();
                if (snapshot.exists()) {
                    // Hide loading dialog
                    String selectedCurrency = snapshot.getValue(String.class);
                    Log.e("currency", "" + selectedCurrency);
                    // Save the selected currency in SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("selectedCurrency", selectedCurrency);
                    editor.apply();
                    Log.e("Currency", "saved");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(mContext, "Currency Error : " + error.getMessage(), Toast.LENGTH_SHORT).show();
                // Hide loading dialog
                Config.hideLoadingDialog();
            }
        });
    }

}
