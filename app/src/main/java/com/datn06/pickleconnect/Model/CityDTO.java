package com.datn06.pickleconnect.Model;

import java.math.BigInteger;

/**
 * DTO for City/Province data
 * Matches backend: com.vnpay.bean.court.CityDTO
 */
public class CityDTO {
    private BigInteger cityId;
    private String name;
    private Integer status;

    public CityDTO() {
    }

    public CityDTO(BigInteger cityId, String name, Integer status) {
        this.cityId = cityId;
        this.name = name;
        this.status = status;
    }

    public BigInteger getCityId() {
        return cityId;
    }

    public void setCityId(BigInteger cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return name; // For Spinner display
    }
}
