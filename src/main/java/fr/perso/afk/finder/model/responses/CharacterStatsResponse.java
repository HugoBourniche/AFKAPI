package fr.perso.afk.finder.model.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterStatsResponse {

    Map<Integer, Float> positionUsage;
    Integer nbTeams;
    Integer nbFights;
    Float winRate;

}
