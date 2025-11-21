package com.datn06.pickleconnect.Model;

/**
 * Enum representing the status of a time slot
 */
public enum SlotStatus {
    /**
     * Slot is available for booking (cyan/blue color)
     */
    AVAILABLE,
    
    /**
     * Slot is already booked by someone (gray color)
     */
    BOOKED,
    
    /**
     * Slot is reserved temporarily (pending payment)
     */
    RESERVED,
    
    /**
     * Slot is not available (e.g., past time, maintenance)
     */
    UNAVAILABLE
}
