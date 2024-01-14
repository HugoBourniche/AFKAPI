package fr.perso.afk.finder.model.responses;

import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.TeamEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hugo Bourniche
 * 04/02/2022
 */
@Data
public class TeamResponse {

    List<CharacterResponse> characterResponses = new ArrayList<>();

    public TeamResponse(TeamEntity teamEntity) {
        for (CharacterEntity character : teamEntity.getCharacters()) {
            this.characterResponses.add(new CharacterResponse(character, 0)); // TODO Set the real position
        }
    }
}
