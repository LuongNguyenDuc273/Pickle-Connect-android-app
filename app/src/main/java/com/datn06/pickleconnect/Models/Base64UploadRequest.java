package com.datn06.pickleconnect.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Request model for Base64 image upload
 * Matches Base64UploadRequest from member-query-api
 */
public class Base64UploadRequest {

    @SerializedName("imageData")
    private String imageData;

    @SerializedName("userId")
    private String userId;

    @SerializedName("requestId")
    private String requestId;

    @SerializedName("requestTime")
    private String requestTime;

    @SerializedName("subFolder")
    private String subFolder;

    // Constructors
    public Base64UploadRequest() {
    }

    public Base64UploadRequest(String imageData, String userId, String requestId, String requestTime) {
        this.imageData = imageData;
        this.userId = userId;
        this.requestId = requestId;
        this.requestTime = requestTime;
    }

    // Getters and Setters
    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

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

    public String getSubFolder() {
        return subFolder;
    }

    public void setSubFolder(String subFolder) {
        this.subFolder = subFolder;
    }
}
