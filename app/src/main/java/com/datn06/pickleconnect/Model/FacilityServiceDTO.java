package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * DTO for facility service information (amenities like parking, shower, etc.)
 * Used in CourtDetailResponse
 */
public class FacilityServiceDTO {

    @SerializedName("serviceId")
    private BigInteger serviceId;

    @SerializedName("serviceName")
    private String serviceName;

    @SerializedName("price")
    private BigDecimal price;

    @SerializedName("status")
    private Integer status;

    // Constructor
    public FacilityServiceDTO() {}

    public FacilityServiceDTO(BigInteger serviceId, String serviceName, BigDecimal price, Integer status) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.price = price;
        this.status = status;
    }

    // Getters and Setters
    public BigInteger getServiceId() {
        return serviceId;
    }

    public void setServiceId(BigInteger serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    // Helper methods
    public boolean isActive() {
        return status != null && status == 1;
    }

    public String getFormattedPrice() {
        if (price == null) {
            return "Miễn phí";
        }
        return String.format("%,dđ", price.intValue());
    }
}
