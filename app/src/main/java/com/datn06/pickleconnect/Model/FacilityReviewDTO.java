package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * DTO for facility review information
 * Used in CourtDetailResponse
 */
public class FacilityReviewDTO {

    @SerializedName("reviewId")
    private BigInteger reviewId;

    @SerializedName("userId")
    private BigInteger userId;

    @SerializedName("username")
    private String username;

    @SerializedName("rating")
    private BigDecimal rating;

    @SerializedName("comment")
    private String comment;

    // Constructor
    public FacilityReviewDTO() {}

    public FacilityReviewDTO(BigInteger reviewId, BigInteger userId, String username,
                             BigDecimal rating, String comment) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public BigInteger getReviewId() {
        return reviewId;
    }

    public void setReviewId(BigInteger reviewId) {
        this.reviewId = reviewId;
    }

    public BigInteger getUserId() {
        return userId;
    }

    public void setUserId(BigInteger userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Helper methods
    public int getRatingStars() {
        return rating != null ? rating.intValue() : 0;
    }

    public String getDisplayName() {
        return username != null ? username : "Ẩn danh";
    }
}
