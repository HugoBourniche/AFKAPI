package fr.perso.afk.finder.controller;

import fr.perso.afk.finder.data.SharedData;
import fr.perso.afk.finder.model.FightEntity;
import fr.perso.afk.finder.model.responses.FightsResponse;
import fr.perso.afk.finder.services.DBService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Hugo Bourniche
 * 13/01/2022
 */
@RestController
@CrossOrigin(origins = "*")
public class FightController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FightController.class);

    @Autowired
    DBService dbService;

    @Autowired
    SharedData sharedData;

    @GetMapping(value="/winning-fights")
    public ResponseEntity <FightsResponse> addCharacter() {
        LOGGER.info("Get Winning Fights");
        List<FightEntity> fights = dbService.findFightByWinnerTeam(sharedData.getSelectedCharactersName());
        return new ResponseEntity<>(new FightsResponse(fights), HttpStatus.OK);
    }

}
