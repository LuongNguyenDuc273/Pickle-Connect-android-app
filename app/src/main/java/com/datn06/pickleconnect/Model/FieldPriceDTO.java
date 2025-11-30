package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * DTO for field price information (pricing schedule)
 * Used in CourtDetailResponse to show price table
 * NOTE: This is different from TimeSlotDTO which is for booking availability
 */
public class FieldPriceDTO {

    @SerializedName("priceId")
    private BigInteger priceId;

    @SerializedName("weekday")
    private String weekday;  // 0=Sunday, 1=Monday, ..., 6=Saturday

    @SerializedName("startTime")
    private String startTime;  // Format: "07:00:00"

    @SerializedName("endTime")
    private String endTime;    // Format: "08:00:00"

    @SerializedName("fixedPrice")
    private BigDecimal fixedPrice;

    @SerializedName("walkinPrice")
    private BigDecimal walkinPrice;

    // Constructor
    public FieldPriceDTO() {}

    public FieldPriceDTO(BigInteger priceId, String weekday, String startTime,
                         String endTime, BigDecimal fixedPrice, BigDecimal walkinPrice) {
        this.priceId = priceId;
        this.weekday = weekday;
        this.startTime = startTime;
        this.endTime = endTime;
        this.fixedPrice = fixedPrice;
        this.walkinPrice = walkinPrice;
    }

    // Getters and Setters
    public BigInteger getPriceId() {
        return priceId;
    }

    public void setPriceId(BigInteger priceId) {
        this.priceId = priceId;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
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

    // Helper methods

    /**
     * Get weekday name in Vietnamese
     */


    /**
     * Get time slot label (e.g., "7:00 - 8:00")
     */
    public String getTimeSlotLabel() {
        if (startTime == null || endTime == null) {
            return "";
        }

        String start = startTime.substring(0, 5);  // "07:00:00" -> "07:00"
        String end = endTime.substring(0, 5);
        return start + " - " + end;
    }

    /**
     * Get formatted fixed price
     */
    public String getFormattedFixedPrice() {
        if (fixedPrice == null) {
            return "-";
        }
        return String.format("%,dđ", fixedPrice.intValue());
    }

    /**
     * Get formatted walk-in price
     */
    public String getFormattedWalkinPrice() {
        if (walkinPrice == null) {
            return "-";
        }
        return String.format("%,dđ", walkinPrice.intValue());
    }

    /**
     * Check if weekend
     */
}
