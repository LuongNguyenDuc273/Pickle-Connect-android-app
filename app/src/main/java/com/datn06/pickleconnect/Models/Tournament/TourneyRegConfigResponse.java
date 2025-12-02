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

    @SerializedName("fieldName")
    private String fieldName;

    @SerializedName("fieldType")
    private String fieldType; // textbox, select, checkbox, radio

    @SerializedName("label")
    private String label;

    @SerializedName("isRequired")
    private Boolean isRequired;

    @SerializedName("options")
    private List<String> options;

    @SerializedName("displayOrder")
    private String displayOrder;

    // Field value được user nhập (không từ API)
    private String value;
    private List<String> selectedValues; // For checkbox

    // Getters and Setters
    public String getFormFieldId() { return formFieldId; }
    public void setFormFieldId(String formFieldId) { this.formFieldId = formFieldId; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Boolean getIsRequired() { return isRequired != null ? isRequired : false; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(String displayOrder) { this.displayOrder = displayOrder; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public List<String> getSelectedValues() { return selectedValues; }
    public void setSelectedValues(List<String> selectedValues) { this.selectedValues = selectedValues; }
}