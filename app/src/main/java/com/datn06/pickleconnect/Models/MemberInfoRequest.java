package com.datn06.pickleconnect.Models;

import com.datn06.pickleconnect.Common.BaseRequest;

public class MemberInfoRequest extends BaseRequest {
    private String userId;
    private String email;
    private String phoneNumber;

    public MemberInfoRequest(String userId, String email, String phoneNumber) {
        this.userId = userId;
        this.email = email;
        this.phoneNumber = phoneNumber;
        setRequestId(String.valueOf(System.currentTimeMillis()));
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
