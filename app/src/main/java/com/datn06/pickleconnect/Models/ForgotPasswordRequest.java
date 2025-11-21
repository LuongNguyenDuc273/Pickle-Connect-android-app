package com.datn06.pickleconnect.Models;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Request DTO for Forgot Password API
 * Step 1: User enters email to request password reset
 */
public class ForgotPasswordRequest {
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("clientId")
    private String clientId;
    
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("requestTime")
    private String requestTime;
    
    public ForgotPasswordRequest(String email) {
        this.email = email;
        this.clientId = "pickle-app";
        this.requestId = "forgot-" + System.currentTimeMillis();
        
        SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss", Locale.getDefault());
        this.requestTime = sdf.format(new Date());
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getClientId() {
        return clientId;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getRequestTime() {
        return requestTime;
    }
    
    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }
}
