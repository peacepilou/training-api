package org.greta.eshop_api.persistence.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

// Aucune table pour cette classe : sa colonne s'ajoute à la table de chaque entité qui l'étend.
// Seul l'id est commun : schema.sql ne donne de colonne de date commune à aucune table
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    // INT AUTO_INCREMENT dans schema.sql : Integer côté Java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
}
