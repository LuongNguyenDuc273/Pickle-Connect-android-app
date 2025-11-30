package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

/**
 * Request for getting tournament list
 * Endpoint: POST /tourney-list
 */
public class TourneyListRequest extends BaseTournamentRequest {

    @SerializedName("searchType")
    private String searchType; // "1"=ongoing, "2"=upcoming, "3"=past, "4"=user tournaments

    @SerializedName("searchKeyword")
    private String searchKeyword; // Optional - search by tournament name

    @SerializedName("searchLocation")
    private String searchLocation; // Optional - search by location

    @SerializedName("page")
    private int page; // Page number (starts from 1)

    @SerializedName("size")
    private int size; // Page size

    public TourneyListRequest() {
        super();
        this.page = 1;
        this.size = 10;
    }

    public TourneyListRequest(String userId, String searchType) {
        super(userId);
        this.searchType = searchType;
        this.page = 1;
        this.size = 10;
    }

    // Getters and Setters
    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getSearchLocation() {
        return searchLocation;
    }

    public void setSearchLocation(String searchLocation) {
        this.searchLocation = searchLocation;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}