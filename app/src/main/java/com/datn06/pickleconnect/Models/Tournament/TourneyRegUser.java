package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

/**
 * User data for tournament registration
 * Used within TourneyRegRequest
 */
public class TourneyRegUser {
    
    @SerializedName("userIdAlias")
    private String userIdAlias;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("phoneNumber")
    private String phoneNumber;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("dateOfBirth")
    private String dateOfBirth;  // Format: dd-MM-yyyy
    
    @SerializedName("gender")
    private String gender;  // "0" = Female, "1" = Male
    
    @SerializedName("pointRanking")
    private String pointRanking;  // Can be null/empty
    
    // Constructors
    public TourneyRegUser() {}
    
    public TourneyRegUser(String userIdAlias, String username, String phoneNumber, 
                          String email, String dateOfBirth, String gender, String pointRanking) {
        this.userIdAlias = userIdAlias;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.pointRanking = pointRanking;
    }
    
    // Getters and Setters
    public String getUserIdAlias() {
        return userIdAlias;
    }
    
    public void setUserIdAlias(String userIdAlias) {
        this.userIdAlias = userIdAlias;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getPointRanking() {
        return pointRanking;
    }
    
    public void setPointRanking(String pointRanking) {
        this.pointRanking = pointRanking;
    }
}
