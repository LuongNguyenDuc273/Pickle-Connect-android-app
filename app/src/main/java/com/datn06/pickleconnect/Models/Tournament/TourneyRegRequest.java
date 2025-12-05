package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Request DTO for tournament registration
 * Endpoint: POST /api-andr/tourney-reg
 */
public class TourneyRegRequest extends BaseTournamentRequest {
    
    @SerializedName("tourneyRegUsers")
    private List<TourneyRegUser> tourneyRegUsers;
    
    @SerializedName("tournamentId")
    private String tournamentId;
    
    @SerializedName("tournamentDetailId")
    private String tournamentDetailId;
    
    @SerializedName("matchType")
    private String matchType;
    
    // Constructors
    public TourneyRegRequest() {}
    
    public TourneyRegRequest(String userId, List<TourneyRegUser> tourneyRegUsers, 
                             String tournamentId, String tournamentDetailId, String matchType) {
        super(userId);
        this.tourneyRegUsers = tourneyRegUsers;
        this.tournamentId = tournamentId;
        this.tournamentDetailId = tournamentDetailId;
        this.matchType = matchType;
    }
    
    // Getters and Setters
    public List<TourneyRegUser> getTourneyRegUsers() {
        return tourneyRegUsers;
    }
    
    public void setTourneyRegUsers(List<TourneyRegUser> tourneyRegUsers) {
        this.tourneyRegUsers = tourneyRegUsers;
    }
    
    public String getTournamentId() {
        return tournamentId;
    }
    
    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }
    
    public String getTournamentDetailId() {
        return tournamentDetailId;
    }
    
    public void setTournamentDetailId(String tournamentDetailId) {
        this.tournamentDetailId = tournamentDetailId;
    }
    
    public String getMatchType() {
        return matchType;
    }
    
    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }
}
