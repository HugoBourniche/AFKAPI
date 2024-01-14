package fr.perso.afk.finder.controller;

import fr.perso.afk.finder.data.SharedData;
import fr.perso.afk.finder.data.TeamSolver;
import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.TeamEntity;
import fr.perso.afk.finder.model.responses.TeamSelectedResponse;
import fr.perso.afk.finder.services.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author: Hugo Bourniche
 * 13/01/2022
 */
@RestController
@CrossOrigin(origins = "*")
public class TeamController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamController.class);

    @Autowired
    DBService dbService;

    @Autowired
    SharedData sharedData;

    @Autowired
    TeamSolver teamSolver;

    @GetMapping(value="/add-character")
    public ResponseEntity <Integer> addCharacter(@RequestParam String name) {
        LOGGER.info("Add " + name + " to team");
        Optional<CharacterEntity> character = dbService.findCharacter(name);
        if (character.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Integer response = sharedData.addCharacter(character.get());
        LOGGER.info("Added to the position: " + response);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value="/remove-character")
    public ResponseEntity <?> removeCharacter(@RequestBody String name) {
        LOGGER.info("Remove " + name + " to team");
        sharedData.removeCharacter(name);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value="/current-team")
    public ResponseEntity<TeamSelectedResponse> fetchCurrentTeam() {
    	LOGGER.info("Fetch current Team");
        TeamSelectedResponse selectedTeam = new TeamSelectedResponse(this.sharedData.getMyTeam());
        List<TeamEntity> associatedTeam = dbService.findTeamContainingCharacter(this.sharedData.getSelectedCharactersName());
        selectedTeam.addTeams(associatedTeam);
        selectedTeam.addWins(dbService.findFightByWinnerTeam(this.sharedData.getSelectedCharactersName()));
        selectedTeam.addLosts(dbService.findFightByLosingTeam(this.sharedData.getSelectedCharactersName()));
    	return new ResponseEntity<TeamSelectedResponse>(selectedTeam, HttpStatus.OK);
    }

}
