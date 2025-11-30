package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

/**
 * Request for getting tournament registration form configuration
 * Endpoint: POST /tourney-reg-config
 */
public class TourneyRegConfigRequest extends BaseTournamentRequest {

    @SerializedName("tournamentId")
    private String tournamentId;

    public TourneyRegConfigRequest() {
        super();
    }

    public TourneyRegConfigRequest(String userId, String tournamentId) {
        super(userId);
        this.tournamentId = tournamentId;
    }

    // Getters and Setters
    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }
}