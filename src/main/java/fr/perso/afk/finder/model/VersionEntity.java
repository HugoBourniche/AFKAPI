package fr.perso.afk.finder.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author: Hugo Bourniche
 * 26/12/2023
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "version")
public class VersionEntity {

    //******************************************************************************************************************
    // VARIABLE
    //******************************************************************************************************************

    @Id
    private String version;

    //******************************************************************************************************************
    // CONSTRUCTOR
    //******************************************************************************************************************

    //******************************************************************************************************************
    // METHODS
    //******************************************************************************************************************

    public boolean isUpdated(String version) {
        return Integer.parseInt(this.version) < Integer.parseInt(version);
    }

}
