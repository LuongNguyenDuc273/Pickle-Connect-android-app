package com.datn06.pickleconnect.Model;

import com.google.gson.annotations.SerializedName;
import java.math.BigInteger;

public class BannerDTO {
    @SerializedName("bannerId")
    private String bannerId;

    @SerializedName("title")
    private String title;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("linkUrl")
    private String linkUrl;

    @SerializedName("type")
    private String type;

    @SerializedName("status")
    private Integer status;

    // Constructor
    public BannerDTO() {}

    // Getters and Setters
    public String getBannerId() {
        return bannerId;
    }

    public void setBannerId(String bannerId) {
        this.bannerId = bannerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
