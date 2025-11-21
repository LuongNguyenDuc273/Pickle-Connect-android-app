package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for payment URL response
 * Returned after creating a booking
 */
public class PaymentUrlResponse {
    
    @SerializedName("paymentUrl")
    private String paymentUrl;
    
    @SerializedName("urlPayment")  // Backend might use this name
    private String urlPayment;
    
    @SerializedName("tmnCode")
    private String tmnCode;
    
    // Constructor
    public PaymentUrlResponse() {}
    
    public PaymentUrlResponse(String paymentUrl, String tmnCode) {
        this.paymentUrl = paymentUrl;
        this.tmnCode = tmnCode;
    }
    
    // Getters and Setters
    public String getPaymentUrl() {
        // Try both field names for compatibility
        return paymentUrl != null ? paymentUrl : urlPayment;
    }
    
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
    
    public String getUrlPayment() {
        return urlPayment;
    }
    
    public void setUrlPayment(String urlPayment) {
        this.urlPayment = urlPayment;
    }
    
    public String getTmnCode() {
        return tmnCode;
    }
    
    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }
    
    // Helper methods
    
    /**
     * Check if payment URL is valid
     */
    public boolean hasValidUrl() {
        String url = getPaymentUrl();
        return url != null && !url.isEmpty() && url.startsWith("http");
    }
    
    @Override
    public String toString() {
        return "PaymentUrlResponse{" +
                "paymentUrl='" + getPaymentUrl() + '\'' +
                ", tmnCode='" + tmnCode + '\'' +
                '}';
    }
}
