package com.allshopping.app;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MyRewards extends AppCompatActivity {
    RecyclerView rewardRecyclerView;
    FloatingActionButton reward_history;
    Button claim_reward;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_rewards);
        rewardRecyclerView = findViewById(R.id.rewardRecyclerView);
        reward_history = findViewById(R.id.reward_history);
        claim_reward = findViewById(R.id.claim_reward);
    }
}