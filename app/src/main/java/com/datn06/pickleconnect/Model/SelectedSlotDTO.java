package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * DTO representing a selected slot for booking
 * Used in CreateBookingCourtRequest
 */
public class SelectedSlotDTO {
    
    @SerializedName("slotId")
    private Long slotId;
    
    // fieldId used only for UI grouping, not sent to backend
    private Long fieldId;
    
    @SerializedName("price")
    private BigDecimal price;
    
    @SerializedName("bookingSlotId")
    private Long bookingSlotId;  // Set by backend after creation
    
    // Additional fields for UI display (not sent to backend)
    private String fieldName;
    private String startTime;
    private String endTime;
    
    // Constructor
    public SelectedSlotDTO() {}
    
    public SelectedSlotDTO(Long slotId, Long fieldId, BigDecimal price) {
        this.slotId = slotId;
        this.fieldId = fieldId;
        this.price = price;
    }
    
    // Getters and Setters
    public Long getSlotId() {
        return slotId;
    }
    
    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }
    
    public Long getFieldId() {
        return fieldId;
    }
    
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Long getBookingSlotId() {
        return bookingSlotId;
    }
    
    public void setBookingSlotId(Long bookingSlotId) {
        this.bookingSlotId = bookingSlotId;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
    
    // Helper methods
    
    /**
     * Create SelectedSlotDTO from TimeSlotDTO and FieldAvailabilityDTO
     */
    public static SelectedSlotDTO fromTimeSlot(TimeSlotDTO timeSlot, Long fieldId) {
        SelectedSlotDTO dto = new SelectedSlotDTO(
            timeSlot.getSlotId(),
            fieldId,
            timeSlot.getFixedPrice()
        );
        // Set display fields
        dto.setStartTime(timeSlot.getStartTime());
        dto.setEndTime(timeSlot.getEndTime());
        return dto;
    }
    
    /**
     * Get formatted price
     */
    public String getFormattedPrice() {
        if (price == null) {
            return "0đ";
        }
        
        return String.format("%,dđ", price.intValue());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SelectedSlotDTO that = (SelectedSlotDTO) o;
        
        return slotId != null && slotId.equals(that.slotId);
    }
    
    @Override
    public int hashCode() {
        return slotId != null ? slotId.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "SelectedSlotDTO{" +
                "slotId=" + slotId +
                ", fieldId=" + fieldId +
                ", price=" + price +
                '}';
    }
}
