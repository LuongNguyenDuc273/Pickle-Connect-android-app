package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Base class for all Tournament API requests
 * Contains common fields required by tournament backend
 */
public class BaseTournamentRequest {

    @SerializedName("userId")
    private String userId;

    @SerializedName("requestId")
    private String requestId;

    @SerializedName("requestTime")
    private String requestTime;

    @SerializedName("clientId")
    private String clientId;

    /**
     * Default constructor
     * Generates requestId, requestTime with correct format, and sets clientId
     */
    public BaseTournamentRequest() {
        this.requestId = generateRequestId();
        this.requestTime = generateRequestTime();
        this.clientId = "ANDROID_APP";
    }

    /**
     * Constructor with userId
     * @param userId User ID
     */
    public BaseTournamentRequest(String userId) {
        this();
        this.userId = userId;
    }

    /**
     * Generate unique request ID
     * Format: REQ_timestamp_randomNumber
     */
    private String generateRequestId() {
        return "REQ_" + System.currentTimeMillis() + "_" +
                (int)(Math.random() * 10000);
    }

    /**
     * Generate request time in format yyyyMMddHHmmss
     * This format is required by backend validation
     * Example: 20251130105012
     */
    private String generateRequestTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}