package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class EventDetailDTO {
    @SerializedName("eventId")
    private String eventId;

    @SerializedName("eventCode")
    private String eventCode;

    @SerializedName("eventName")
    private String eventName;

    @SerializedName("eventDescription")
    private String eventDescription;

    @SerializedName("eventType")
    private String eventType;

    @SerializedName("eventDate")
    private String eventDate;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("timeDisplay")
    private String timeDisplay;

    @SerializedName("ticketPrice")
    private BigDecimal ticketPrice;

    @SerializedName("priceDisplay")
    private String priceDisplay;

    @SerializedName("maxParticipants")
    private Integer maxParticipants;

    @SerializedName("currentParticipants")
    private Integer currentParticipants;

    @SerializedName("availableSlots")
    private Integer availableSlots;

    @SerializedName("bookingStatus")
    private String bookingStatus;

    @SerializedName("facility")
    private FacilityDTO facility;

    @SerializedName("fields")
    private List<EventFieldDTO> fields;

    @SerializedName("status")
    private Integer status;

    @SerializedName("statusDisplay")
    private String statusDisplay;

    @SerializedName("canRegister")
    private Boolean canRegister;

    @SerializedName("createdBy")
    private String createdBy;

    @SerializedName("creatorName")
    private String creatorName;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("systemBookingId")
    private String systemBookingId;

    @SerializedName("totalSlotsBooked")
    private Integer totalSlotsBooked;

    // Constructors
    public EventDetailDTO() {}

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTimeDisplay() {
        return timeDisplay;
    }

    public void setTimeDisplay(String timeDisplay) {
        this.timeDisplay = timeDisplay;
    }

    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(BigDecimal ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String getPriceDisplay() {
        return priceDisplay;
    }

    public void setPriceDisplay(String priceDisplay) {
        this.priceDisplay = priceDisplay;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public Integer getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(Integer availableSlots) {
        this.availableSlots = availableSlots;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public FacilityDTO getFacility() {
        return facility;
    }

    public void setFacility(FacilityDTO facility) {
        this.facility = facility;
    }

    public List<EventFieldDTO> getFields() {
        return fields;
    }

    public void setFields(List<EventFieldDTO> fields) {
        this.fields = fields;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusDisplay() {
        return statusDisplay;
    }

    public void setStatusDisplay(String statusDisplay) {
        this.statusDisplay = statusDisplay;
    }

    public Boolean getCanRegister() {
        return canRegister;
    }

    public void setCanRegister(Boolean canRegister) {
        this.canRegister = canRegister;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSystemBookingId() {
        return systemBookingId;
    }

    public void setSystemBookingId(String systemBookingId) {
        this.systemBookingId = systemBookingId;
    }

    public Integer getTotalSlotsBooked() {
        return totalSlotsBooked;
    }

    public void setTotalSlotsBooked(Integer totalSlotsBooked) {
        this.totalSlotsBooked = totalSlotsBooked;
    }

    // Helper methods
    public int getStatusColor() {
        if (status == null) return 0xFF9800;

        switch (status) {
            case 1: // ACTIVE
                return 0x4CAF50; // Green
            case 2: // FULL
                return 0xFF5722; // Red-Orange
            case 3: // CANCELLED
                return 0x9E9E9E; // Grey
            default:
                return 0xFF9800; // Orange
        }
    }

    public String getFieldsAsString() {
        if (fields == null || fields.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            sb.append(fields.get(i).getFieldName());
            if (i < fields.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    public boolean isFree() {
        return ticketPrice != null && ticketPrice.compareTo(BigDecimal.ZERO) == 0;
    }
}