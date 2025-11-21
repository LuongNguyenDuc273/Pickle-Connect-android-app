package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * DTO representing the response from field availability API
 * Corresponds to backend's FieldBookingResponse
 */
public class FieldBookingResponse {
    
    @SerializedName("facilityId")
    private Long facilityId;
    
    @SerializedName("facilityName")
    private String facilityName;
    
    @SerializedName("bookingDate")
    private String bookingDate;  // Format: "2025-04-22"
    
    @SerializedName("fields")
    private List<FieldAvailabilityDTO> fields;
    
    // Constructor
    public FieldBookingResponse() {
        this.fields = new ArrayList<>();
    }
    
    public FieldBookingResponse(Long facilityId, String facilityName, 
                                String bookingDate, List<FieldAvailabilityDTO> fields) {
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.bookingDate = bookingDate;
        this.fields = fields != null ? fields : new ArrayList<>();
    }
    
    // Getters and Setters
    public Long getFacilityId() {
        return facilityId;
    }
    
    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }
    
    public String getFacilityName() {
        return facilityName;
    }
    
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
    
    public String getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public List<FieldAvailabilityDTO> getFields() {
        return fields;
    }
    
    public void setFields(List<FieldAvailabilityDTO> fields) {
        this.fields = fields;
    }
    
    // Helper methods
    
    /**
     * Get total number of fields
     */
    public int getFieldCount() {
        return fields != null ? fields.size() : 0;
    }
    
    /**
     * Get total number of available slots across all fields
     */
    public int getTotalAvailableSlots() {
        if (fields == null) {
            return 0;
        }
        
        return fields.stream()
                .mapToInt(FieldAvailabilityDTO::getAvailableSlotCount)
                .sum();
    }
    
    /**
     * Get total number of slots across all fields
     */
    public int getTotalSlots() {
        if (fields == null) {
            return 0;
        }
        
        return fields.stream()
                .mapToInt(FieldAvailabilityDTO::getTotalSlotCount)
                .sum();
    }
    
    /**
     * Get field by ID
     */
    public FieldAvailabilityDTO getFieldById(Long fieldId) {
        if (fields == null || fieldId == null) {
            return null;
        }
        
        return fields.stream()
                .filter(field -> fieldId.equals(field.getFieldId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Get field by index
     */
    public FieldAvailabilityDTO getFieldByIndex(int index) {
        if (fields == null || index < 0 || index >= fields.size()) {
            return null;
        }
        
        return fields.get(index);
    }
    
    /**
     * Get all time slots from a specific field grouped by time period
     * Returns Map: "Sáng" -> [slots], "Chiều" -> [slots], "Tối" -> [slots]
     */
    public Map<String, List<TimeSlotDTO>> getSlotsByPeriod(int fieldIndex) {
        FieldAvailabilityDTO field = getFieldByIndex(fieldIndex);
        
        if (field == null || field.getTimeSlots() == null) {
            return new LinkedHashMap<>();
        }
        
        Map<String, List<TimeSlotDTO>> grouped = new LinkedHashMap<>();
        grouped.put("Sáng", new ArrayList<>());
        grouped.put("Chiều", new ArrayList<>());
        grouped.put("Tối", new ArrayList<>());
        
        for (TimeSlotDTO slot : field.getTimeSlots()) {
            String period = slot.getTimePeriod();
            grouped.get(period).add(slot);
        }
        
        return grouped;
    }
    
    /**
     * Check if any field has available slots
     */
    public boolean hasAvailableSlots() {
        return getTotalAvailableSlots() > 0;
    }
    
    /**
     * Get formatted date for display
     */
    public String getFormattedDate() {
        if (bookingDate == null) {
            return "";
        }
        
        try {
            // Parse "2025-04-22" and format as "22/04/2025"
            String[] parts = bookingDate.split("-");
            if (parts.length == 3) {
                return parts[2] + "/" + parts[1] + "/" + parts[0];
            }
        } catch (Exception e) {
            // Return original if parsing fails
        }
        
        return bookingDate;
    }
    
    /**
     * Get day of week from booking date
     */
    public String getDayOfWeek() {
        if (bookingDate == null) {
            return "";
        }
        
        try {
            // This is a simplified version
            // For production, use java.time.LocalDate to get actual day
            return "Thu"; // Placeholder
        } catch (Exception e) {
            return "";
        }
    }
    
    @Override
    public String toString() {
        return "FieldBookingResponse{" +
                "facilityId=" + facilityId +
                ", facilityName='" + facilityName + '\'' +
                ", bookingDate='" + bookingDate + '\'' +
                ", fields=" + getFieldCount() +
                ", totalSlots=" + getTotalSlots() +
                ", availableSlots=" + getTotalAvailableSlots() +
                '}';
    }
}
