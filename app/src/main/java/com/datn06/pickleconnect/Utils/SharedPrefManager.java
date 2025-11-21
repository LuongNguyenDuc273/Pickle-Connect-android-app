package com.datn06.pickleconnect.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manager class for handling SharedPreferences
 * Stores user information and authentication tokens
 */
public class SharedPrefManager {
    
    private static final String SHARED_PREF_NAME = "pickle_connect_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phoneNumber";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_BOOKING_FACILITY_ID = "booking_facility_id";
    
    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;
    
    private SharedPrefManager(Context context) {
        sharedPreferences = context.getApplicationContext()
            .getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    /**
     * Get singleton instance
     */
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }
    
    /**
     * Save user information after login
     */
    public void saveUser(String userId, String username, String email, String phone, String fullName) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }
    
    /**
     * Save authentication tokens
     */
    public void saveTokens(String accessToken, String refreshToken) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }
    
    /**
     * Get user ID
     */
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }
    
    /**
     * Get username
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
    
    /**
     * Get email
     */
    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }
    
    /**
     * Get phone number
     */
    public String getPhone() {
        return sharedPreferences.getString(KEY_PHONE, null);
    }
    
    /**
     * Get full name
     */
    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, null);
    }
    
    /**
     * Get access token
     */
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }
    
    /**
     * Get refresh token
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    /**
     * Clear all user data (logout)
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Clear only tokens (for token refresh scenarios)
     */
    public void clearTokens() {
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }
    
    /**
     * Save booking facility ID (used when creating booking)
     */
    public void saveBookingFacilityId(String facilityId) {
        editor.putString(KEY_BOOKING_FACILITY_ID, facilityId);
        editor.apply();
    }
    
    /**
     * Get booking facility ID
     */
    public String getBookingFacilityId() {
        return sharedPreferences.getString(KEY_BOOKING_FACILITY_ID, null);
    }
}
