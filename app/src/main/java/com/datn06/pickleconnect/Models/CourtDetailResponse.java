package com.datn06.pickleconnect.Models;

import com.datn06.pickleconnect.Model.FacilityReviewDTO;
import com.datn06.pickleconnect.Model.FacilityServiceDTO;
import com.datn06.pickleconnect.Model.FieldDetailDTO;
import com.datn06.pickleconnect.Model.FieldPriceDTO;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * Response DTO for court detail
 * Maps to backend FacilityDetailDTO
 */
public class CourtDetailResponse {

    @SerializedName("facilityId")
    private BigInteger facilityId;

    @SerializedName("facilityName")
    private String facilityName;

    @SerializedName("userId")
    private String userId;

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

    @SerializedName("fields")
    private List<FieldDetailDTO> fields;

    @SerializedName("services")
    private List<FacilityServiceDTO> services;

    @SerializedName("reviews")
    private List<FacilityReviewDTO> reviews;

    @SerializedName("prices")
    private List<FieldPriceDTO> prices;

    // Constructor
    public CourtDetailResponse() {}

    // Getters and Setters
    public BigInteger getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(BigInteger facilityId) {
        this.facilityId = facilityId;
    }

    public String getFacilityName() {
        return facilityName;
    }

    public void setFacilityName(String facilityName) {
        this.facilityName = facilityName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public List<FieldDetailDTO> getFields() {
        return fields;
    }

    public void setFields(List<FieldDetailDTO> fields) {
        this.fields = fields;
    }

    public List<FacilityServiceDTO> getServices() {
        return services;
    }

    public void setServices(List<FacilityServiceDTO> services) {
        this.services = services;
    }

    public List<FacilityReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<FacilityReviewDTO> reviews) {
        this.reviews = reviews;
    }

    public List<FieldPriceDTO> getPrices() {
        return prices;
    }

    public void setPrices(List<FieldPriceDTO> prices) {
        this.prices = prices;
    }

    // Helper methods

    /**
     * Get full address string
     */
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s",
                streetAddress, ward, district, province);
    }

    /**
     * Get number of fields
     */
    public int getFieldCount() {
        return fields != null ? fields.size() : 0;
    }

    /**
     * Get number of services
     */
    public int getServiceCount() {
        return services != null ? services.size() : 0;
    }

    /**
     * Get number of reviews
     */
    public int getReviewCount() {
        return reviews != null ? reviews.size() : 0;
    }

    /**
     * Calculate average rating from reviews
     */
    public double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        double sum = 0;
        for (FacilityReviewDTO review : reviews) {
            if (review.getRating() != null) {
                sum += review.getRating().doubleValue();
            }
        }

        return sum / reviews.size();
    }

    /**
     * Get price range from prices list
     */
    public String getPriceRange() {
        if (prices == null || prices.isEmpty()) {
            return "Liên hệ";
        }

        BigDecimal min = null;
        BigDecimal max = null;

        for (FieldPriceDTO price : prices) {
            BigDecimal fixed = price.getFixedPrice();
            if (fixed != null) {
                if (min == null || fixed.compareTo(min) < 0) {
                    min = fixed;
                }
                if (max == null || fixed.compareTo(max) > 0) {
                    max = fixed;
                }
            }
        }

        if (min != null && max != null) {
            if (min.equals(max)) {
                return String.format("%,dđ", min.intValue());
            } else {
                return String.format("%,dđ - %,dđ", min.intValue(), max.intValue());
            }
        }

        return "Liên hệ";
    }
}
