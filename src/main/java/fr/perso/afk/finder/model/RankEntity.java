package fr.perso.afk.finder.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "ranks")
@NoArgsConstructor
public class RankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;
    private Integer position;
//    private Integer value;
    private LocalDate date;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rank_values", joinColumns = @JoinColumn(name = "rank_id"))
    @MapKeyColumn(name = "type")
    @Column(name = "value")
    private Map<String, Integer> mappedValues = new HashMap<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "rank_advises", joinColumns = @JoinColumn(name = "rank_id"))
    @MapKeyColumn(name = "type")
    @Column(name = "advise")
    private Map<String, String> mappedAdvises = new HashMap<>();
    @ManyToOne
    @JoinColumn(name = "name")
    private CharacterEntity character;

    public Integer getValue() {
        int overAllValue = 0;
        for (Integer value : mappedValues.values()) {
            overAllValue = overAllValue + value;
        }
        return Math.min(overAllValue / mappedValues.size(), 6); // Max rank is 6
    }
}
