package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;

public class TourneyRegTypeRequest extends BaseTournamentRequest {

    @SerializedName("tournamentId")
    private String tournamentId;

    /**
     * Default constructor
     */
    public TourneyRegTypeRequest() {
        super();
    }

    /**
     * Constructor with parameters
     * @param userId User ID (passed to BaseTournamentRequest)
     * @param tournamentId Tournament ID
     */
    public TourneyRegTypeRequest(String userId, String tournamentId) {
        super(userId); // Gọi BaseTournamentRequest(userId)
        this.tournamentId = tournamentId;
    }

    // Getter and Setter for tournamentId only
    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }
}

