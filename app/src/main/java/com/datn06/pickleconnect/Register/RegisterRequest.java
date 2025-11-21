package com.datn06.pickleconnect.Register;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterRequest {
    @SerializedName("username")
    private String userName;
    
    private String email;
    private String fullName;
    private String phoneNumber;
    private String dateOfBirth;  // Format: dd-MM-yyyy
    private String gender;       // "0" for Female, "1" for Male
    private String address;
    private String height;
    private String weight;
    private String requestId;
    private String clientId;
    private String requestTime;  // Format: YYYYMMddHHmmss

    public RegisterRequest() {
        // Generate unique requestId
        this.requestId = String.valueOf(System.currentTimeMillis());
        this.clientId = "pickle-app"; // Default client ID
        
        // Generate requestTime in format YYYYMMddHHmmss
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        this.requestTime = sdf.format(new Date());
    }

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }
}
