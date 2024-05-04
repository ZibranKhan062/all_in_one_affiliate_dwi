package com.allshopping.app.LoginSignup;

public class RegisterModel {
    private String name;
    private String email;
    private String phone;
    private String referralCode;
    private long referralCount;

    public RegisterModel() {
    }

    public RegisterModel(String name, String email, String phone, String referralCode) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.referralCode = referralCode;
        this.referralCount = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getReferralCode() {
        return referralCode;
    }

    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }

    public long getReferralCount() {
        return referralCount;
    }

    public void setReferralCount(long referralCount) {
        this.referralCount = referralCount;
    }
}
