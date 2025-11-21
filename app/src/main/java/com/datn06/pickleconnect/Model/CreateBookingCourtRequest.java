package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a court booking
 * Corresponds to backend's CreateBookingCourtRequest
 */
public class CreateBookingCourtRequest {
    
    @SerializedName("facilityId")
    private Long facilityId;
    
    @SerializedName("userEmail")
    private String userEmail;
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("userName")
    private String userName;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("paymentMethodCode")
    private String paymentMethodCode;  // "VNPPGW"
    
    @SerializedName("totalAmount")
    private BigDecimal totalAmount;
    
    @SerializedName("totalHours")
    private Integer totalHours;
    
    @SerializedName("bookingDate")
    private String bookingDate;  // Format: "2025-04-22"
    
    @SerializedName("selectedSlots")
    private List<SelectedSlotDTO> selectedSlots;
    
    @SerializedName("orderDescription")
    private String orderDescription;
    
    // Fields set by backend (optional in request)
    @SerializedName("orderType")
    private String orderType;
    
    @SerializedName("requestId")
    private String requestId;
    
    @SerializedName("responseQueueName")
    private String responseQueueName;
    
    @SerializedName("txnId")
    private Long txnId;
    
    @SerializedName("bookId")
    private Long bookId;
    
    // Constructor
    public CreateBookingCourtRequest() {}
    
    // Builder pattern
    public static class Builder {
        private CreateBookingCourtRequest request = new CreateBookingCourtRequest();
        
        public Builder facilityId(Long facilityId) {
            request.facilityId = facilityId;
            return this;
        }
        
        public Builder userEmail(String userEmail) {
            request.userEmail = userEmail;
            return this;
        }
        
        public Builder userId(Long userId) {
            request.userId = userId;
            return this;
        }
        
        public Builder userName(String userName) {
            request.userName = userName;
            return this;
        }
        
        public Builder orderType(String orderType) {
            request.orderType = orderType;
            return this;
        }
        
        public Builder paymentMethodCode(String paymentMethodCode) {
            request.paymentMethodCode = paymentMethodCode;
            return this;
        }
        
        public Builder totalAmount(BigDecimal totalAmount) {
            request.totalAmount = totalAmount;
            return this;
        }
        
        public Builder totalHours(Integer totalHours) {
            request.totalHours = totalHours;
            return this;
        }
        
        public Builder bookingDate(String bookingDate) {
            request.bookingDate = bookingDate;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            request.phoneNumber = phoneNumber;
            return this;
        }
        
        public Builder selectedSlots(List<SelectedSlotDTO> selectedSlots) {
            request.selectedSlots = selectedSlots;
            return this;
        }
        
        public Builder orderDescription(String orderDescription) {
            request.orderDescription = orderDescription;
            return this;
        }
        
        public CreateBookingCourtRequest build() {
            return request;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public Long getFacilityId() {
        return facilityId;
    }
    
    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }
    
    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getTotalHours() {
        return totalHours;
    }
    
    public void setTotalHours(Integer totalHours) {
        this.totalHours = totalHours;
    }
    
    public String getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public List<SelectedSlotDTO> getSelectedSlots() {
        return selectedSlots;
    }
    
    public void setSelectedSlots(List<SelectedSlotDTO> selectedSlots) {
        this.selectedSlots = selectedSlots;
    }
    
    public String getOrderDescription() {
        return orderDescription;
    }
    
    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }
    
    public String getOrderType() {
        return orderType;
    }
    
    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getResponseQueueName() {
        return responseQueueName;
    }
    
    public void setResponseQueueName(String responseQueueName) {
        this.responseQueueName = responseQueueName;
    }
    
    public Long getTxnId() {
        return txnId;
    }
    
    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
}
