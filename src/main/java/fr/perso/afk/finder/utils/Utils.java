package fr.perso.afk.finder.utils;

import fr.perso.afk.finder.exceptions.UndefinedRankException;

public class Utils {


    public static Integer convertRanksToInt(String pveRank, String pvpRank, String bossRank) {
        try {
            Integer pveRankInt = convertRankToInt(pveRank);
            Integer pvpRankInt = convertRankToInt(pvpRank);
            Integer bossRankInt = convertRankToInt(bossRank);
            return Math.min(( pveRankInt + pvpRankInt + bossRankInt ) / 3, 6); // Max rank is 6
        } catch (UndefinedRankException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static Integer convertRankToInt(String rank) throws UndefinedRankException {
        switch (rank) {
            case "S+":
            case "S": return 0;
            case "A+": return 1;
            case "A": return 2;
            case "B+": return 3;
            case "B": return 4;
            case "C+":
            case "C": return 5;
            case "D+": return 6;
            case "D": return 7;
            default: throw new UndefinedRankException("Rank " + rank + " does not exist");
        }
    }

}
