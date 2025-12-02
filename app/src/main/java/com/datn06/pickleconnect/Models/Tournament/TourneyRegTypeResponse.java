package com.datn06.pickleconnect.Models.Tournament;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TourneyRegTypeResponse extends BaseTournamentRequest{
    @SerializedName("tournamentRegTypes")
    private List<RegType> tournamentRegTypes;

    public List<RegType> getTournamentRegTypes() { return tournamentRegTypes; }
    public void setTournamentRegTypes(List<RegType> tournamentRegTypes) {
        this.tournamentRegTypes = tournamentRegTypes;
    }
}
