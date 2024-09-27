package fr.perso.afk.finder.data;

import fr.perso.afk.finder.exceptions.UndefinedCharacterException;
import fr.perso.afk.finder.exceptions.UndefinedRankException;
import fr.perso.afk.finder.model.CharacterEntity;
import fr.perso.afk.finder.model.FightEntity;
import fr.perso.afk.finder.model.RankEntity;
import fr.perso.afk.finder.model.TeamEntity;
import fr.perso.afk.finder.model.VersionEntity;
import fr.perso.afk.finder.model.characteristics.FactionEntity;
import fr.perso.afk.finder.services.DBService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.perso.afk.finder.utils.Utils.convertCharacterName;
import static fr.perso.afk.finder.utils.Utils.convertRankToInt;

/**
 * @author: Hugo Bourniche
 * 30/11/2021
 */
@Service
public class LoadData {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadData.class);
    private static final List<String> FIGHTS_KEY_WORDS = Arrays.asList("CHAMPION", "TRESOR", "Combats", "Campain", "SAISON");
    private static final List<String> RANK_ADVISE_KEY_WORDS = Arrays.asList("Signature", "Furniture", "Engraving", "Artifact");

    @Value("${afk.data.excel.path}")
    private String excelPath;

    @Value("${afk.data.trigger-reload}")
    private Boolean triggerReload;

    @Autowired private DBService dbService;

    private Workbook excelWB;

    private final Map<String, CharacterEntity> characters = new HashMap<>();

    private int charsCount = 0;
    private int teamsCount = 0;
    private int fightCount = 0;

    @EventListener(ApplicationReadyEvent.class)
    public void loadData() throws Exception {
        LOGGER.info("Loading Data");
        this.loadMultipartFile();
        if (triggerReload) {
            LOGGER.info("is new version");
            this.emptyData();
            this.loadFactions();
            this.loadCharacters();
            this.loadRanks();
            this.loadTeamsAndFights();

            LOGGER.info(this.charsCount + " characters added");
            LOGGER.info(this.teamsCount + " teams added");
            LOGGER.info(this.fightCount + " fights added");
            LOGGER.info("All the data is loaded");
        } else {
            LOGGER.info("Loading data ignored");
        }
    }

    private void loadMultipartFile() throws IOException {
        Path path = Paths.get(excelPath);
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
        return false;
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
        Sheet sheet = this.excelWB.getSheet("Personnages");
        for (int r = 2; r < sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row.getCell(2).getStringCellValue().isEmpty()) {
                continue;
            }
            String characterName        = row.getCell(2).getStringCellValue();
            String characterFullName    = row.getCell(3).getStringCellValue().split(" - ")[1];
            FactionEntity characterFaction    = FactionEntity.getFactionByName(row.getCell(4).getStringCellValue());
            String characterType        = row.getCell(5).getStringCellValue();
            String characterClass       = row.getCell(6).getStringCellValue();
            String characterRole        = row.getCell(7).getStringCellValue();
            String characterSummary     = row.getCell(8).getStringCellValue();
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
                    characterFaction);
            if (!this.dbService.charactersExist(characterName)) {
                this.charsCount++;
            }
            this.dbService.saveCharacter(newCharacter);
            this.characters.put(characterName.toLowerCase(), newCharacter);
        }
    }

    private void loadRanks() {
        LOGGER.info("Load Ranks");

        for (int sheetIndex = 0; sheetIndex < this.excelWB.getNumberOfSheets(); sheetIndex++) {
            Sheet currentSheet = this.excelWB.getSheetAt(sheetIndex);
            if (currentSheet.getSheetName().contains("Rangs")) {
                try {
                    loadRankBySheet(currentSheet);
                } catch (UndefinedCharacterException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadRankBySheet(Sheet currentSheet) throws UndefinedCharacterException {
        LOGGER.info("Loading {}", currentSheet.getSheetName());
        // Add 01 day because ranks
        String currentDate = currentSheet.getSheetName().split("-")[1] + "01";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(currentDate, formatter);
        // Prepare titles
        List<String> titles = new ArrayList<>();
        Row row = currentSheet.getRow(0);
        for (int c = 1; c < row.getLastCellNum(); c++) {
            titles.add(row.getCell(c).getStringCellValue());
        }
        // Read rows
        for (int r = 1; r < currentSheet.getLastRowNum(); r++) {
            Row currentRow = currentSheet.getRow(r);
            if (currentRow.getLastCellNum() == 0) {
                continue;
            }
            RankEntity rankEntity = new RankEntity();
            rankEntity.setPosition(r);
            rankEntity.setDate(date);
            String characterName = currentRow.getCell(0).getStringCellValue();
            String convertedName = convertCharacterName(characterName);
            CharacterEntity character = characters.get(convertedName);
            if (character == null) {
                throw new UndefinedCharacterException(characterName + " does not exists");
            }
            rankEntity.setCharacter(character);
            // Read cells after the character name
            for (int index = 0; index < titles.size(); index++) {
                // Add 1 because you start reading after the character name
                Cell cell = currentRow.getCell(index + 1);
                String title = titles.get(index);
                if ("".equals(title)) continue;
                String value;
                if (cell.getCellType() == CellType.NUMERIC) {
                    value = cell.getNumericCellValue() + "";
                } else if (cell.getCellType() == CellType.FORMULA) {
                 value = cell.getCellFormula();
                } else {
                    value = cell.getStringCellValue();
                }
                try {
                    if (RANK_ADVISE_KEY_WORDS.contains(title)) {
                        rankEntity.getMappedAdvises().put(title, value);
                    } else {
                        Integer convertedValue = "".equals(value) ? -1 : convertRankToInt(value);
                        rankEntity.getMappedValues().put(title, convertedValue);
                    }
                } catch (UndefinedRankException e) {
                    e.printStackTrace();
                }
            }
            dbService.saveRank(rankEntity);
        }
    }

    private void loadTeamsAndFights() {
        List<TeamEntity> teams = new ArrayList<>();
        this.loadEpreuveTeams(teams);
        // Load others fights
        for (int sheetIndex = 0; sheetIndex < this.excelWB.getNumberOfSheets(); sheetIndex++) {
            Sheet currentSheet = this.excelWB.getSheetAt(sheetIndex);
            String sheetName = currentSheet.getSheetName();
            if (FIGHTS_KEY_WORDS.contains(sheetName.split("-")[0])) {
                this.loadTeamsAndFightBySheet(teams, sheetName, 1, 2);
            }
        }
    }

    private void loadEpreuveTeams(List<TeamEntity> teams) {
        LOGGER.info("Load Epreuves teams");
        String currentCharacterName = "";

        Sheet sheet = this.excelWB.getSheet("Épreuves");
        for (int r = 1; r < sheet.getLastRowNum(); r+=5) {
            TeamEntity teamAlly = new TeamEntity();
            TeamEntity teamEnemy = new TeamEntity();
            TeamEntity guildTeamAlly = new TeamEntity();
            TeamEntity guildTeamEnemy = new TeamEntity();
            TeamEntity guildTeamAllyBis = new TeamEntity();
            if (sheet.getRow(r).getCell(1) != null && !sheet.getRow(r).getCell(1).getStringCellValue().isEmpty()) {
                currentCharacterName = sheet.getRow(r).getCell(1).getStringCellValue();
            }
            if (r+5 > sheet.getLastRowNum()) {
                continue;
            }
            for (int countRow = r; countRow < r+5; countRow++) {
                Row row = sheet.getRow(countRow);
                String teamAllyCharacterName = row.getCell(4).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(teamAlly, teamAllyCharacterName);
                String teamEnemyCharacterName = row.getCell(3).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(teamEnemy, teamEnemyCharacterName);
                String guildTeamAllyCharacterName = row.getCell(9).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(guildTeamAlly, guildTeamAllyCharacterName);
                String guildTeamEnemyCharacterName = row.getCell(10).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(guildTeamEnemy, guildTeamEnemyCharacterName);
                String guildTeamAllyBisCharacterName = row.getCell(11).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(guildTeamAllyBis, guildTeamAllyBisCharacterName);
            }

            teamAlly = this.saveTeam(teamAlly, teams);
            teamEnemy = this.saveTeam(teamEnemy, teams);
            guildTeamAlly = this.saveTeam(guildTeamAlly, teams);
            guildTeamEnemy = this.saveTeam(guildTeamEnemy, teams);
            guildTeamAllyBis = this.saveTeam(guildTeamAllyBis, teams);

            if (teamAlly != null && teamEnemy != null) {
                dbService.saveFight(new FightEntity(teamAlly, teamEnemy, "Épreuves-" + currentCharacterName));
                this.fightCount++;
            }
            if (guildTeamAlly != null && guildTeamEnemy != null) {
                dbService.saveFight(new FightEntity(guildTeamAlly, guildTeamEnemy, "Épreuves de guilde-" + currentCharacterName));
                this.fightCount++;
            }
            if (guildTeamAllyBis != null && guildTeamEnemy != null) {
                dbService.saveFight(new FightEntity(guildTeamAllyBis, guildTeamEnemy, "Épreuves de guilde-" + currentCharacterName + "-2"));
                this.fightCount++;
            }
        }
    }

    private void loadTeamsAndFightBySheet(List<TeamEntity> teams, String sheetName, int ally, int enemy) {
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
                this.addCharacterToTeam(t1, character1Name);
                String character2Name = row.getCell(enemy).getStringCellValue().toLowerCase();
                this.addCharacterToTeam(t2, character2Name);
                if (character1Name.isEmpty() && character2Name.isEmpty()) break;
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

    private void addCharacterToTeam(TeamEntity teamEntity, String characterName) {
        CharacterEntity character = characters.get(characterName.toLowerCase());
        if (character != null) {
            teamEntity.addCharacter(character);
        } else {
            if (!characterName.isEmpty()) {
                LOGGER.info(characterName + " n'existe pas");
            }
        }
    }
}
