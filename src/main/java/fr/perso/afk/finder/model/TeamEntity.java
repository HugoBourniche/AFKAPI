package fr.perso.afk.finder.model;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author: Hugo Bourniche
 * 05/12/2021
 */
@Data
@Entity
@Table(name = "teams")
public class TeamEntity {

    //******************************************************************************************************************
    // ATTRIBUTES
    //******************************************************************************************************************

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    protected UUID id;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamCharacterEntity> teamCharacters = new ArrayList<>();

    private Integer use;

    //******************************************************************************************************************
    // CONSTRUCTOR
    //******************************************************************************************************************

    public TeamEntity() {
        this.use = 1;
    }

    //******************************************************************************************************************
    // PUBLIC METHODS
    //******************************************************************************************************************

    public Integer addCharacter(CharacterEntity character) {
        TeamCharacterEntity teamCharacter = new TeamCharacterEntity();
        teamCharacter.setCharacter(character);
        teamCharacter.setTeam(this);
        // Search unused position
        int position = 1;
        for (TeamCharacterEntity teamCharacterEntity : teamCharacters.stream().sorted(Comparator.comparing(TeamCharacterEntity::getPosition)).collect(Collectors.toList())) {
            if (teamCharacterEntity.getPosition() == position) {
                position++;
            } else {
                break;
            }
        }
        teamCharacter.setPosition(position);
        this.teamCharacters.add(teamCharacter);
        return position;
    }

    public void removeCharacter(int position) {
        this.teamCharacters.remove(position);
    }

    //******************************************************************************************************************
    // GETTER METHODS
    //******************************************************************************************************************

    public boolean isValidTeam() {
        return this.teamCharacters.size() == 5;
    }

    public Integer getCharacterPosition(String name) {
        for (TeamCharacterEntity teamCharacter : teamCharacters) {
            if (teamCharacter.getCharacter().getName().equals(name)) {
                return teamCharacter.getPosition();
            }
        }
        return 0;
    }

    public List<CharacterEntity> getAllOtherCharacters(String name) {
        return this.teamCharacters.stream().map(TeamCharacterEntity::getCharacter).filter(character -> !character.getName().equals(name)).collect(Collectors.toList());
    }

    public List<CharacterEntity> getCharacters() {
        return teamCharacters.stream().map(TeamCharacterEntity::getCharacter).collect(Collectors.toList());
    }

    public List<String> getCharactersByName() {
        return teamCharacters.stream().map(teamCharacterEntity -> teamCharacterEntity.getCharacter().getName()).collect(Collectors.toList());
    }

    //******************************************************************************************************************
    // OVERRIDE METHODS
    //******************************************************************************************************************

    public boolean contains(String name) {
        for (TeamCharacterEntity character : this.teamCharacters) {
            if (character.getCharacter().getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TeamEntity team = (TeamEntity) o;
        for (int i = 0; i < this.teamCharacters.size(); i++) {
            if (!teamCharacters.get(i).getCharacter().getName().equals(team.getTeamCharacters().get(i).getCharacter().getName())) {
                return false;
            }
        }
        return true;
    }
}
