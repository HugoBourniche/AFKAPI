package fr.perso.afk.finder.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author: Hugo Bourniche
 * 05/12/2021
 */

@Entity
@Getter
@Setter
@Table(name = "teams")
public class TeamEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    protected UUID id;

    @ManyToMany
    private List<CharacterEntity> characters = new ArrayList<>();

    private Integer use;

    public TeamEntity() {
        this.use = 1;
    }

    public Integer addCharacter(CharacterEntity character) {
        this.characters.add(character);
        return this.characters.size();
    }

    public void removeCharacter(int position) {
        this.characters.remove(position);
    }

    public boolean isValidTeam() {
        return this.characters.size() == 5;
    }

    public boolean contains(String name) {
        for (CharacterEntity character : this.characters) {
            if (character.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Integer getCharacterPosition(String name) {
        for (int i = 0; i < characters.size(); i++) {
            if (this.characters.get(i).getName().equals(name)) {
                return i + 1;
            }
        }
        return 0;
    }

    public List<CharacterEntity> getAllOtherCharacters(String name) {
        return this.characters.stream().filter(characterEntity -> !characterEntity.getName().equals(name)).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamEntity team = (TeamEntity) o;
        for (int i = 0; i < this.characters.size(); i++) {
            if (!characters.get(i).getName().equals(team.getCharacters().get(i).getName())) {
                return false;
            }
        }
        return true;
    }

    public List<String> getCharactersByName() {
        List<String> charactersByName = new ArrayList<>();
        for (CharacterEntity character: characters) {
            charactersByName.add(character.getName());
        }
        return charactersByName;
    }
}
