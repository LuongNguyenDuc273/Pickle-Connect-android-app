package com.datn06.pickleconnect.Models;

import com.google.gson.annotations.SerializedName;

public class OtpVerifyResponse {
    
    @SerializedName("otpToken")
    private String otpToken;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("expiresIn")
    private Integer expiresIn; // Token expiry time in seconds (e.g., 900 = 15 minutes)

    // Getters and Setters
    public String getOtpToken() {
        return otpToken;
    }

    public void setOtpToken(String otpToken) {
        this.otpToken = otpToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }
}
