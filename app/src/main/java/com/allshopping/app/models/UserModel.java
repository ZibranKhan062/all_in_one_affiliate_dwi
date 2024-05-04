package com.allshopping.app.models;

public class UserModel {
    private String name;
    private String email;
    private String contact;
    private String createdAt;
    private String isActiveLogin;
    private String isPaymentComplete;
    private String isUserEnabled;
    private String password;
    private String paymentDate;
    private String paymentID;
    private String amountPaid;

    // Empty constructor is needed for Firebase
    public UserModel() {
    }

    public UserModel(String name, String email, String contact, String createdAt, String isActiveLogin, String isPaymentComplete, String isUserEnabled, String password, String paymentDate, String paymentID, String amountPaid) {
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.createdAt = createdAt;
        this.isActiveLogin = isActiveLogin;
        this.isPaymentComplete = isPaymentComplete;
        this.isUserEnabled = isUserEnabled;
        this.password = password;
        this.paymentDate = paymentDate;
        this.paymentID = paymentID;
        this.amountPaid = amountPaid;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getIsActiveLogin() {
        return isActiveLogin;
    }

    public void setIsActiveLogin(String isActiveLogin) {
        this.isActiveLogin = isActiveLogin;
    }

    public String getIsPaymentComplete() {
        return isPaymentComplete;
    }

    public void setIsPaymentComplete(String isPaymentComplete) {
        this.isPaymentComplete = isPaymentComplete;
    }

    public String getIsUserEnabled() {
        return isUserEnabled;
    }

    public void setIsUserEnabled(String isUserEnabled) {
        this.isUserEnabled = isUserEnabled;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public String getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(String amountPaid) {
        this.amountPaid = amountPaid;
    }
}
