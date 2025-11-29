package com.datn06.pickleconnect.Utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * TokenManager - Singleton class to manage JWT Token storage and retrieval
 */
public class TokenManager {

    private static TokenManager instance;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "PickleConnectPrefs";
    private static final String KEY_JWT_TOKEN = "jwt_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE_NUMBER = "phone_number";

    // Private constructor
    private TokenManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Singleton instance
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    // =========================================================================
    //                         SAVE METHODS
    // =========================================================================

    /**
     * Save JWT token after successful login
     */
    public void saveToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_JWT_TOKEN, token);
        editor.apply();
    }

    /**
     * Save refresh token
     */
    public void saveRefreshToken(String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    /**
     * Save user information after login
     */
    public void saveUserInfo(String userId, String username, String fullName, String email, String phoneNumber) { // ✅ THÊM phoneNumber
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber); // ✅ LƯU phoneNumber
        editor.apply();
    }

    /**
     * Save all login data at once
     */
    public void saveLoginData(String token, String refreshToken, String userId,
                              String username, String fullName, String email, String phoneNumber) { // ✅ THÊM phoneNumber
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_JWT_TOKEN, token);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE_NUMBER, phoneNumber); // ✅ LƯU phoneNumber
        editor.apply();
    }

    // =========================================================================
    //                         GET METHODS
    // =========================================================================

    /**
     * Get JWT token
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_JWT_TOKEN, null);
    }

    /**
     * Get JWT token with "Bearer " prefix for API calls
     */
    public String getAuthHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

    /**
     * Get refresh token
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
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
     * Get full name
     */
    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, null);
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
    public String getPhoneNumber() { // ✅ THÊM MỚI
        return sharedPreferences.getString(KEY_PHONE_NUMBER, null);
    }

    // =========================================================================
    //                         CHECK METHODS
    // =========================================================================

    /**
     * Check if user is logged in (has valid token)
     */
    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    // =========================================================================
    //                         CLEAR METHODS
    // =========================================================================

    /**
     * Clear all stored data (logout)
     */
    public void clearAll() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * Clear only token data (keep user info)
     */
    public void clearTokens() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_JWT_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.apply();
    }
}