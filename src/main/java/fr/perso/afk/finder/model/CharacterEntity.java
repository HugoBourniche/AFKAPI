package fr.perso.afk.finder.model;

import fr.perso.afk.finder.model.characteristics.FactionEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: Hugo Bourniche
 * 21/11/2021
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "characters")
public class CharacterEntity {

    //******************************************************************************************************************
    // ATTRIBUTES
    //******************************************************************************************************************

    @Id
    private String name;
    private String fullName;
    private String type;
    private String classe;
    private String role;
    private String summary;

    @ManyToOne(fetch= FetchType.EAGER)
    private FactionEntity faction;

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamCharacterEntity> teamCharacters = new ArrayList<>();

    @OneToMany(mappedBy = "character", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RankEntity> ranks = new ArrayList<>();

    //******************************************************************************************************************
    // CONSTRUCTOR
    //******************************************************************************************************************

    public CharacterEntity(String name, String fullName, String type, String classe, String role, String summary, FactionEntity faction) {
        this.name = name;
        this.fullName = fullName;
        this.type = type;
        this.classe = classe;
        this.role = role;
        this.summary = summary;
        this.faction = faction;
    }

    //******************************************************************************************************************
    // PUBLIC METHODS
    //******************************************************************************************************************

    public List<TeamEntity> getTeams() {
        return this.getTeamCharacters().stream().map(TeamCharacterEntity::getTeam).collect(Collectors.toList());
    }

    public List<TeamEntity> getTeamsContaining(List<String> characterNames) {
        List<TeamEntity> teams = new ArrayList<>();
        for (TeamEntity team : getTeams()) {
            boolean isValidTeam = true;
            for(String characterName : characterNames) {
                if (!team.contains(characterName)) {
                    isValidTeam = false;
                    break;
                }
            }
            if (isValidTeam) teams.add(team);
        }
        return teams;
    }

    public Integer getRank() {
        if (ranks.isEmpty()) {
            return -1;
        }
        RankEntity lastestRank = null;
        LocalDate date = null;
        for (RankEntity rank : ranks) {
            if (lastestRank == null || rank.getDate().isAfter(date)) {
                lastestRank = rank;
                date = rank.getDate();
            }
        }
        return lastestRank.getValue();
    }
}
