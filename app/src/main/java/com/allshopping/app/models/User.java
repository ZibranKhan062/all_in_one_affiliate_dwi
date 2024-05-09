package com.allshopping.app.models;

public class User {
    private String email;
    private String name;
    private String phone;
    private String referralCode;
    private int referralCount;
    private int rewardsPoints;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String name, String phone, String referralCode, int referralCount, int rewardsPoints) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.referralCode = referralCode;
        this.referralCount = referralCount;
        this.rewardsPoints = rewardsPoints;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public int getReferralCount() {
        return referralCount;
    }

    public void setReferralCount(int referralCount) {
        this.referralCount = referralCount;
    }

    public int getRewardsPoints() {
        return rewardsPoints;
    }

    public void setRewardsPoints(int rewardsPoints) {
        this.rewardsPoints = rewardsPoints;
    }
}
