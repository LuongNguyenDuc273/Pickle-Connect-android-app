package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response for tournament list item
 * ✅ FIXED: Changed @SerializedName from snake_case to camelCase to match API
 */
public class TourneyListResponse {

    // ✅ Changed from "tournament_id" to "tournamentId"
    @SerializedName("tournamentId")
    private String tournamentId;

    // ✅ Changed from "tournament_name" to "tournamentName"
    @SerializedName("tournamentName")
    private String tournamentName;

    // ✅ Changed from "tournament_img" to "tournamentImg"
    @SerializedName("tournamentImg")
    private String tournamentImg;

    // ✅ Changed from "tournament_location" to "tournamentLocation"
    @SerializedName("tournamentLocation")
    private String tournamentLocation;

    // ✅ Changed from "organizer_name" to "organizerName"
    @SerializedName("organizerName")
    private String organizerName;

    // ✅ Changed from "organizer_logo" to "organizerLogo"
    @SerializedName("organizerLogo")
    private String organizerLogo;

    // ✅ Changed from "start_date" to "startDate"
    @SerializedName("startDate")
    private String startDate; // Format: "dd-MM-yyyy HH:mm:ss"

    // ✅ Changed from "end_date" to "endDate"
    @SerializedName("endDate")
    private String endDate; // Format: "dd-MM-yyyy HH:mm:ss"

    // ✅ Changed from "reg_start_date" to "regStartDate"
    @SerializedName("regStartDate")
    private String regStartDate;

    // ✅ Changed from "reg_end_date" to "regEndDate"
    @SerializedName("regEndDate")
    private String regEndDate;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status;

    // ✅ Changed from "tournament_images" to "tournamentImages"
    @SerializedName("tournamentImages")
    private List<ImageList> tournamentImages;

    // ✅ Changed from "single_male_participants" to "singleMaleParticipants"
    @SerializedName("singleMaleParticipants")
    private int singleMaleParticipants;

    // ✅ Changed from "single_female_participants" to "singleFemaleParticipants"
    @SerializedName("singleFemaleParticipants")
    private int singleFemaleParticipants;

    // ✅ Changed from "double_male_participants" to "doubleMaleParticipants"
    @SerializedName("doubleMaleParticipants")
    private int doubleMaleParticipants;

    // ✅ Changed from "double_female_participants" to "doubleFemaleParticipants"
    @SerializedName("doubleFemaleParticipants")
    private int doubleFemaleParticipants;

    // ✅ Changed from "double_mixed_participants" to "doubleMixedParticipants"
    @SerializedName("doubleMixedParticipants")
    private int doubleMixedParticipants;

    // ✅ Changed from "single_male_max_participants" to "singleMaleMaxParticipants"
    @SerializedName("singleMaleMaxParticipants")
    private int singleMaleMaxParticipants;

    // ✅ Changed from "single_female_max_participants" to "singleFemaleMaxParticipants"
    @SerializedName("singleFemaleMaxParticipants")
    private int singleFemaleMaxParticipants;

    // ✅ Changed from "double_male_max_participants" to "doubleMaleMaxParticipants"
    @SerializedName("doubleMaleMaxParticipants")
    private int doubleMaleMaxParticipants;

    // ✅ Changed from "double_female_max_participants" to "doubleFemaleMaxParticipants"
    @SerializedName("doubleFemaleMaxParticipants")
    private int doubleFemaleMaxParticipants;

    // ✅ Changed from "double_mixed_max_participants" to "doubleMixedMaxParticipants"
    @SerializedName("doubleMixedMaxParticipants")
    private int doubleMixedMaxParticipants;

    // Inner class for images
    public static class ImageList {
        // ✅ Changed from "image_url" to "imageUrl"
        @SerializedName("imageUrl")
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    // Getters and Setters
    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public String getTournamentImg() {
        return tournamentImg;
    }

    public void setTournamentImg(String tournamentImg) {
        this.tournamentImg = tournamentImg;
    }

    public String getTournamentLocation() {
        return tournamentLocation;
    }

    public void setTournamentLocation(String tournamentLocation) {
        this.tournamentLocation = tournamentLocation;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public String getOrganizerLogo() {
        return organizerLogo;
    }

    public void setOrganizerLogo(String organizerLogo) {
        this.organizerLogo = organizerLogo;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRegStartDate() {
        return regStartDate;
    }

    public void setRegStartDate(String regStartDate) {
        this.regStartDate = regStartDate;
    }

    public String getRegEndDate() {
        return regEndDate;
    }

    public void setRegEndDate(String regEndDate) {
        this.regEndDate = regEndDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ImageList> getTournamentImages() {
        return tournamentImages;
    }

    public void setTournamentImages(List<ImageList> tournamentImages) {
        this.tournamentImages = tournamentImages;
    }

    public int getSingleMaleParticipants() {
        return singleMaleParticipants;
    }

    public void setSingleMaleParticipants(int singleMaleParticipants) {
        this.singleMaleParticipants = singleMaleParticipants;
    }

    public int getSingleFemaleParticipants() {
        return singleFemaleParticipants;
    }

    public void setSingleFemaleParticipants(int singleFemaleParticipants) {
        this.singleFemaleParticipants = singleFemaleParticipants;
    }

    public int getDoubleMaleParticipants() {
        return doubleMaleParticipants;
    }

    public void setDoubleMaleParticipants(int doubleMaleParticipants) {
        this.doubleMaleParticipants = doubleMaleParticipants;
    }

    public int getDoubleFemaleParticipants() {
        return doubleFemaleParticipants;
    }

    public void setDoubleFemaleParticipants(int doubleFemaleParticipants) {
        this.doubleFemaleParticipants = doubleFemaleParticipants;
    }

    public int getDoubleMixedParticipants() {
        return doubleMixedParticipants;
    }

    public void setDoubleMixedParticipants(int doubleMixedParticipants) {
        this.doubleMixedParticipants = doubleMixedParticipants;
    }

    public int getSingleMaleMaxParticipants() {
        return singleMaleMaxParticipants;
    }

    public void setSingleMaleMaxParticipants(int singleMaleMaxParticipants) {
        this.singleMaleMaxParticipants = singleMaleMaxParticipants;
    }

    public int getSingleFemaleMaxParticipants() {
        return singleFemaleMaxParticipants;
    }

    public void setSingleFemaleMaxParticipants(int singleFemaleMaxParticipants) {
        this.singleFemaleMaxParticipants = singleFemaleMaxParticipants;
    }

    public int getDoubleMaleMaxParticipants() {
        return doubleMaleMaxParticipants;
    }

    public void setDoubleMaleMaxParticipants(int doubleMaleMaxParticipants) {
        this.doubleMaleMaxParticipants = doubleMaleMaxParticipants;
    }

    public int getDoubleFemaleMaxParticipants() {
        return doubleFemaleMaxParticipants;
    }

    public void setDoubleFemaleMaxParticipants(int doubleFemaleMaxParticipants) {
        this.doubleFemaleMaxParticipants = doubleFemaleMaxParticipants;
    }

    public int getDoubleMixedMaxParticipants() {
        return doubleMixedMaxParticipants;
    }

    public void setDoubleMixedMaxParticipants(int doubleMixedMaxParticipants) {
        this.doubleMixedMaxParticipants = doubleMixedMaxParticipants;
    }

    // Helper methods
    public int getTotalParticipants() {
        return singleMaleParticipants + singleFemaleParticipants +
                doubleMaleParticipants + doubleFemaleParticipants +
                doubleMixedParticipants;
    }

    public int getTotalMaxParticipants() {
        return singleMaleMaxParticipants + singleFemaleMaxParticipants +
                doubleMaleMaxParticipants + doubleFemaleMaxParticipants +
                doubleMixedMaxParticipants;
    }

    @Override
    public String toString() {
        return "TourneyListResponse{" +
                "tournamentId='" + tournamentId + '\'' +
                ", tournamentName='" + tournamentName + '\'' +
                ", status='" + status + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", location='" + tournamentLocation + '\'' +
                ", organizerName='" + organizerName + '\'' +
                '}';
    }
}