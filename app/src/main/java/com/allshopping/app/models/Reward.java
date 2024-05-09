package com.allshopping.app.models;

public class Reward {
    private String email;
    private String name;
    private int rewardPoints;

    public Reward() {
        // Default constructor required for calls to DataSnapshot.getValue(Reward.class)
    }

    public Reward(String email, String name, int rewardPoints) {
        this.email = email;
        this.name = name;
        this.rewardPoints = rewardPoints;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }
}

