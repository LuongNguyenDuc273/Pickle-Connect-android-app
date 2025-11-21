package com.datn06.pickleconnect.Common;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Base class for all API requests
 * Contains common fields required by backend: clientId, requestId, requestTime
 * 
 * Backend validation requirements:
 * - clientId: Not blank (e.g., "pickle-app")
 * - requestId: Not blank (e.g., "ex-12345678910")
 * - requestTime: Not blank, must match format yyyyMMddHHmmss (e.g., "20251118135049")
 */
public class BaseRequest {
    
    @SerializedName("clientId")
    private String clientId;

    @SerializedName("requestId")
    private String requestId;

    @SerializedName("requestTime")
    private String requestTime;

    /**
     * Default constructor that auto-generates required fields
     */
    public BaseRequest() {
        this.clientId = "pickle-app";
        this.requestId = generateRequestId();
        this.requestTime = generateRequestTime();
    }

    /**
     * Constructor with custom clientId
     */
    public BaseRequest(String clientId) {
        this.clientId = clientId;
        this.requestId = generateRequestId();
        this.requestTime = generateRequestTime();
    }

    /**
     * Generate unique request ID
     * Format: ex-[timestamp]
     * Example: ex-1700302249685
     */
    private String generateRequestId() {
        return "ex-" + System.currentTimeMillis();
    }

    /**
     * Generate request time in backend required format
     * Format: yyyyMMddHHmmss
     * Example: 20251118135049
     */
    private String generateRequestTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // Getters and Setters
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
