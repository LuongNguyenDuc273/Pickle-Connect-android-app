package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response for tournament detail
 */
public class TourneyDetailResponse {

    @SerializedName("tournamentId")
    private String tournamentId;

    @SerializedName("tournamentName")
    private String tournamentName;

    @SerializedName("tournamentImg")
    private List<ImageList> tournamentImg;

    @SerializedName("tournamentDescription")
    private String tournamentDescription;

    @SerializedName("tournamentSponsors")
    private List<TournamentSponsor> tournamentSponsors;

    @SerializedName("organizerName")
    private String organizerName;

    @SerializedName("organizerLogo")
    private String organizerLogo;

    @SerializedName("tournamentStartDate")
    private String tournamentStartDate;

    @SerializedName("tournamentEndDate")
    private String tournamentEndDate;

    @SerializedName("regStartDate")
    private String regStartDate;

    @SerializedName("regEndDate")
    private String regEndDate;

    @SerializedName("tournamentLocation")
    private String tournamentLocation;

    @SerializedName("currentNumberParticipants")
    private String currentNumberParticipants;

    @SerializedName("maxParticipants")
    private String maxParticipants;

    @SerializedName("matchTypes")
    private List<MatchType> matchTypes;

    public String getParticipationConditions() {
        return participationConditions;
    }

    public void setParticipationConditions(String participationConditions) {
        this.participationConditions = participationConditions;
    }

    @SerializedName("participationConditions")
    private String participationConditions;

    public String getParticipationRules() {
        return participationRules;
    }

    public void setParticipationRules(String participationRules) {
        this.participationRules = participationRules;
    }

    @SerializedName("participationRules")
    private String participationRules;

    // Inner Classes
    public static class ImageList {
        @SerializedName("imageUrl")
        private String imageUrl;

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }

    public static class TournamentSponsor {
        @SerializedName("sponsorId")
        private String sponsorId;

        @SerializedName("sponsorName")
        private String sponsorName;

        @SerializedName("sponsorLogo")
        private String sponsorLogo;

        @SerializedName("websiteUrl")
        private String websiteUrl;

        @SerializedName("displayOrder")
        private Integer displayOrder;

        // Getters and Setters
        public String getSponsorId() {
            return sponsorId;
        }

        public void setSponsorId(String sponsorId) {
            this.sponsorId = sponsorId;
        }

        public String getSponsorName() {
            return sponsorName;
        }

        public void setSponsorName(String sponsorName) {
            this.sponsorName = sponsorName;
        }

        public String getSponsorLogo() {
            return sponsorLogo;
        }

        public void setSponsorLogo(String sponsorLogo) {
            this.sponsorLogo = sponsorLogo;
        }

        public String getWebsiteUrl() {
            return websiteUrl;
        }

        public void setWebsiteUrl(String websiteUrl) {
            this.websiteUrl = websiteUrl;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }
    }

    public static class MatchType {
        @SerializedName("matchTypeCode")
        private String matchTypeCode; // SINGLE_MALE, SINGLE_FEMALE, DOUBLE_MALE, DOUBLE_FEMALE, DOUBLE_MIXED

        @SerializedName("matchTypeName")
        private String matchTypeName;

        @SerializedName("numberOfParticipant")
        private String numberOfParticipant;

        @SerializedName("maxParticipants")
        private String maxParticipants;

        @SerializedName("startDate")
        private String startDate;

        @SerializedName("endDate")
        private String endDate;

        // Getters and Setters
        public String getMatchTypeCode() {
            return matchTypeCode;
        }

        public void setMatchTypeCode(String matchTypeCode) {
            this.matchTypeCode = matchTypeCode;
        }

        public String getMatchTypeName() {
            return matchTypeName;
        }

        public void setMatchTypeName(String matchTypeName) {
            this.matchTypeName = matchTypeName;
        }

        public String getNumberOfParticipant() {
            return numberOfParticipant;
        }

        public void setNumberOfParticipant(String numberOfParticipant) {
            this.numberOfParticipant = numberOfParticipant;
        }

        public String getMaxParticipants() {
            return maxParticipants;
        }

        public void setMaxParticipants(String maxParticipants) {
            this.maxParticipants = maxParticipants;
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

        // Helper method
        public boolean isFull() {
            try {
                int current = Integer.parseInt(numberOfParticipant);
                int max = Integer.parseInt(maxParticipants);
                return current >= max;
            } catch (NumberFormatException e) {
                return false;
            }
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

    public List<ImageList> getTournamentImg() {
        return tournamentImg;
    }

    public void setTournamentImg(List<ImageList> tournamentImg) {
        this.tournamentImg = tournamentImg;
    }

    public String getTournamentDescription() {
        return tournamentDescription;
    }

    public void setTournamentDescription(String tournamentDescription) {
        this.tournamentDescription = tournamentDescription;
    }

    public List<TournamentSponsor> getTournamentSponsors() {
        return tournamentSponsors;
    }

    public void setTournamentSponsors(List<TournamentSponsor> tournamentSponsors) {
        this.tournamentSponsors = tournamentSponsors;
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

    public String getTournamentStartDate() {
        return tournamentStartDate;
    }

    public void setTournamentStartDate(String tournamentStartDate) {
        this.tournamentStartDate = tournamentStartDate;
    }

    public String getTournamentEndDate() {
        return tournamentEndDate;
    }

    public void setTournamentEndDate(String tournamentEndDate) {
        this.tournamentEndDate = tournamentEndDate;
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

    public String getTournamentLocation() {
        return tournamentLocation;
    }

    public void setTournamentLocation(String tournamentLocation) {
        this.tournamentLocation = tournamentLocation;
    }

    public String getCurrentNumberParticipants() {
        return currentNumberParticipants;
    }

    public void setCurrentNumberParticipants(String currentNumberParticipants) {
        this.currentNumberParticipants = currentNumberParticipants;
    }

    public String getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(String maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public List<MatchType> getMatchTypes() {
        return matchTypes;
    }

    public void setMatchTypes(List<MatchType> matchTypes) {
        this.matchTypes = matchTypes;
    }

    // Helper methods
    public boolean isRegistrationOpen() {
        // You can implement date comparison logic here
        return true; // Placeholder
    }



    public int getParticipationPercentage() {
        try {
            int current = Integer.parseInt(currentNumberParticipants);
            int max = Integer.parseInt(maxParticipants);
            if (max == 0) return 0;
            return (current * 100) / max;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}