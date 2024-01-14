package fr.perso.afk.finder.model.responses;

import fr.perso.afk.finder.model.CharacterEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: Hugo Bourniche
 * 12/12/2021
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterSelectedResponse extends CharacterResponse {

    CharacterStatsResponse characterStats;
    List<List<String>> relationship = List.of(
            List.of("friends", "Friends"),
            List.of("bullies", "Bullies"),
            List.of("enemies", "Enemies")
    );
    List<CharacterResponse> friends;
    List<CharacterResponse> bullies;
    List<CharacterResponse> enemies;

    public CharacterSelectedResponse(CharacterEntity character, Integer position, CharacterStatsResponse characterStats, List<CharacterResponse> bestFriends, List<CharacterResponse> bullies, List<CharacterResponse> enemies) {
        super(character, position);
        this.characterStats = characterStats;
        this.friends = bestFriends;
        this.bullies = bullies;
        this.enemies = enemies;
    }
}
