package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigInteger;

/**
 * DTO for field detail information
 * Used in CourtDetailResponse to show list of fields in a facility
 */
public class FieldDetailDTO {

    @SerializedName("fieldId")
    private BigInteger fieldId;

    @SerializedName("fieldName")
    private String fieldName;

    @SerializedName("location")
    private String location;

    @SerializedName("description")
    private String description;

    // Constructor
    public FieldDetailDTO() {}

    public FieldDetailDTO(BigInteger fieldId, String fieldName, String location, String description) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.location = location;
        this.description = description;
    }

    // Getters and Setters
    public BigInteger getFieldId() {
        return fieldId;
    }

    public void setFieldId(BigInteger fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
