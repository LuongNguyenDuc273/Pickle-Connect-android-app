package com.datn06.pickleconnect.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Response DTO for Forgot Password API
 * Contains email, masked email, username, and OTP expiry time
 */
public class ForgotPasswordResponse {
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("maskedEmail")
    private String maskedEmail;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("otpExpireTime")
    private Integer otpExpireTime;
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMaskedEmail() {
        return maskedEmail;
    }
    
    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Integer getOtpExpireTime() {
        return otpExpireTime;
    }
    
    public void setOtpExpireTime(Integer otpExpireTime) {
        this.otpExpireTime = otpExpireTime;
    }
}
