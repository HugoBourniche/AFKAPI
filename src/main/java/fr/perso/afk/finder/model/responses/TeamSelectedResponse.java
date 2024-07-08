package fr.perso.afk.finder.model.responses;

import java.util.ArrayList;
import java.util.List;

import fr.perso.afk.finder.model.FightEntity;
import fr.perso.afk.finder.model.TeamCharacterEntity;
import fr.perso.afk.finder.model.TeamEntity;
import lombok.Data;

/**
 * @author: Hugo Bourniche
 * 04/02/2022
 */
@Data
public class TeamSelectedResponse {
	
	List<CharacterResponse> characters = new ArrayList<>();
	List<TeamResponse> teams = new ArrayList<>();
	List<FightResponse> winning = new ArrayList<>();
	List<FightResponse> lost = new ArrayList<>();

	public TeamSelectedResponse(TeamEntity team) {
		for(TeamCharacterEntity teamCharacter : team.getTeamCharacters()) {
			this.characters.add(new CharacterResponse(teamCharacter.getCharacter(), teamCharacter.getPosition()));
		}
	}

	public void addTeams(List<TeamEntity> teams) {
		for(TeamEntity team: teams) {
			this.addTeam(team);
		}
	}

	public void addWins(List<FightEntity> fights) {
		for(FightEntity fight : fights) {
			this.addWinningFight(fight);
		}
	}

	public void addLosts(List<FightEntity> fights) {
		for(FightEntity fight : fights) {
			this.addLostFight(fight);
		}
	}

	public void addTeam(TeamEntity team) {
		this.teams.add(new TeamResponse(team));
	}

	public void addWinningFight(FightEntity fight) {
		this.winning.add(new FightResponse(fight));
	}

	public void addLostFight(FightEntity fight) {
		this.lost.add(new FightResponse(fight));
	}
}
