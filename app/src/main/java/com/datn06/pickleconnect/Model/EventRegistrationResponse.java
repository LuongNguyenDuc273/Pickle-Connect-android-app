package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * Response sau khi đăng ký sự kiện thành công
 * Matches backend EventRegistrationResponse.java
 */
public class EventRegistrationResponse {

    @SerializedName("registrationId")
    private String registrationId; // Backend BigInteger → Android String

    @SerializedName("registrationCode")
    private String registrationCode;

    @SerializedName("eventId")
    private String eventId; // Backend BigInteger → Android String

    @SerializedName("eventName")
    private String eventName;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("totalPrice")
    private BigDecimal totalPrice;

    @SerializedName("paymentUrl")
    private String paymentUrl; // URL để thanh toán VNPay

    @SerializedName("txnId")
    private String txnId; // Transaction ID

    @SerializedName("message")
    private String message;

    // Constructor
    public EventRegistrationResponse() {}

    // Getters and Setters
    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Check if payment URL is available
     */
    public boolean hasPaymentUrl() {
        return paymentUrl != null && !paymentUrl.isEmpty();
    }

    /**
     * Format total price for display
     */
    public String getFormattedPrice() {
        if (totalPrice != null) {
            return String.format("%,d VNĐ", totalPrice.longValue());
        }
        return "0 VNĐ";
    }

    @Override
    public String toString() {
        return "EventRegistrationResponse{" +
                "registrationId='" + registrationId + '\'' +
                ", registrationCode='" + registrationCode + '\'' +
                ", eventId='" + eventId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", paymentUrl='" + (paymentUrl != null ? "***" : "null") + '\'' +
                ", txnId='" + txnId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
