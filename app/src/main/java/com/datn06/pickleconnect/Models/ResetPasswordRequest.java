package com.datn06.pickleconnect.Models;

import com.datn06.pickleconnect.Common.BaseRequest;
import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for resetting password
 * Used in registration flow after OTP verification
 * Extends BaseRequest to inherit clientId, requestId, requestTime
 */
public class ResetPasswordRequest extends BaseRequest {
    
    @SerializedName("email")
    private String email; // For registration flow
    
    @SerializedName("phoneNumber")
    private String phoneNumber; // For reset password flow (optional)
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("confirmPassword")
    private String confirmPassword;


    public ResetPasswordRequest(String phoneNumber, String email, String password, String confirmPassword) {
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
