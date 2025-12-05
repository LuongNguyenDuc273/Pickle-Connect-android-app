package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

/**
 * Request DTO for tournament payment initialization
 * Endpoint: POST /api-andr/tourney-reg-init
 */
public class TourneyRegInitRequest extends BaseTournamentRequest {
    
    @SerializedName("totalAmount")
    private String totalAmount;
    
    @SerializedName("orderDescription")
    private String orderDescription;
    
    @SerializedName("paymentMethod")
    private String paymentMethod;
    
    @SerializedName("orderId")
    private String orderId;  // From TourneyRegResponse
    
    // Constructors
    public TourneyRegInitRequest() {}
    
    public TourneyRegInitRequest(String userId, String totalAmount, String orderDescription,
                                 String paymentMethod, String orderId) {
        super(userId);
        this.totalAmount = totalAmount;
        this.orderDescription = orderDescription;
        this.paymentMethod = paymentMethod;
        this.orderId = orderId;
    }
    
    // Getters and Setters
    public String getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getOrderDescription() {
        return orderDescription;
    }
    
    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
