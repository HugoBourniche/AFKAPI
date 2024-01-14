package fr.perso.afk.finder.model.responses;

import fr.perso.afk.finder.model.FightEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hugo Bourniche
 * 11/02/2022
 */
@Data
public class FightsResponse {

    public List<FightResponse> fights = new ArrayList<>();

    public FightsResponse(List<FightEntity> fights) {
        for (FightEntity entity: fights) {
            this.fights.add(new FightResponse(entity));
        }
    }
}
