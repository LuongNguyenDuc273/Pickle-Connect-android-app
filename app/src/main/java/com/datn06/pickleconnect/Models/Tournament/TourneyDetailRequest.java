package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

/**
 * Request for getting tournament detail
 * Endpoint: POST /tourney-detail
 */
public class TourneyDetailRequest extends BaseTournamentRequest {

    @SerializedName("tournamentId")
    private String tournamentId;

    @SerializedName("type")
    private String type; // "1"=full details with sponsors, "2"=basic with match types

    public TourneyDetailRequest() {
        super();
    }

    public TourneyDetailRequest(String userId, String tournamentId, String type) {
        super(userId);
        this.tournamentId = tournamentId;
        this.type = type;
    }

    // Getters and Setters
    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}