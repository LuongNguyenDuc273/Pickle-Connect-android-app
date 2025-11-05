package com.datn06.pickleconnect.Home;

import com.datn06.pickleconnect.Model.BannerDTO;
import com.datn06.pickleconnect.Model.FacilityDTO;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class HomeResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private HomeData data;

    // Constructor
    public HomeResponse() {}

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HomeData getData() {
        return data;
    }

    public void setData(HomeData data) {
        this.data = data;
    }

    // Inner class for data
    public static class HomeData {
        @SerializedName("banners")
        private List<BannerDTO> banners;

        @SerializedName("featuredFacilities")
        private List<FacilityDTO> featuredFacilities;

        public HomeData() {}

        public List<BannerDTO> getBanners() {
            return banners;
        }

        public void setBanners(List<BannerDTO> banners) {
            this.banners = banners;
        }

        public List<FacilityDTO> getFeaturedFacilities() {
            return featuredFacilities;
        }

        public void setFeaturedFacilities(List<FacilityDTO> featuredFacilities) {
            this.featuredFacilities = featuredFacilities;
        }
    }
}
