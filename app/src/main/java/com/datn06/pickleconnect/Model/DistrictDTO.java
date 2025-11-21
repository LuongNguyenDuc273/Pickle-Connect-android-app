package com.datn06.pickleconnect.Model;

import java.math.BigInteger;

/**
 * DTO for District data
 * Matches backend: com.vnpay.bean.court.DistrictDTO
 */
public class DistrictDTO {
    private Long districtId;
    private String districtName;
    private BigInteger cityId;
    private Integer status;

    public DistrictDTO() {
    }

    public DistrictDTO(Long districtId, String districtName, BigInteger cityId, Integer status) {
        this.districtId = districtId;
        this.districtName = districtName;
        this.cityId = cityId;
        this.status = status;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public BigInteger getCityId() {
        return cityId;
    }

    public void setCityId(BigInteger cityId) {
        this.cityId = cityId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return districtName; // For Spinner display
    }
}
