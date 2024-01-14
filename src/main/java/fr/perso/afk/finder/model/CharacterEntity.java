package fr.perso.afk.finder.model;

import fr.perso.afk.finder.model.characteristics.FactionEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author: Hugo Bourniche
 * 21/11/2021
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "characters")
public class CharacterEntity {

    @Id
    private String name;
    private String fullName;
    private String type;
    private String classe;
    private String role;
    private String summary;
    private Integer rank;

    @ManyToOne(fetch= FetchType.EAGER)
    private FactionEntity faction;
}
