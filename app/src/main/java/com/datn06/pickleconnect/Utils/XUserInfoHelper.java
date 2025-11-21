package com.datn06.pickleconnect.Utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

/**
 * Helper class to generate X-Userinfo header for API requests
 * Backend expects Base64-encoded JSON with user information
 */
public class XUserInfoHelper {
    
    private static final String TAG = "XUserInfoHelper";
    
    /**
     * Generate X-Userinfo header value
     * Backend expects Base64-encoded JSON like:
     * {
     *   "preferred_username": "user123",
     *   "sub": "user-id",
     *   "email": "user@example.com"
     * }
     * 
     * @param username Username (preferred_username field)
     * @param userId User ID (sub field)
     * @param email User email
     * @return Base64-encoded JSON string
     */
    public static String generateXUserInfo(String username, String userId, String email) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("preferred_username", username != null ? username : "");
            jsonObject.put("sub", userId != null ? userId : "");
            jsonObject.put("email", email != null ? email : "");
            
            String jsonString = jsonObject.toString();
            
            // Encode to Base64
            byte[] data = jsonString.getBytes("UTF-8");
            String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
            
            Log.d(TAG, "Generated X-Userinfo: " + jsonString + " -> " + base64);
            
            return base64;
            
        } catch (Exception e) {
            Log.e(TAG, "Error generating X-Userinfo", e);
            return "";
        }
    }
    
    /**
     * Generate X-Userinfo from SharedPrefManager
     * 
     * @param prefManager SharedPrefManager instance
     * @return Base64-encoded JSON string
     */
    public static String generateXUserInfoFromPrefs(SharedPrefManager prefManager) {
        if (prefManager == null) {
            Log.w(TAG, "SharedPrefManager is null");
            return "";
        }
        
        String username = prefManager.getUsername();
        String userId = prefManager.getUserId();
        String email = prefManager.getEmail();
        
        return generateXUserInfo(username, userId, email);
    }
    
    /**
     * Decode X-Userinfo header (for debugging)
     * 
     * @param base64String Base64-encoded string
     * @return Decoded JSON string
     */
    public static String decodeXUserInfo(String base64String) {
        try {
            byte[] data = Base64.decode(base64String, Base64.NO_WRAP);
            String jsonString = new String(data, "UTF-8");
            Log.d(TAG, "Decoded X-Userinfo: " + jsonString);
            return jsonString;
        } catch (Exception e) {
            Log.e(TAG, "Error decoding X-Userinfo", e);
            return "";
        }
    }
}
