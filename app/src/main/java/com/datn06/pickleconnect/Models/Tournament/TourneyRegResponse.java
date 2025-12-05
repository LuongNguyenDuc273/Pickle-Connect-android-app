package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response DTO for tournament registration
 * Returned from: POST /api-andr/tourney-reg
 */
public class TourneyRegResponse {
    
    @SerializedName("tournamentId")
    private String tournamentId;
    
    @SerializedName("userIdAlias")
    private String userIdAlias;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("gender")
    private String gender;
    
    @SerializedName("pointRanking")
    private String pointRanking;
    
    @SerializedName("entryFee")
    private String entryFee;
    
    @SerializedName("orderId")
    private String orderId;  // Important: Used for payment init
    
    @SerializedName("paymentMethod")
    private List<PaymentMethod> paymentMethod;
    
    // Constructors
    public TourneyRegResponse() {}
    
    // Getters and Setters
    public String getTournamentId() {
        return tournamentId;
    }
    
    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }
    
    public String getUserIdAlias() {
        return userIdAlias;
    }
    
    public void setUserIdAlias(String userIdAlias) {
        this.userIdAlias = userIdAlias;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPointRanking() {
        return pointRanking;
    }
    
    public void setPointRanking(String pointRanking) {
        this.pointRanking = pointRanking;
    }
    
    public String getEntryFee() {
        return entryFee;
    }
    
    public void setEntryFee(String entryFee) {
        this.entryFee = entryFee;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public List<PaymentMethod> getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(List<PaymentMethod> paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    // Nested class for payment methods
    public static class PaymentMethod {
        @SerializedName("methodCode")
        private String methodCode;
        
        @SerializedName("methodName")
        private String methodName;
        
        public PaymentMethod() {}
        
        public String getMethodCode() {
            return methodCode;
        }
        
        public void setMethodCode(String methodCode) {
            this.methodCode = methodCode;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }
    }
}
