package com.datn06.pickleconnect.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Response model for S3 image upload
 * Matches UploadResponse from s3-api service
 */
public class UploadImageResponse {
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("fileUrl")
    private String fileUrl;
    
    @SerializedName("originalName")
    private String originalName;
    
    @SerializedName("fileSize")
    private Long fileSize;
    
    @SerializedName("contentType")
    private String contentType;

    // Constructors
    public UploadImageResponse() {
    }

    public UploadImageResponse(String code, String message, String fileUrl) {
        this.code = code;
        this.message = message;
        this.fileUrl = fileUrl;
    }

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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Check if upload was successful
     */
    public boolean isSuccess() {
        return "00".equals(code);
    }
}
