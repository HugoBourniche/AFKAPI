package fr.perso.afk.finder.data;

import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.FightEntity;
import fr.perso.afk.finder.model.TeamEntity;
import fr.perso.afk.finder.model.VersionEntity;
import fr.perso.afk.finder.model.characteristics.FactionEntity;
import fr.perso.afk.finder.services.DBService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.perso.afk.finder.utils.Utils.convertRanksToInt;

/**
 * @author: Hugo Bourniche
 * 30/11/2021
 */
@Service
public class LoadData {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadData.class);
    private static final Boolean FORCE_UPDATE = false;

    @Autowired private DBService dbService;

    private Workbook excelWB;

    private int charsCount = 0;
    private int teamsCount = 0;
    private int fightCount = 0;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() throws Exception {
        LOGGER.info("Loading Data");
        this.loadMultipartFile();
        if (isNewVersion()) {
            LOGGER.info("is new version");
            this.emptyData();
            this.loadFactions();
            this.loadCharacters();
            this.loadTeamsAndFights();

            LOGGER.info(this.charsCount + " characters added");
            LOGGER.info(this.teamsCount + " teams added");
            LOGGER.info(this.fightCount + " fights added");
        }
        LOGGER.info("All the data is loaded");
    }

    private void loadMultipartFile() throws IOException {
        Path path = Paths.get("src/main/resources/excelFile/AFKArena.xlsx");
        String name = "data.xlsx";
        String originalFileName = "data.xlsx";
        String contentType = "text/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        MultipartFile file = new MockMultipartFile(name, originalFileName, null, content);
        this.excelWB = WorkbookFactory.create(file.getInputStream());
    }

    /**
     * Check if it is an old version, and update the version if yes
     */
    private boolean isNewVersion() {
        Sheet s = this.excelWB.getSheet("Personnages");
        double version = s.getRow(0).getCell(0).getNumericCellValue();
        VersionEntity currentVersion = this.dbService.getVersion();
        if (currentVersion == null || currentVersion.isUpdated(version)) {
            this.dbService.replaceVersion(currentVersion, new VersionEntity(version));
            return true;
        }
        return FORCE_UPDATE;
    }

    private void emptyData() {
        LOGGER.info("Empty data");
        this.dbService.emptyData();
    }

    private void loadFactions() {
        LOGGER.info("Load Factions");
        for (FactionEntity faction : FactionEntity.FACTIONS) {
            if (!dbService.factionExist(faction.getName())) {
                dbService.saveFaction(faction);
            }
        }
    }

    private void loadCharacters() {
        LOGGER.info("Load Characters");
        Map<String, Integer> rankMap = mapRanks();
        Sheet sheet = this.excelWB.getSheet("Personnages");
        for (int r = 2; r < sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row.getCell(2).getStringCellValue().equals("")) {
                continue;
            }
            String characterName        = row.getCell(2).getStringCellValue();
            String characterFullName    = row.getCell(3).getStringCellValue().split(" - ")[1];
            FactionEntity characterFaction    = FactionEntity.getFactionByName(row.getCell(4).getStringCellValue());
            String characterType        = row.getCell(5).getStringCellValue();
            String characterClass       = row.getCell(6).getStringCellValue();
            String characterRole        = row.getCell(7).getStringCellValue();
            String characterSummary     = row.getCell(8).getStringCellValue();
            Integer characterRank       = -1;
            if (rankMap.containsKey(characterName)) { characterRank = rankMap.get(characterName); }
            if (characterRole.equals("Tank")) {
                characterRole = "Tank.";
            }
            CharacterEntity newCharacter = new CharacterEntity(
                    characterName,
                    characterFullName,
                    characterType,
                    characterClass,
                    characterRole,
                    characterSummary,
                    characterRank,
                    characterFaction);
            if (!this.dbService.charactersExist(characterName)) {
                this.charsCount++;
            }
            this.dbService.saveCharacter(newCharacter);
        }
    }

    private Map<String, Integer> mapRanks() {
        Map<String, Integer> ranks = new HashMap<>();
        Sheet sheet = this.excelWB.getSheet("Rangs-202305");
        for (int r = 1; r < sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            String characterName = row.getCell(0).getStringCellValue();
            String pveRank = row.getCell(1).getStringCellValue();
            String pvpRank = row.getCell(2).getStringCellValue();
            String bossRank = row.getCell(3).getStringCellValue();
            Integer rank = convertRanksToInt(pveRank, pvpRank, bossRank);
            ranks.put(characterName, rank);
        }
        return ranks;
    }

    private void loadTeamsAndFights() {
//        dbService.emptyFights();
//        dbService.emptyTeams();
        List<TeamEntity> teams = new ArrayList<>();
        Map<String, CharacterEntity> charactersMap = this.dbService.mapAllCharacters();

        this.loadTeamsAndFightBySheet(teams, charactersMap, "Combats", 1, 2);
        this.loadTeamsAndFightBySheet(teams, charactersMap, "Épreuves", 4, 3);
        for (int sheetIndex = 0; sheetIndex < this.excelWB.getNumberOfSheets(); sheetIndex++) {
            Sheet currentSheet = this.excelWB.getSheetAt(sheetIndex);
            String sheetName = currentSheet.getSheetName();
            if (sheetName.contains("SAISON") || sheetName.contains("TRESOR")) {
                this.loadTeamsAndFightBySheet(teams, charactersMap, sheetName, 1, 2);
            }
        }
    }

    private void loadTeamsAndFightBySheet(List<TeamEntity> teams, Map<String, CharacterEntity> characters, String sheetName, int ally, int enemy) {
        LOGGER.info("Load " + sheetName + " teams");
        Sheet sheet = this.excelWB.getSheet(sheetName);
        for (int r = 1; r < sheet.getLastRowNum(); r+=5) {
            TeamEntity t1 = new TeamEntity();
            TeamEntity t2 = new TeamEntity();
            if (r+5 > sheet.getLastRowNum()) {
                continue;
            }
            for (int countRow = r; countRow < r+5; countRow++) {
                Row row = sheet.getRow(countRow);
                String character1Name = row.getCell(ally).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(characters, t1, character1Name);
                String character2Name = row.getCell(enemy).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(characters, t2, character2Name);
                if ("".equals(character1Name) && "".equals(character2Name)) break;
            }

            t1 = this.saveTeam(t1, teams);
            t2 = this.saveTeam(t2, teams);
            if (t1 != null && t2 != null) {
                dbService.saveFight(new FightEntity(t1, t2, sheetName));
                this.fightCount++;
            }
        }
    }

    private TeamEntity saveTeam(TeamEntity t, List<TeamEntity> teams) {
        if (t.isValidTeam()) {
            if (teams.contains(t)) {
                int index = teams.indexOf(t);
                t = teams.get(index);
                t.setUse(t.getUse()+1);
            } else {
                this.teamsCount++;
            }
            dbService.saveTeam(t);
            teams.add(t);
            return t;
        }
        return null;
    }

    private void addCharacterToTeam(Map<String, CharacterEntity> charactersMap, TeamEntity teamEntity, String characterName) {
        CharacterEntity character1 = charactersMap.get(characterName);
        if (character1 != null) {
            teamEntity.addCharacter(character1);
        } else {
            if (!"".equals(characterName)) {
                LOGGER.info(characterName + " n'existe pas");
            }
        }
    }
}
