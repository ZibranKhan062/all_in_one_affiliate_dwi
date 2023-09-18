package com.allshopping.app;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

public class DetailsDeals extends AppCompatActivity {

    ImageView prodImage;
    TextView prodName, sellingPrice, discountedPrice, percentOff, prodDesc;
    Button grabDealButton;
    Intent intent;
    String getProdName, getProdImage, getProdDesc, getProdLink, getProdDiscountPrice, getProdSellingPrice, getProdPercentOff, getVidID;


    protected RelativeLayout relativeLayoutOverYouTubeThumbnailView;
    YouTubeThumbnailView youTubeThumbnailView;
    public ImageView playButton;
    CardView card1;
    TextView videoLabel;
    ImageButton shareDeal, shareWADeal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_deals);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        intent = getIntent();
        getProdName = intent.getStringExtra("ProdName");
        getProdImage = intent.getStringExtra("ProdImage");
        getProdDesc = intent.getStringExtra("ProdDesc");
        getProdLink = intent.getStringExtra("ProdLink");
        getProdDiscountPrice = intent.getStringExtra("ProdDiscountPrice");
        getProdSellingPrice = intent.getStringExtra("ProdSellingPrice");
        getProdPercentOff = intent.getStringExtra("ProdPercentOff");
        getVidID = intent.getStringExtra("VidID");
        Log.e("VID ID", getVidID);

        prodImage = findViewById(R.id.prodImage);
        prodName = findViewById(R.id.prodName);
        sellingPrice = findViewById(R.id.sellingPrice);
        discountedPrice = findViewById(R.id.discountedPrice);
        percentOff = findViewById(R.id.percentOff);
        prodDesc = findViewById(R.id.prodDesc);
        grabDealButton = findViewById(R.id.grabDealButton);
        playButton = findViewById(R.id.btnYoutube_player);
        relativeLayoutOverYouTubeThumbnailView = findViewById(R.id.relativeLayout_over_youtube_thumbnail);
        youTubeThumbnailView = findViewById(R.id.youtube_thumbnail);
        card1 = findViewById(R.id.card1);
        videoLabel = findViewById(R.id.videoLabel);
        shareDeal = findViewById(R.id.shareDeal);
        shareWADeal = findViewById(R.id.shareWADeal);

        Glide.with(DetailsDeals.this).load(getProdImage).placeholder(R.drawable.placeholder).error(R.drawable.placeholder).into(prodImage);
        prodName.setText(getProdName.trim());
        String selectedCurrency = Config.getSelectedCurrency(DetailsDeals.this);
        sellingPrice.setText(selectedCurrency + "" + getProdSellingPrice.trim());
        sellingPrice.setPaintFlags(sellingPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        discountedPrice.setText(selectedCurrency + "" + getProdDiscountPrice.trim());
        percentOff.setText(getProdPercentOff.trim() + " % off");
        prodDesc.setText(getProdDesc.trim());

        if (getVidID.equalsIgnoreCase("n/a")) {
            card1.setVisibility(View.GONE);
            videoLabel.setVisibility(View.GONE);
        }


        final YouTubeThumbnailLoader.OnThumbnailLoadedListener onThumbnailLoadedListener = new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
            @Override
            public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                /*error handling*/
            }

            @Override
            public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                youTubeThumbnailView.setVisibility(View.VISIBLE);
                relativeLayoutOverYouTubeThumbnailView.setVisibility(View.VISIBLE);
            }
        };
        youTubeThumbnailView.initialize(Config.getApiKey(), new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, final YouTubeThumbnailLoader youTubeThumbnailLoader) {

                youTubeThumbnailLoader.setVideo(getVidID);
                youTubeThumbnailLoader.setOnThumbnailLoadedListener(onThumbnailLoadedListener);
                youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {

                        youTubeThumbnailView.setVisibility(View.VISIBLE);
                        relativeLayoutOverYouTubeThumbnailView.setVisibility(View.VISIBLE);
                        youTubeThumbnailLoader.release();
                    }

                    @Override
                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                        /*error handling */
                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(DetailsDeals.this, "Details : " + youTubeInitializationResult, Toast.LENGTH_LONG).show();
            }
        });
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = YouTubeStandalonePlayer.createVideoIntent(DetailsDeals.this, Config.getApiKey(), getVidID, 100,
                        true,
                        false);
                startActivity(intent);
            }
        });


        grabDealButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeActionOnDeal();
            }
        });


        shareDeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = "Check this out: " + prodName.getText().toString().trim() + " " + getProdLink;
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        shareWADeal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("text/plain");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_TEXT, "Check this out: " + prodName.getText().toString().trim() + " " + getProdLink);
                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(DetailsDeals.this,
                            "Whatsapp have not been installed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void takeActionOnDeal() {

        Intent i = new Intent(DetailsDeals.this, DealsWebView.class);
        i.putExtra("Link", getProdLink);
        i.putExtra("Title", getProdName);
        startActivity(i);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}