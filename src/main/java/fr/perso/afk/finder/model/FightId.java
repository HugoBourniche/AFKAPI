package fr.perso.afk.finder.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class FightId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "winner_id")
    TeamEntity winner;
    @ManyToOne
    @JoinColumn(name = "loser_id")
    TeamEntity loser;

    public TeamEntity getLoser() {
        return loser;
    }

    public TeamEntity getWinner() {
        return winner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FightId fightId = (FightId) o;
        return Objects.equals(winner, fightId.winner) && Objects.equals(loser, fightId.loser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(winner, loser);
    }
}
