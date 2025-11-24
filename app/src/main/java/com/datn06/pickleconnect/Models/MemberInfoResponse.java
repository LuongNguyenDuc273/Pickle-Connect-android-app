package com.datn06.pickleconnect.Models;

import com.google.gson.annotations.SerializedName;

public class MemberInfoResponse {
    private String userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String heightCm;
    private String weightKg;
    private String province;
    private String district;
    private String address;
    
    @SerializedName("userImageUrl")
    private String avatarUrl;
    
    private Boolean isFirstLogin;

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getHeightCm() { return heightCm; }
    public void setHeightCm(String heightCm) { this.heightCm = heightCm; }
    public String getWeightKg() { return weightKg; }
    public void setWeightKg(String weightKg) { this.weightKg = weightKg; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public Boolean getIsFirstLogin() { return isFirstLogin; }
    public void setIsFirstLogin(Boolean isFirstLogin) { this.isFirstLogin = isFirstLogin; }
}
