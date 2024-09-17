package fr.perso.afk.finder.data;

import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.TeamEntity;
import lombok.Data;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hugo Bourniche
 * 13/01/2022
 */
@Data
@Component
public class SharedData {

    private TeamEntity myTeam = new TeamEntity();

    public Integer addCharacter(CharacterEntity character) {
        return myTeam.addCharacter(character);
    }

    public void removeCharacter(String name) {
        myTeam.removeCharacter(this.getCharacterPosition(name));
    }

    public Integer getCharacterPosition(String name) {
        return myTeam.getCharacterPosition(name);
    }

    public List<String> getSelectedCharactersName() {
        return new ArrayList<>(this.myTeam.getCharactersByName());
    }
}
