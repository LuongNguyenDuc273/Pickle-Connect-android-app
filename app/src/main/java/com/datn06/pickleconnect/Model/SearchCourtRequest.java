package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class SearchCourtRequest {
    
    @SerializedName("facilityName")
    private String facilityName;
    
    @SerializedName("userLatitude")
    private BigDecimal userLatitude;
    
    @SerializedName("userLongitude")
    private BigDecimal userLongitude;
    
    @SerializedName("maxDistanceKm")
    private Double maxDistanceKm;
    
    @SerializedName("provinces")
    private List<String> provinces;
    
    @SerializedName("districts")
    private List<String> districts;
    
    @SerializedName("wards")
    private List<String> wards;
    
    @SerializedName("page")
    private Integer page;
    
    @SerializedName("size")
    private Integer size;
    
    // Constructor
    public SearchCourtRequest() {}
    
    // Builder pattern
    public static class Builder {
        private SearchCourtRequest request = new SearchCourtRequest();
        
        public Builder facilityName(String facilityName) {
            request.facilityName = facilityName;
            return this;
        }
        
        public Builder userLatitude(BigDecimal userLatitude) {
            request.userLatitude = userLatitude;
            return this;
        }
        
        public Builder userLongitude(BigDecimal userLongitude) {
            request.userLongitude = userLongitude;
            return this;
        }
        
        public Builder maxDistanceKm(Double maxDistanceKm) {
            request.maxDistanceKm = maxDistanceKm;
            return this;
        }
        
        public Builder provinces(List<String> provinces) {
            request.provinces = provinces;
            return this;
        }
        
        public Builder districts(List<String> districts) {
            request.districts = districts;
            return this;
        }
        
        public Builder wards(List<String> wards) {
            request.wards = wards;
            return this;
        }
        
        public Builder page(Integer page) {
            request.page = page;
            return this;
        }
        
        public Builder size(Integer size) {
            request.size = size;
            return this;
        }
        
        public SearchCourtRequest build() {
            return request;
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Getters and Setters
    public String getFacilityName() {
        return facilityName;
    }
    
    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }
    
    public BigDecimal getUserLatitude() {
        return userLatitude;
    }
    
    public void setUserLatitude(BigDecimal userLatitude) {
        this.userLatitude = userLatitude;
    }
    
    public BigDecimal getUserLongitude() {
        return userLongitude;
    }
    
    public void setUserLongitude(BigDecimal userLongitude) {
        this.userLongitude = userLongitude;
    }
    
    public Double getMaxDistanceKm() {
        return maxDistanceKm;
    }
    
    public void setMaxDistanceKm(Double maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }
    
    public List<String> getProvinces() {
        return provinces;
    }
    
    public void setProvinces(List<String> provinces) {
        this.provinces = provinces;
    }
    
    public List<String> getDistricts() {
        return districts;
    }
    
    public void setDistricts(List<String> districts) {
        this.districts = districts;
    }
    
    public List<String> getWards() {
        return wards;
    }
    
    public void setWards(List<String> wards) {
        this.wards = wards;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
}
