package com.datn06.pickleconnect.Models;

import com.datn06.pickleconnect.Common.BaseRequest;
import com.google.gson.annotations.SerializedName;

public class UpdateMemberRequest extends BaseRequest {
    private String userId;
    private String fullName;
    private String dateOfBirth;
    private String gender;

    // ✅ Thêm @SerializedName để map heightCm → height
    @SerializedName("height")
    private Double heightCm;

    // ✅ Thêm @SerializedName để map weightKg → weight
    @SerializedName("weight")
    private Double weightKg;

    private String province;
    private String district;
    private String address;

    @SerializedName("userImageUrl")
    private String avatarUrl;

    public UpdateMemberRequest(String userId) {
        this.userId = userId;
        setRequestId(String.valueOf(System.currentTimeMillis()));
    }

    // Giữ nguyên getters và setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getWeightKg() { return weightKg; }
    public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}