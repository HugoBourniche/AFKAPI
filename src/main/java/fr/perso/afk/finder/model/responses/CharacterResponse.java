package fr.perso.afk.finder.model.responses;


import fr.perso.afk.finder.model.CharacterEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Hugo Bourniche
 * 30/11/2021
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CharacterResponse {

    private String name;
    private String fullName;
    private String faction;
    private String type;
    private String classe;
    private String role;
    private Integer rank;
    private Integer position;

    public CharacterResponse(CharacterEntity character, Integer position) {
        this(   character.getName(),
                character.getFullName(),
                character.getFaction().getName(),
                character.getType(),
                character.getClasse(),
                character.getRole(),
                character.getRank(),
                position
        );
    }

    @Override
    public String toString() {
        return "CharacterResponse{" +
                "name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", faction='" + faction + '\'' +
                ", type='" + type + '\'' +
                ", classe='" + classe + '\'' +
                ", role='" + role + '\'' +
                ", rank='" + rank + '\'' +
                '}';
    }
}
