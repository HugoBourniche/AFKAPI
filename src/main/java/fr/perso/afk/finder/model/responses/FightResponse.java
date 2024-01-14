package fr.perso.afk.finder.model.responses;

import fr.perso.afk.finder.model.FightEntity;
import lombok.Data;

/**
 * @author: Hugo Bourniche
 * 04/02/2022
 */
@Data
public class FightResponse {

    public TeamResponse winner;
    public TeamResponse loser;

    public FightResponse(FightEntity fight) {
        this.winner = new TeamResponse(fight.getWinner());
        this.loser = new TeamResponse(fight.getLoser());
    }
}
