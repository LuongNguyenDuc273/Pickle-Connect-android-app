package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;

public class FacilityDTO {
    @SerializedName("facilityId")
    private Long facilityId;

    @SerializedName("facilityName")
    private String facilityName;

    @SerializedName("streetAddress")
    private String streetAddress;

    @SerializedName("ward")
    private String ward;

    @SerializedName("district")
    private String district;

    @SerializedName("province")
    private String province;

    @SerializedName("contactInfo")
    private String contactInfo;

    @SerializedName("status")
    private Integer status;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("distanceKm")
    private Double distanceKm;

    @SerializedName("fullAddress")
    private String fullAddress;

    @SerializedName("imageUrls")
    private List<String> imageUrls;

    @SerializedName("priceRangeMin")
    private BigDecimal priceRangeMin;

    @SerializedName("priceRangeMax")
    private BigDecimal priceRangeMax;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("services")
    private List<String> services;

    // Constructor
    public FacilityDTO() {}

    // Getters and Setters
    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public BigDecimal getPriceRangeMin() {
        return priceRangeMin;
    }

    public void setPriceRangeMin(BigDecimal priceRangeMin) {
        this.priceRangeMin = priceRangeMin;
    }

    public BigDecimal getPriceRangeMax() {
        return priceRangeMax;
    }

    public void setPriceRangeMax(BigDecimal priceRangeMax) {
        this.priceRangeMax = priceRangeMax;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public List<String> getServices() {
        return services;
    }

    public void setServices(List<String> services) {
        this.services = services;
    }

    // Helper method to get first image
    public String getFirstImageUrl() {
        if (imageUrls != null && !imageUrls.isEmpty()) {
            return imageUrls.get(0);
        }
        return null;
    }

    // Helper method to format price range
    public String getFormattedPriceRange() {
        if (priceRangeMin != null && priceRangeMax != null) {
            return String.format("%,d - %,d VNĐ",
                    priceRangeMin.intValue(),
                    priceRangeMax.intValue());
        }
        return "Liên hệ";
    }

    // Helper method to format distance
    public String getFormattedDistance() {
        if (distanceKm != null) {
            if (distanceKm < 1) {
                return String.format("%.0f m", distanceKm * 1000);
            } else {
                return String.format("%.1f km", distanceKm);
            }
        }
        return "";
    }
}
