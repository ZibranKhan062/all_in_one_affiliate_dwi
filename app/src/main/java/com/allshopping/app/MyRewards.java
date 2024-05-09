package com.allshopping.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.allshopping.app.models.Reward;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyRewards extends AppCompatActivity {
    RecyclerView rewardRecyclerView;
    FloatingActionButton reward_history;
    Button claim_reward;
    private int totalRewardPoints = 0;


    private DatabaseReference mDatabase;
    private List<Reward> rewardList = new ArrayList<>();
    private RewardAdapter rewardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rewards);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Rewards List");  // Set the title of the toolbar
        }


        rewardRecyclerView = findViewById(R.id.rewardRecyclerView);
        reward_history = findViewById(R.id.reward_history);
        claim_reward = findViewById(R.id.claim_reward);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        rewardAdapter = new RewardAdapter(rewardList);
        rewardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        rewardRecyclerView.setAdapter(rewardAdapter);

        loadRewardsFromFirebase();
        claim_reward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalRewardPoints >= 100) {
                    sendRewardClaimEmail();
                }
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();  // Close this activity and return to the previous activity (or close the app if this is the bottom activity on the stack)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadRewardsFromFirebase() {
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                totalRewardPoints = 0; // Reset the total reward points
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String email = (String) userSnapshot.child("email").getValue();
                    String name = (String) userSnapshot.child("name").getValue();
                    for (DataSnapshot referralSnapshot : userSnapshot.child("Referrals").getChildren()) {
                        int rewardPoints = referralSnapshot.child("rewardPoints").getValue(Integer.class);
                        totalRewardPoints += rewardPoints; // Increment the total reward points
                        Reward reward = new Reward(email, name, rewardPoints);
                        rewardList.add(reward);
                    }
                }
                rewardAdapter.notifyDataSetChanged();
                updateTotalRewardPointsTextView(); // Update the TextView with the total reward points
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                Toast.makeText(MyRewards.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalRewardPointsTextView() {
        TextView textViewTotalRewardPoint = findViewById(R.id.textView_total_reward_point_fragment);
        textViewTotalRewardPoint.setText(String.valueOf(totalRewardPoints));

        Button claimRewardButton = findViewById(R.id.claim_reward);
        if (totalRewardPoints < 100) {
            claimRewardButton.setEnabled(false);
            claimRewardButton.setBackgroundResource(R.drawable.rounded_disabled);
        } else {
            claimRewardButton.setEnabled(true);
            claimRewardButton.setBackgroundResource(R.drawable.rounded);
        }
    }

    private void sendRewardClaimEmail() {
        String emailAddress = getString(R.string.email);
        String subject = "Reward Claim Request";
        String message = "Hello,\n\nI would like to claim my reward. My total reward points are: " + totalRewardPoints + ".\n\nThank you.";

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

}


