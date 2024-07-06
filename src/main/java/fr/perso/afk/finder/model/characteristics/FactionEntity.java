package fr.perso.afk.finder.model.characteristics;

import fr.perso.afk.finder.model.CharacterEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Hugo Bourniche
 * 21/11/2021
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "factions")
public class FactionEntity {

    public static List<FactionEntity> FACTIONS = List.of(
            new FactionEntity("Dimensional", "", "", null),
            new FactionEntity("Draconis", "Draconis", "Draconis", null),
            new FactionEntity("Celestial", "Hypogean", "Hypogean", null),
            new FactionEntity("Hypogean", "Celestial", "Celestial", null),
            new FactionEntity("Lightbearer", "Graveborn", "Mauler", null),
            new FactionEntity("Graveborn", "Wilder", "Lightbearer", null),
            new FactionEntity("Wilder", "Mauler", "Graveborn", null),
            new FactionEntity("Mauler", "Wilder", "Lightbearer", null)
            );

    @Id
    private String name;
    private String weakness;
    private String strongness;

    @OneToMany(fetch = FetchType.EAGER) @JoinColumn(name="faction_name")
    public List<CharacterEntity> characters = new ArrayList<>();

    public static FactionEntity getFactionByName(String name) {
        switch(name) {
            case "Dimensionals"  : return FACTIONS.get(0);
            case "Draconis"      : return FACTIONS.get(1);
            case "Celestials"    : return FACTIONS.get(2);
            case "Hypogeans"     : return FACTIONS.get(3);
            case "Lightbearers"  : return FACTIONS.get(4);
            case "Graveborns"    : return FACTIONS.get(5);
            case "Wilders"       : return FACTIONS.get(6);
            case "Maulers"       : return FACTIONS.get(7);
            default: return new FactionEntity("", "", "", null);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeakness() {
        return weakness;
    }

    public void setWeakness(String weakness) {
        this.weakness = weakness;
    }

    public String getStrongness() {
        return strongness;
    }

    public void setStrongness(String strongness) {
        this.strongness = strongness;
    }
}
