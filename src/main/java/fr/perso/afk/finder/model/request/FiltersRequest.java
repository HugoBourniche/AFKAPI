package fr.perso.afk.finder.model.request;

import fr.perso.afk.finder.model.CharacterEntity;

import fr.perso.afk.finder.model.responses.CharacterResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hugo Bourniche
 * 30/11/2021
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FiltersRequest {

    private List<String> factions   = new ArrayList<>();
    private List<String> types      = new ArrayList<>();
    private List<String> classes    = new ArrayList<>();
    private List<String> roles      = new ArrayList<>();
    private List<Integer> ranks      = new ArrayList<>();

    public boolean isValidCharacter(CharacterResponse character) {
        return (factions.contains(character.getFaction())
            &&  types.contains(character.getType())
            &&  classes.contains(character.getClasse())
            && roles.contains(character.getRole())
            && ranks.contains(character.getRank()))
            || (factions.isEmpty()
                || types.isEmpty()
                || classes.isEmpty()
                || roles.isEmpty()
                || ranks.isEmpty()
            );
    }
}
