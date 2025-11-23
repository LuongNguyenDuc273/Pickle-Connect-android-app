package com.datn06.pickleconnect.Search;

import com.datn06.pickleconnect.Model.FacilityDTO;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SearchResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private SearchData data;

    // Constructor
    public SearchResponse() {}

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

    public SearchData getData() {
        return data;
    }

    public void setData(SearchData data) {
        this.data = data;
    }

    // Inner class for data (FacilitySearchResponse)
    public static class SearchData {
        @SerializedName("facilities")
        private List<FacilityDTO> facilities;

        @SerializedName("totalCount")
        private Integer totalCount;

        @SerializedName("currentPage")
        private Integer currentPage;

        @SerializedName("totalPages")
        private Integer totalPages;

        public SearchData() {}

        public List<FacilityDTO> getFacilities() {
            return facilities;
        }

        public void setFacilities(List<FacilityDTO> facilities) {
            this.facilities = facilities;
        }

        public Integer getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(Integer totalCount) {
            this.totalCount = totalCount;
        }

        public Integer getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(Integer currentPage) {
            this.currentPage = currentPage;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }
    }
}