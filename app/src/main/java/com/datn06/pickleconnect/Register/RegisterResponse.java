package com.datn06.pickleconnect.Register;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("code")
    private String code;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private RegisterData data;
    
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("traceId")
    private String traceId;
    
    @SerializedName("timestamp")
    private String timestamp;

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RegisterData getData() {
        return data;
    }

    public void setData(RegisterData data) {
        this.data = data;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Inner class for registration response data
     * Contains email info and OTP expiration time
     */
    public static class RegisterData {
        @SerializedName("email")
        private String email;
        
        @SerializedName("maskedEmail")
        private String maskedEmail;
        
        @SerializedName("otpExpireTime")
        private Integer otpExpireTime; // In seconds (e.g., 300 = 5 minutes)

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

        public Integer getOtpExpireTime() {
            return otpExpireTime;
        }

        public void setOtpExpireTime(Integer otpExpireTime) {
            this.otpExpireTime = otpExpireTime;
        }
    }
}
