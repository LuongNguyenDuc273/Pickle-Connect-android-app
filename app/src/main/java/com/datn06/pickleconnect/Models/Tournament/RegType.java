package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

public class RegType {
    @SerializedName("tournamentDetailId")
    private String tournamentDetailId;

    @SerializedName("matchTypeCode")
    private String matchTypeCode;

    @SerializedName("matchTypeName")
    private String matchTypeName;

    @SerializedName("currentRegistrations")
    private int currentRegistrations;

    @SerializedName("maxParticipants")
    private int maxParticipants;

    @SerializedName("entryFee")
    private String entryFee;

    // Getters and Setters
    public String getTournamentDetailId() { return tournamentDetailId; }
    public void setTournamentDetailId(String tournamentDetailId) {
        this.tournamentDetailId = tournamentDetailId;
    }

    public String getMatchTypeCode() { return matchTypeCode; }
    public void setMatchTypeCode(String matchTypeCode) { this.matchTypeCode = matchTypeCode; }

    public String getMatchTypeName() { return matchTypeName; }
    public void setMatchTypeName(String matchTypeName) { this.matchTypeName = matchTypeName; }

    public int getCurrentRegistrations() { return currentRegistrations; }
    public void setCurrentRegistrations(int currentRegistrations) {
        this.currentRegistrations = currentRegistrations;
    }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getEntryFee() { return entryFee; }
    public void setEntryFee(String entryFee) { this.entryFee = entryFee; }

    // Helper method
    public boolean isFull() {
        return currentRegistrations >= maxParticipants;
    }

    public String getAvailabilityText() {
        return currentRegistrations + "/" + maxParticipants;
    }
}
