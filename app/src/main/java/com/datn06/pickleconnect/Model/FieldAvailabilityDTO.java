package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO representing a field (court) with its available time slots
 */
public class FieldAvailabilityDTO {
    
    @SerializedName("fieldId")
    private Long fieldId;
    
    @SerializedName("fieldName")
    private String fieldName;  // "Sân 1", "Sân 2", etc.
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("status")
    private Integer status;  // 1 = Active, 0 = Inactive
    
    @SerializedName("timeSlots")
    private List<TimeSlotDTO> timeSlots;
    
    // Constructor
    public FieldAvailabilityDTO() {
        this.timeSlots = new ArrayList<>();
    }
    
    public FieldAvailabilityDTO(Long fieldId, String fieldName, String description, 
                                Integer status, List<TimeSlotDTO> timeSlots) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.description = description;
        this.status = status;
        this.timeSlots = timeSlots != null ? timeSlots : new ArrayList<>();
    }
    
    // Getters and Setters
    public Long getFieldId() {
        return fieldId;
    }
    
    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public List<TimeSlotDTO> getTimeSlots() {
        return timeSlots;
    }
    
    public void setTimeSlots(List<TimeSlotDTO> timeSlots) {
        this.timeSlots = timeSlots;
    }
    
    // Helper methods
    
    /**
     * Check if field is active
     */
    public boolean isActive() {
        return status != null && status == 1;
    }
    
    /**
     * Get number of available slots
     */
    public int getAvailableSlotCount() {
        if (timeSlots == null) {
            return 0;
        }
        
        return (int) timeSlots.stream()
                .filter(slot -> slot.getIsAvailable() != null && slot.getIsAvailable())
                .count();
    }
    
    /**
     * Get number of total slots
     */
    public int getTotalSlotCount() {
        return timeSlots != null ? timeSlots.size() : 0;
    }
    
    /**
     * Get availability percentage
     */
    public int getAvailabilityPercentage() {
        int total = getTotalSlotCount();
        if (total == 0) {
            return 0;
        }
        
        int available = getAvailableSlotCount();
        return (available * 100) / total;
    }
    
    /**
     * Get display name (e.g., "Sân 1 (5/10 slots)")
     */
    public String getDisplayName() {
        if (fieldName == null) {
            return "Unknown Field";
        }
        
        int available = getAvailableSlotCount();
        int total = getTotalSlotCount();
        
        return String.format("%s (%d/%d)", fieldName, available, total);
    }
    
    /**
     * Get short display name (just field number)
     */
    public String getShortName() {
        if (fieldName == null) {
            return "?";
        }
        
        // Extract number from "Sân 1" -> "1"
        String[] parts = fieldName.split(" ");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }
        
        return fieldName;
    }
    
    @Override
    public String toString() {
        return "FieldAvailabilityDTO{" +
                "fieldId=" + fieldId +
                ", fieldName='" + fieldName + '\'' +
                ", status=" + status +
                ", slots=" + getTotalSlotCount() +
                ", available=" + getAvailableSlotCount() +
                '}';
    }
}
