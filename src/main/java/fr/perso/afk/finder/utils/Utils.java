package fr.perso.afk.finder.utils;

import fr.perso.afk.finder.exceptions.UndefinedRankException;

public class Utils {

    public static Integer convertRankToInt(String rank) throws UndefinedRankException {
        switch (rank) {
            case "SSS":
            case "SS":
            case "S+":
            case "S": return 0;
            case "S-":
            case "A+": return 1;
            case "A": return 2;
            case "B+": return 3;
            case "B": return 4;
            case "C+":
            case "C": return 5;
            case "D+": return 6;
            case "D": return 7;
            case "E":
            case "F": return 8;
            default: throw new UndefinedRankException("Rank " + rank + " does not exist");
        }
    }

    public static String convertCharacterName(String name) {
        if (name.contains("Awakened ")) {
            return name.split("Awakened ")[1].toLowerCase() + "s";
        }
        return name.toLowerCase();
    }
}
