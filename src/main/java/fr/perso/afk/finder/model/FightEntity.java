package fr.perso.afk.finder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author: Hugo Bourniche
 * 14/12/2021
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "fights")
public class FightEntity {

    @EmbeddedId FightId id;

    private String type;

    public FightEntity(TeamEntity winner, TeamEntity loser, String type) {
        this.id = new FightId(winner, loser);
        this.type = type;
    }

    public boolean isWinner(TeamEntity team) {
        return id.getWinner().equals(team);
    }

    public boolean isLoser(TeamEntity team) {
        return id.getLoser().equals(team);
    }

    public TeamEntity getWinner() {
        return this.id.getWinner();
    }

    public TeamEntity getLoser() {
        return this.id.getLoser();
    }

    public String getType() { return this.type; }
}

