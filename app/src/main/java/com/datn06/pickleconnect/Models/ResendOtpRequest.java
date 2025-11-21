package com.datn06.pickleconnect.Models;

import com.datn06.pickleconnect.Constants.SecurityConstants;
import com.datn06.pickleconnect.Utils.ChecksumUtils;
import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for resending OTP email
 * Maps to backend SendEmailRequest
 */
public class ResendOtpRequest {
    
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("timestamp")
    private String timestamp;
    
    @SerializedName("to")
    private String to; // Email address
    
    @SerializedName("checksum")
    private String checksum;

    /**
     * Constructor with auto-generated timestamp and checksum
     * @param email User email address
     * @param requestId Unique request ID
     */
    public ResendOtpRequest(String email, String requestId) {
        this.to = email;
        this.requestId = requestId;
        this.timestamp = String.valueOf(System.currentTimeMillis());
        this.checksum = ChecksumUtils.generateSendEmailChecksum(
            SecurityConstants.EMAIL_SECRET_KEY,
            requestId,
            email,
            timestamp
        );
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
