package fr.perso.afk.finder.controller;

import fr.perso.afk.finder.data.SharedData;
import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.request.FiltersRequest;
import fr.perso.afk.finder.model.responses.CharacterSelectedResponse;
import fr.perso.afk.finder.model.responses.CharacterResponse;
import fr.perso.afk.finder.model.responses.CharacterStatsResponse;
import fr.perso.afk.finder.model.responses.DataResponse;
import fr.perso.afk.finder.services.DBService;
import fr.perso.afk.finder.data.CharacterSolver;
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
 * 21/11/2021
 */
@RestController
@CrossOrigin(origins = "*")
public class CharacterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CharacterController.class);

    @Autowired
    DBService dbService;

    @Autowired
    SharedData sharedData;

    @Autowired
    CharacterSolver characterSolver;

    private FiltersRequest filtersRequest = new FiltersRequest();

    @GetMapping(value="/characters")
    public ResponseEntity <DataResponse> getCharacters() {
        LOGGER.info("Get All Characters");
        return new ResponseEntity<>(this.fetchFilteredCharacters(), HttpStatus.OK);
    }

    @PostMapping(value="/filterCharacters")
    public ResponseEntity<DataResponse> filterCharacters(@RequestBody FiltersRequest filtersRequest) {
        LOGGER.info("Filter Characters");
        this.filtersRequest = filtersRequest;
        return new ResponseEntity<>(this.fetchFilteredCharacters(), HttpStatus.OK);
    }

    @GetMapping(value="/character")
    public ResponseEntity <CharacterSelectedResponse> getCharacter(@RequestParam String name) {
        LOGGER.info("Get {} details", name);
        Optional<CharacterEntity> character = dbService.findCharacter(name); // 0s
        if (character.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        name = character.get().getName();
        CharacterStatsResponse characterStatsResponse = this.characterSolver.computeCharacterStats(name); // 42s
        List<CharacterResponse> bestFriends = characterSolver.getFriends(name); // 0s
        List<CharacterResponse> bullies = characterSolver.getBullies(name); // 10s
        List<CharacterResponse> enemies = characterSolver.getEnemies(name); // 9s
        CharacterSelectedResponse response = new CharacterSelectedResponse(character.get(),
                sharedData.getCharacterPosition(name),characterStatsResponse, bestFriends, bullies, enemies);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private DataResponse fetchFilteredCharacters() {
        DataResponse response = new DataResponse(filtersRequest);
        for(CharacterEntity character: dbService.findAllCharacters()) {
            CharacterResponse characterResponse = new CharacterResponse(character, sharedData.getCharacterPosition(character.getName()));
           response.populateCharactersAndCaracteristics(characterResponse);
        }
        return response;
    }
}
