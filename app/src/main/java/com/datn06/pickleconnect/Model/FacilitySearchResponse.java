package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class FacilitySearchResponse {
    
    @SerializedName("facilities")
    private List<FacilityDTO> facilities;
    
    @SerializedName("totalCount")
    private Long totalCount;
    
    @SerializedName("currentPage")
    private Integer currentPage;
    
    @SerializedName("totalPages")
    private Integer totalPages;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("requestID")
    private String requestID;
    
    // Constructor
    public FacilitySearchResponse() {}
    
    // Getters and Setters
    public List<FacilityDTO> getFacilities() {
        return facilities;
    }
    
    public void setFacilities(List<FacilityDTO> facilities) {
        this.facilities = facilities;
    }
    
    public Long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
    
    public Integer getCurrentPage() {
        return currentPage;
    }
    
    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getRequestID() {
        return requestID;
    }
    
    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
}
