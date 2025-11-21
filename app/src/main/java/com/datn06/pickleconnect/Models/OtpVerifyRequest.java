package com.datn06.pickleconnect.Models;

import com.datn06.pickleconnect.Constants.SecurityConstants;
import com.datn06.pickleconnect.Utils.ChecksumUtils;
import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for verifying OTP
 * Maps to backend VerifyOtpRequest
 */
public class OtpVerifyRequest {
    
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("phone")
    private String phone; // Empty string for email-based OTP
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("timestamp")
    private String timestamp;
    
    @SerializedName("otp")
    private String otp;
    
    @SerializedName("checksum")
    private String checksum;

    /**
     * Constructor for email-based OTP verification
     * Auto-generates timestamp and checksum
     * 
     * @param email User email address
     * @param otp 6-digit OTP code
     * @param requestId Unique request ID
     */
    public OtpVerifyRequest(String email, String otp, String requestId) {
        this.email = email;
        this.phone = null; // ✅ null for email-based OTP
        this.otp = otp;
        this.requestId = requestId;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        
        // ✅ FIXED: Use email instead of phone in checksum for email-based OTP
        // Backend logic: identifier = phone != null ? phone : email
        // So we pass email to checksum calculation
        this.checksum = ChecksumUtils.generateVerifyOtpChecksum(
            SecurityConstants.SMS_SECRET_KEY,
            requestId,
            email, // ✅ Use email instead of phone/null
            timestamp,
            otp
        );
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
