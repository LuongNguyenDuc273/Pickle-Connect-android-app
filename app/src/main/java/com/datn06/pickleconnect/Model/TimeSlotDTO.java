package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;

/**
 * DTO representing a time slot for field booking
 */
public class TimeSlotDTO {
    
    @SerializedName("slotId")
    private Long slotId;
    
    @SerializedName("startTime")
    private String startTime;  // Format: "07:00:00" or "07:00"
    
    @SerializedName("endTime")
    private String endTime;    // Format: "08:00:00" or "08:00"
    
    @SerializedName("slotLabel")
    private String slotLabel;  // Format: "7:00-8:00"
    
    @SerializedName("slotStatus")
    private String slotStatus; // "AVAILABLE", "BOOKED", etc.
    
    @SerializedName("fixedPrice")
    private BigDecimal fixedPrice;  // Price for fixed booking
    
    @SerializedName("walkinPrice")
    private BigDecimal walkinPrice; // Price for walk-in
    
    @SerializedName("isAvailable")
    private Boolean isAvailable;
    
    // Constructor
    public TimeSlotDTO() {}
    
    public TimeSlotDTO(Long slotId, String startTime, String endTime, String slotLabel,
                       String slotStatus, BigDecimal fixedPrice, BigDecimal walkinPrice,
                       Boolean isAvailable) {
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotLabel = slotLabel;
        this.slotStatus = slotStatus;
        this.fixedPrice = fixedPrice;
        this.walkinPrice = walkinPrice;
        this.isAvailable = isAvailable;
    }
    
    // Getters and Setters
    public Long getSlotId() {
        return slotId;
    }
    
    public void setSlotId(Long slotId) {
        this.slotId = slotId;
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
    
    public String getSlotLabel() {
        return slotLabel;
    }
    
    public void setSlotLabel(String slotLabel) {
        this.slotLabel = slotLabel;
    }
    
    public String getSlotStatus() {
        return slotStatus;
    }
    
    public void setSlotStatus(String slotStatus) {
        this.slotStatus = slotStatus;
    }
    
    public BigDecimal getFixedPrice() {
        return fixedPrice;
    }
    
    public void setFixedPrice(BigDecimal fixedPrice) {
        this.fixedPrice = fixedPrice;
    }
    
    public BigDecimal getWalkinPrice() {
        return walkinPrice;
    }
    
    public void setWalkinPrice(BigDecimal walkinPrice) {
        this.walkinPrice = walkinPrice;
    }
    
    public Boolean getIsAvailable() {
        return isAvailable;
    }
    
    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
    
    // Helper methods
    
    /**
     * Get slot status as enum
     */
    public SlotStatus getStatusEnum() {
        if (slotStatus == null) {
            return SlotStatus.UNAVAILABLE;
        }
        
        try {
            return SlotStatus.valueOf(slotStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SlotStatus.UNAVAILABLE;
        }
    }
    
    /**
     * Format price for display (e.g., "80k", "100k")
     */
    public String getFormattedPrice() {
        if (fixedPrice == null) {
            return "-";
        }
        
        long value = fixedPrice.longValue();
        if (value >= 1000) {
            return (value / 1000) + "k";
        }
        return value + "";
    }
    
    /**
     * Get full price with currency (e.g., "80,000đ")
     */
    public String getFullFormattedPrice() {
        if (fixedPrice == null) {
            return "Liên hệ";
        }
        
        return String.format("%,dđ", fixedPrice.intValue());
    }
    
    /**
     * Get time period (morning/afternoon/evening)
     */
    public String getTimePeriod() {
        if (startTime == null) {
            return "Khác";
        }
        
        int hour = parseHour(startTime);
        
        if (hour >= 6 && hour < 12) {
            return "Sáng";
        } else if (hour >= 12 && hour < 18) {
            return "Chiều";
        } else {
            return "Tối";
        }
    }
    
    /**
     * Parse hour from time string
     */
    private int parseHour(String time) {
        try {
            String hourStr = time.split(":")[0];
            return Integer.parseInt(hourStr);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if this slot is in the past
     */
    public boolean isPast() {
        // Implementation depends on current time comparison
        // For now, return false (backend should handle this)
        return false;
    }
    
    @Override
    public String toString() {
        return "TimeSlotDTO{" +
                "slotId=" + slotId +
                ", slotLabel='" + slotLabel + '\'' +
                ", status=" + slotStatus +
                ", price=" + getFormattedPrice() +
                ", available=" + isAvailable +
                '}';
    }
}
