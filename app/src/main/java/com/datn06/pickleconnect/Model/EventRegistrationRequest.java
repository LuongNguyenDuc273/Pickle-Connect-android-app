package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request để đăng ký tham gia sự kiện
 * Matches backend EventRegistrationRequest.java
 */
public class EventRegistrationRequest {

    @SerializedName("requestId")
    private String requestId;

    @SerializedName("eventId")
    private String eventId; // Backend uses BigInteger but Android sends as String

    @SerializedName("userId")
    private Long userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("quantity")
    private Integer quantity; // Default = 1 nếu null

    @SerializedName("notes")
    private String notes;

    @SerializedName("paymentMethodCode")
    private String paymentMethodCode; // "VNPAY"

    @SerializedName("orderDescription")
    private String orderDescription;

    @SerializedName("txnId")
    private Long txnId;

    @SerializedName("responseQueueName")
    private String responseQueueName;
    
    // ✅ ADDED: Additional fields for booking creation (match backend)
    @SerializedName("totalAmount")
    private String totalAmount; // Total price as String for JSON (backend converts to BigDecimal)
    
    @SerializedName("totalHours")
    private Integer totalHours; // Event duration in hours (usually 0 for events)
    
    @SerializedName("bookingDate")
    private String bookingDate; // Event date for booking (yyyy-MM-dd)
    
    @SerializedName("facilityId")
    private String facilityId; // Facility ID as String (backend converts to BigInteger)
    
    @SerializedName("bookId")
    private Long bookId; // Generated booking ID (= registrationId on backend)
    
    // ✅ ADDED: Selected slots for event (merged consecutive slots)
    @SerializedName("selectedSlots")
    private List<SelectedSlotDTO> selectedSlots; // Event slots to save in TXN_BOOKING_SLOTS

    // Constructor
    public EventRegistrationRequest() {}

    // Builder pattern
    public static class Builder {
        private EventRegistrationRequest request = new EventRegistrationRequest();

        public Builder eventId(String eventId) {
            request.eventId = eventId;
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

        public Builder userEmail(String userEmail) {
            request.userEmail = userEmail;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            request.phoneNumber = phoneNumber;
            return this;
        }

        public Builder quantity(Integer quantity) {
            request.quantity = quantity;
            return this;
        }

        public Builder notes(String notes) {
            request.notes = notes;
            return this;
        }

        public Builder paymentMethodCode(String paymentMethodCode) {
            request.paymentMethodCode = paymentMethodCode;
            return this;
        }

        public Builder orderDescription(String orderDescription) {
            request.orderDescription = orderDescription;
            return this;
        }
        
        public Builder totalAmount(String totalAmount) {
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
        
        public Builder facilityId(String facilityId) {
            request.facilityId = facilityId;
            return this;
        }
        
        public Builder selectedSlots(List<SelectedSlotDTO> selectedSlots) {
            request.selectedSlots = selectedSlots;
            return this;
        }

        public EventRegistrationRequest build() {
            return request;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPaymentMethodCode() {
        return paymentMethodCode;
    }

    public void setPaymentMethodCode(String paymentMethodCode) {
        this.paymentMethodCode = paymentMethodCode;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }

    public Long getTxnId() {
        return txnId;
    }

    public void setTxnId(Long txnId) {
        this.txnId = txnId;
    }

    public String getResponseQueueName() {
        return responseQueueName;
    }

    public void setResponseQueueName(String responseQueueName) {
        this.responseQueueName = responseQueueName;
    }
    
    public String getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(String totalAmount) {
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
    
    public String getFacilityId() {
        return facilityId;
    }
    
    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public List<SelectedSlotDTO> getSelectedSlots() {
        return selectedSlots;
    }
    
    public void setSelectedSlots(List<SelectedSlotDTO> selectedSlots) {
        this.selectedSlots = selectedSlots;
    }

    @Override
    public String toString() {
        return "EventRegistrationRequest{" +
                "eventId='" + eventId + '\'' +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", quantity=" + quantity +
                ", notes='" + notes + '\'' +
                ", paymentMethodCode='" + paymentMethodCode + '\'' +
                '}';
    }
}
