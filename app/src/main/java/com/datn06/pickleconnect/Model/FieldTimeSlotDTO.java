package com.datn06.pickleconnect.Model;

public class FieldTimeSlotDTO {
    private Long slotId;
    private Long fieldId;
    private String fieldName;
    private String slotDate; // LocalDateTime from backend -> String in Android
    private String startTime; // LocalTime from backend -> String in Android (e.g., "06:30")
    private String endTime;   // LocalTime from backend -> String in Android (e.g., "08:00")
    private String slotLabel; // e.g., "6:30 - 8:00"
    private Integer status;

    public FieldTimeSlotDTO() {
    }

    public FieldTimeSlotDTO(Long slotId, Long fieldId, String fieldName, String slotDate, 
                            String startTime, String endTime, String slotLabel, Integer status) {
        this.slotId = slotId;
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.slotDate = slotDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotLabel = slotLabel;
        this.status = status;
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

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public void setSlotDate(String slotDate) {
        this.slotDate = slotDate;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * Helper method to get time slot display text
     * @return Formatted time slot like "6:30 - 8:00"
     */
    public String getTimeSlotDisplay() {
        if (slotLabel != null && !slotLabel.isEmpty()) {
            return slotLabel;
        }
        if (startTime != null && endTime != null) {
            return startTime + " - " + endTime;
        }
        return "N/A";
    }
}
