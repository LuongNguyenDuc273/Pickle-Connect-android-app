package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;

public class EventFieldDTO {
    @SerializedName("fieldId")
    private Long fieldId;

    @SerializedName("fieldName")
    private String fieldName;

    @SerializedName("description")
    private String description;

    // Constructor
    public EventFieldDTO() {}

    public EventFieldDTO(Long fieldId, String fieldName, String description) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.description = description;
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
}