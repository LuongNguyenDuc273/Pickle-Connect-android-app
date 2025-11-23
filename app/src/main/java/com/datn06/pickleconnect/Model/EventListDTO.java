package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.math.BigInteger;

public class EventListDTO {
    @SerializedName("eventId")
    private String eventId;

    @SerializedName("eventCode")
    private String eventCode;

    @SerializedName("eventName")
    private String eventName;

    @SerializedName("eventType")
    private String eventType;

    @SerializedName("eventDate")
    private String eventDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("ticketPrice")
    private BigDecimal ticketPrice;

    @SerializedName("priceDisplay")
    private String priceDisplay;

    @SerializedName("maxParticipants")
    private Integer maxParticipants;

    @SerializedName("currentParticipants")
    private Integer currentParticipants;

    @SerializedName("bookingStatus")
    private String bookingStatus;

    @SerializedName("availableSlots")
    private Integer availableSlots;

    @SerializedName("facilityId")
    private String facilityId;

    @SerializedName("facilityName")
    private String facilityName;

    @SerializedName("fields")
    private String fields;

    @SerializedName("status")
    private Integer status;

    @SerializedName("statusDisplay")
    private String statusDisplay;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getEventCode() { return eventCode; }
    public void setEventCode(String eventCode) { this.eventCode = eventCode; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public BigDecimal getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(BigDecimal ticketPrice) { this.ticketPrice = ticketPrice; }

    public String getPriceDisplay() { return priceDisplay; }
    public void setPriceDisplay(String priceDisplay) { this.priceDisplay = priceDisplay; }

    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }

    public Integer getCurrentParticipants() { return currentParticipants; }
    public void setCurrentParticipants(Integer currentParticipants) { this.currentParticipants = currentParticipants; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public Integer getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

    public String getFacilityId() { return facilityId; }
    public void setFacilityId(String facilityId) { this.facilityId = facilityId; }

    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }

    public String getFields() { return fields; }
    public void setFields(String fields) { this.fields = fields; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper method để lấy màu theo trạng thái
    public int getStatusColor() {
        if (status == null) return 0xFF9800; // Orange default

        switch (status) {
            case 1: // ACTIVE - Đang mở
                return 0x4CAF50; // Green
            case 2: // FULL - Đã đầy
                return 0xFF5722; // Red-Orange
            case 3: // CANCELLED - Đã hủy
                return 0x9E9E9E; // Grey
            default:
                return 0xFF9800; // Orange
        }
    }
}
