package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response for tournament registration form field configuration
 */
public class TourneyRegConfigResponse {

    @SerializedName("formFieldId")
    private String formFieldId;

    @SerializedName("tournamentId")
    private String tournamentId;

    @SerializedName("label")
    private String label; // Display label for the field

    @SerializedName("fieldName")
    private String fieldName; // Internal field name

    @SerializedName("fieldType")
    private String fieldType; // text, number, date, select, radio, checkbox, etc.

    @SerializedName("isRequired")
    private Boolean isRequired;

    @SerializedName("options")
    private List<String> options; // For select/radio/checkbox fields

    @SerializedName("displayOrder")
    private Integer displayOrder;

    // Getters and Setters
    public String getFormFieldId() {
        return formFieldId;
    }

    public void setFormFieldId(String formFieldId) {
        this.formFieldId = formFieldId;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    // Helper methods
    public boolean isSelectType() {
        return "select".equalsIgnoreCase(fieldType);
    }

    public boolean isTextType() {
        return "text".equalsIgnoreCase(fieldType);
    }

    public boolean isNumberType() {
        return "number".equalsIgnoreCase(fieldType);
    }

    public boolean isDateType() {
        return "date".equalsIgnoreCase(fieldType);
    }

    public boolean isRadioType() {
        return "radio".equalsIgnoreCase(fieldType);
    }

    public boolean isCheckboxType() {
        return "checkbox".equalsIgnoreCase(fieldType);
    }

    public boolean hasOptions() {
        return options != null && !options.isEmpty();
    }
}