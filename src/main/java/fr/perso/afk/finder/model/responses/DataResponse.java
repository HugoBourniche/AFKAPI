package fr.perso.afk.finder.model.responses;

import fr.perso.afk.finder.model.request.FiltersRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hugo Bourniche
 * 30/11/2021
 */
@Data
public class DataResponse {

    private List<CharacterResponse> characters;
    private FiltersRequest activeFilter;
    private List<String> factions;
    private List<String> types;
    private List<String> classes;
    private List<String> roles;
    private List<Integer> ranks;

    public DataResponse(FiltersRequest activeFilter) {
        this.characters = new ArrayList<>();
        this.factions = new ArrayList<>();
        this.types = new ArrayList<>();
        this.classes = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.ranks = new ArrayList<>();
        this.activeFilter = activeFilter;
    }

    public void populateCharactersAndCaracteristics(CharacterResponse characterResponse) {
        if (!factions.contains(characterResponse.getFaction())) this.factions.add(characterResponse.getFaction());
        if (!types.contains(characterResponse.getType()))                 this.types.add(characterResponse.getType());
        if (!classes.contains(characterResponse.getClasse()))             this.classes.add(characterResponse.getClasse());
        if (!roles.contains(characterResponse.getRole()))                 this.roles.add(characterResponse.getRole());
        if (!ranks.contains(characterResponse.getRank()))                 this.ranks.add(characterResponse.getRank());
        if (this.activeFilter.isValidCharacter(characterResponse))        this.characters.add(characterResponse);
    }
}
