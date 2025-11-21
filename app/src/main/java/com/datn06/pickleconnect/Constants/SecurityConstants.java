package com.datn06.pickleconnect.Constants;

/**
 * API Security Constants
 * Must match backend configuration in application.yaml
 */
public class SecurityConstants {
    
    /**
     * Secret key for SMS/OTP verification checksum
     * Backend: app.sms-secret-key = SMS_HMAC_KEYCLOAK_2025
     */
    public static final String SMS_SECRET_KEY = "SMS_HMAC_KEYCLOAK_2025";
    
    /**
     * Secret key for Email OTP checksum
     * Backend: app.email-secret-key = HMAC_EMAIL_2025
     */
    public static final String EMAIL_SECRET_KEY = "HMAC_EMAIL_2025";
}
