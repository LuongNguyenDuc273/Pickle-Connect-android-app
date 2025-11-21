package com.datn06.pickleconnect.Utils;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for generating HMAC-SHA512 checksum
 * Used to validate API requests with backend
 */
public class ChecksumUtils {
    private static final String TAG = "ChecksumUtils";
    private static final String HMAC_ALGORITHM = "HmacSHA512";

    /**
     * Generate HMAC-SHA512 checksum
     *
     * @param secretKey The secret key from backend configuration
     * @param data      The data to hash (e.g., "requestId|email|timestamp")
     * @return Hex string of HMAC-SHA512 hash
     */
    public static String hmacSHA512(String secretKey, String data) {
        try {
            if (secretKey == null || data == null) {
                throw new NullPointerException("SecretKey and data must not be null");
            }

            // Initialize HMAC-SHA512
            Mac hmac512 = Mac.getInstance(HMAC_ALGORITHM);
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, HMAC_ALGORITHM);
            hmac512.init(secretKeySpec);

            // Hash the data
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);

            // Convert to hex string
            StringBuilder hexString = new StringBuilder(2 * result.length);
            for (byte b : result) {
                hexString.append(String.format("%02x", b & 0xff));
            }

            return hexString.toString();
        } catch (Exception ex) {
            Log.e(TAG, "Error generating HMAC-SHA512 checksum", ex);
            return "";
        }
    }

    /**
     * Generate checksum for SendEmailRequest
     * Format: requestId|email|timestamp
     *
     * @param secretKey The email secret key from backend
     * @param requestId Unique request ID
     * @param email     User email
     * @param timestamp Current timestamp in milliseconds
     * @return HMAC-SHA512 checksum
     */
    public static String generateSendEmailChecksum(String secretKey, String requestId, String email, String timestamp) {
        String data = requestId + "|" + email + "|" + timestamp;
        return hmacSHA512(secretKey, data);
    }

    /**
     * Generate checksum for VerifyOtpRequest
     * Format: requestId|identifier|timestamp|otp
     * 
     * Identifier can be either phone or email:
     * - Phone-based OTP: identifier = phone number
     * - Email-based OTP: identifier = email address
     *
     * @param secretKey The SMS/OTP secret key from backend
     * @param requestId Unique request ID
     * @param identifier User phone or email (depends on OTP type)
     * @param timestamp Current timestamp in milliseconds
     * @param otp       6-digit OTP code
     * @return HMAC-SHA512 checksum
     */
    public static String generateVerifyOtpChecksum(String secretKey, String requestId, String identifier, String timestamp, String otp) {
        // Format: requestId|identifier|timestamp|otp
        // identifier can be phone or email
        String data = requestId + "|" + identifier + "|" + timestamp + "|" + otp;
        
        Log.d(TAG, "VerifyOtp checksum data: " + data);
        return hmacSHA512(secretKey, data);
    }
}
