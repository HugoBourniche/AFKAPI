package fr.perso.afk.finder.data;

import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.FightEntity;
import fr.perso.afk.finder.model.TeamEntity;
import fr.perso.afk.finder.model.responses.CharacterResponse;
import fr.perso.afk.finder.model.responses.CharacterStatsResponse;
import fr.perso.afk.finder.services.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author: Hugo Bourniche
 * 13/12/2021
 */
@Component
public class CharacterSolver {

    @Autowired
    private DBService dbService;

    @Autowired
    private SharedData sharedData;

    //******************************************************************************************************************
    // COMPUTE STATS
    //******************************************************************************************************************

    public CharacterStatsResponse computeCharacterStats(String name) {
        float teams = 0;
        Map<Integer, Integer> positions = new HashMap<>(Map.of(
                1, 0,
                2, 0,
                3, 0,
                4, 0,
                5, 0
        ));
        int wins = 0;
        int loses = 0;
        for (TeamEntity team : this.dbService.findTeamContainingCharacter(List.of(name))) {
            teams+=team.getUse();
            Integer position = team.getCharacterPosition(name);
            positions.put(position, positions.get(position) + 1);
            wins = wins + this.dbService.countWins(team);
            loses = loses + this.dbService.countLose(team);
        }
        return new CharacterStatsResponse(Map.of(
                1, positions.get(1) / teams,
                2, positions.get(2) / teams,
                3, positions.get(3) / teams,
                4, positions.get(4) / teams,
                5, positions.get(5) / teams), (int) teams, wins+loses, (wins / teams));
    }

    //******************************************************************************************************************
    // FRIENDS
    //******************************************************************************************************************

    public List<CharacterResponse> getFriends(String name) {
        Map<String, Integer> friendshipness = this.computeFriendship(name);
        List<String> bestFriends = this.associateCharacters(friendshipness);
        return this.finalizeResponse(bestFriends);
    }

    private Map<String, Integer> computeFriendship(String name) {
        Map<String, Integer> friendship = new HashMap<>();
        for (TeamEntity team : dbService.findTeamContainingCharacter(List.of(name))) {
            for (CharacterEntity friend : team.getAllOtherCharacters(name)) {
                Integer count = (friendship.containsKey(friend.getName()) ? friendship.get(friend.getName()) + team.getUse() : 1);
                friendship.put(friend.getName(), count);
            }
        }
        return friendship;
    }

    //******************************************************************************************************************
    // BULLIES
    //******************************************************************************************************************

    public List<CharacterResponse> getBullies(String name) {
        Map<String, Integer> bullies = this.computeBullies(name);
        List<String> bestBullies = this.associateCharacters(bullies);
        return this.finalizeResponse(bestBullies);
    }

    public Map<String, Integer> computeBullies(String name) {
        Map<String, Integer> bullies = new HashMap<>();
        List<TeamEntity> teams = dbService.findTeamContainingCharacter(List.of(name));
        for (FightEntity fight: dbService.findFightByWinnerTeams(teams)) {
            for (CharacterEntity bully : fight.getLoser().getCharacters()) {
                Integer count = (bullies.containsKey(bully.getName()) ? bullies.get(bully.getName()) + fight.getWinner().getUse() : 1);
                bullies.put(bully.getName(), count);
            }
        }
        return bullies;
    }

    //******************************************************************************************************************
    // ENEMY
    //******************************************************************************************************************

    public List<CharacterResponse> getEnemies(String name) {
        Map<String, Integer> enemies = this.computeEnemies(name);
        List<String> bestEnemies = this.associateCharacters(enemies);
        return this.finalizeResponse(bestEnemies);
    }

    public Map<String, Integer> computeEnemies(String name) {
        Map<String, Integer> enemies = new HashMap<>();
        List<TeamEntity> teams = dbService.findTeamContainingCharacter(List.of(name));
        for (FightEntity fight: dbService.findFightByLosingTeams(teams)) {
            for (CharacterEntity enemy : fight.getWinner().getCharacters()) {
                Integer count = (enemies.containsKey(enemy.getName()) ? enemies.get(enemy.getName()) + fight.getLoser().getUse() : 1);
                enemies.put(enemy.getName(), count);
            }
        }
        return enemies;
    }

    //******************************************************************************************************************
    // UTILS
    //******************************************************************************************************************

    private List<String> associateCharacters(Map<String, Integer> characterCountMap) {
        List<String> characters = new ArrayList<>();
        Integer maxCount = 0;
        for (Integer value : characterCountMap.values()) {
            if (value > maxCount) maxCount = value;
        }
        do {
            for (String character : characterCountMap.keySet()) {
                if (Objects.equals(characterCountMap.get(character), maxCount)) characters.add(character);
            }
            maxCount--;
        } while (characters.size() == 1 && maxCount > 0);
        return characters;
    }

    private List<CharacterResponse> finalizeResponse(List<String> characters) {
        List<CharacterResponse> list = new ArrayList<>();
        for (String characterName : characters) {
            Optional<CharacterEntity> character = dbService.findCharacter(characterName);
            character.ifPresent(characterEntity -> list.add(new CharacterResponse(characterEntity, sharedData.getCharacterPosition(characterName))));
        }
        return list;
    }
}
