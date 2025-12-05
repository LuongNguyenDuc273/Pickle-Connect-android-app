package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

public class RegType {
    @SerializedName("tournamentDetailId")
    private String tournamentDetailId;

    @SerializedName("code")
    private String code;

    @SerializedName("name")
    private String name;

    @SerializedName("numberOfParticipants")
    private String numberOfParticipants;

    @SerializedName("maxParticipants")
    private String maxParticipants;

    // Getters and Setters
    public String getTournamentDetailId() { return tournamentDetailId; }
    public void setTournamentDetailId(String tournamentDetailId) {
        this.tournamentDetailId = tournamentDetailId;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNumberOfParticipants() { return numberOfParticipants; }
    public void setNumberOfParticipants(String numberOfParticipants) {
        this.numberOfParticipants = numberOfParticipants;
    }

    public String getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(String maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    // Helper methods
    public boolean isFull() {
        try {
            int current = Integer.parseInt(numberOfParticipants != null ? numberOfParticipants : "0");
            int max = Integer.parseInt(maxParticipants != null ? maxParticipants : "0");
            return current >= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public String getAvailabilityText() {
        String current = numberOfParticipants != null ? numberOfParticipants : "0";
        String max = maxParticipants != null ? maxParticipants : "0";
        return current + "/" + max;
    }
}
