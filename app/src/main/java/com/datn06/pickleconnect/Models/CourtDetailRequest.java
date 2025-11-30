package com.datn06.pickleconnect.Models;

import com.datn06.pickleconnect.Common.BaseRequest;
import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;

// ===================== REQUEST DTO =====================

/**
 * Request DTO for getting court detail
 * Backend: GET /court/detail
 */
public class CourtDetailRequest extends BaseRequest {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("facilityId")
    private BigInteger facilityId;

    // Constructor
    public CourtDetailRequest() {
        super();
    }

    public CourtDetailRequest(Long userId, BigInteger facilityId) {
        super();
        this.userId = userId;
        this.facilityId = facilityId;
        setRequestId(String.valueOf(System.currentTimeMillis()));
    }

    // Convenience constructor with Long facilityId
    public CourtDetailRequest(Long userId, Long facilityId) {
        this(userId, BigInteger.valueOf(facilityId));
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigInteger getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(BigInteger facilityId) {
        this.facilityId = facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = BigInteger.valueOf(facilityId);
    }
}