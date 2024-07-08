package fr.perso.afk.finder.services;

import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.FightEntity;
import fr.perso.afk.finder.model.FightId;
import fr.perso.afk.finder.model.TeamEntity;
import fr.perso.afk.finder.model.VersionEntity;
import fr.perso.afk.finder.model.characteristics.FactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;

@Repository interface VersionRepository     extends CrudRepository<VersionEntity, String> {}
@Repository interface FactionRepository     extends CrudRepository<FactionEntity, String> {}
@Repository interface CharacterRepository   extends CrudRepository<CharacterEntity, String> {}
@Repository interface TeamRepository        extends CrudRepository<TeamEntity, UUID> {}
@Repository interface FightRepository       extends CrudRepository<FightEntity, FightId> {
    List<FightEntity> findByIdWinnerIn(List<TeamEntity> teams);
    List<FightEntity> findByIdLoserIn(List<TeamEntity> teams);
    Integer countByIdWinner(TeamEntity team);
    Integer countByIdLoser(TeamEntity team);
}

/**
 * @author: Hugo Bourniche
 * 01/12/2021
 */
@Service
@Transactional
public class DBService {

    //******************************************************************************************************************
    // CONSTANTS
    //******************************************************************************************************************

    private static final Logger LOGGER = LoggerFactory.getLogger(DBService.class);

    //******************************************************************************************************************
    // REPOSITORIES
    //******************************************************************************************************************

    @PersistenceContext private EntityManager manager;
    @Autowired private VersionRepository versionRepository;
    @Autowired private FactionRepository factionRepository;
    @Autowired private CharacterRepository characterRepository;
    @Autowired private TeamRepository teamsRepository;
    @Autowired private FightRepository fightRepository;

    //******************************************************************************************************************
    // EMPTY BASE
    //******************************************************************************************************************

    /**
     * Empty everything except the version
     */
    public void emptyData() {
        long start = System.currentTimeMillis();
        this.fightRepository.deleteAll();
        this.teamsRepository.deleteAll();
        this.characterRepository.deleteAll();
        this.factionRepository.deleteAll();
        long executionTime = System.currentTimeMillis() - start;
        LOGGER.info("EmptyData() has been executed in " + executionTime + "ms");
    }

    //******************************************************************************************************************
    // VERSION
    //******************************************************************************************************************

    public VersionEntity getVersion() {
        Iterator<VersionEntity> version = this.versionRepository.findAll().iterator();
        if (version.hasNext()) {
            return version.next();
        }
        return null;
    }

    public void replaceVersion(VersionEntity oldVersion, VersionEntity newVersion) {
        if (oldVersion != null) {
            this.versionRepository.delete(oldVersion);
        }
        this.versionRepository.save(newVersion);
    }

    //******************************************************************************************************************
    // CHARACTERS
    //******************************************************************************************************************

    public List<CharacterEntity> findAllCharacters() {
        List<CharacterEntity> characters = new ArrayList<>();
        characterRepository.findAll().forEach(characters::add);
        characters.sort(Comparator.comparing(CharacterEntity::getName));
        return characters;
    }

    public Map<String, CharacterEntity> mapAllCharacters() {
        Map<String, CharacterEntity> charactersMap = new HashMap<>();
        List<CharacterEntity> characters = this.findAllCharacters();
        for (CharacterEntity character : characters) {
            charactersMap.put(character.getName().toLowerCase(), character);
        }
        return charactersMap;
    }

    public Optional<CharacterEntity> findCharacter(String name) {
        name = name.replace(" and ", " & ");
        return characterRepository.findById(name);
    }

    public boolean charactersExist(String name) {
        return characterRepository.findById(name).isPresent();
    }

    public void saveCharacter(CharacterEntity characterEntity) {
        characterRepository.save(characterEntity);
    }

    //******************************************************************************************************************
    // FACTIONS
    //******************************************************************************************************************

    public List<FactionEntity> findAllFactions() {
        List<FactionEntity> factions = new ArrayList<>();
        factionRepository.findAll().forEach(factions::add);
        return factions;
    }

    public FactionEntity findFactions(String name) {
        return factionRepository.findById(name).get();
    }

    public boolean factionExist(String name) {
        return factionRepository.findById(name).isPresent();
    }

    public void saveFaction(FactionEntity factionEntity) {
        factionRepository.save(factionEntity);
    }

    //******************************************************************************************************************
    // TEAMS
    //******************************************************************************************************************

    public List<TeamEntity> findAllTeams() {
        List<TeamEntity> teams = new ArrayList<>();
        teamsRepository.findAll().forEach(teams::add);
        return teams;
    }

    public List<TeamEntity> findTeamContainingCharacter(List<String> characterNames) {
        List<TeamEntity> teams = new ArrayList<>();
        if (characterNames.size() == 0) return teams;
        CharacterEntity characterEntity = fetchCharacterWithTheLessTeams(characterNames);
        if (characterEntity == null) return teams;
        if (characterNames.size() == 1) return characterEntity.getTeams();
        teams = characterEntity.getTeamsContaining(characterNames);
        return teams;
    }

    public void saveTeam(TeamEntity teamEntity) {
        teamsRepository.save(teamEntity);
    }

    public void emptyTeams() {
        teamsRepository.deleteAll();
    }

    //******************************************************************************************************************
    // FIGHTS
    //******************************************************************************************************************

    public List<FightEntity> findAllFights() {
        List<FightEntity> fights = new ArrayList<>();
        fightRepository.findAll().forEach(fights::add);
        return fights;
    }

    public void saveFight(FightEntity fight) {
        fightRepository.save(fight);
    }

    public void emptyFights() {
        fightRepository.deleteAll();
    }

    // Winning

    public List<FightEntity> findFightByWinnerTeamMatesName(List<String> characters) {
        List<TeamEntity> teams = this.findTeamContainingCharacter(characters);
        return this.findFightByWinnerTeams(teams);
    }

    public List<FightEntity> findFightByWinnerTeams(List<TeamEntity> teams) {
        return this.fightRepository.findByIdWinnerIn(teams);
    }

    public Integer countWins(TeamEntity team) {
        return this.fightRepository.countByIdWinner(team);
    }

    // Losing

    public List<FightEntity> findFightByLosingTeamMatesName(List<String> characters) {
        List<TeamEntity> teams = this.findTeamContainingCharacter(characters);
        return this.findFightByLosingTeams(teams);
    }

    public List<FightEntity> findFightByLosingTeams(List<TeamEntity> teams) {
        return this.fightRepository.findByIdLoserIn(teams);
    }

    public Integer countLose(TeamEntity team) {
        return this.fightRepository.countByIdLoser(team);
    }

    //******************************************************************************************************************
    // PRIVATE METHODS
    //******************************************************************************************************************

    private CharacterEntity fetchCharacterWithTheLessTeams(List<String> characterNames) {
        CharacterEntity character = null;
        int nbTeams = -1;
        for (String characterName : characterNames) {
            Optional<CharacterEntity> currentCharacterOptional = this.characterRepository.findById(characterName);
            if (currentCharacterOptional.isEmpty()) continue;
            CharacterEntity currentCharacter = currentCharacterOptional.get();
            int currentNbTeams = currentCharacter.getTeamCharacters().size();
            if (nbTeams < 0 || nbTeams > currentNbTeams) {
                character = currentCharacter;
                nbTeams = currentNbTeams;
            }
        }
        return character;
    }
}
