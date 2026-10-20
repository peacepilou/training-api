package org.greta.eshop_api.persistence.documents;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

// Une recherche du catalogue, hors du MCD : forme variable, lue en bloc pour les statistiques, jamais jointe
@Document(collection = "search_logs")
@NoArgsConstructor
@Getter
@Setter
public class SearchLogDocument {

    // Généré par MongoDB : un ObjectId de 24 caractères hexadécimaux, pas un entier
    @Id
    private String id;

    private LocalDateTime searchedAt;

    // Seuls les filtres tapés : {"keyword": "figue"} une fois, {"maxPrice": 25} la suivante.
    // Dans une table, chaque filtre possible serait une colonne, vide la plupart du temps
    private Map<String, Object> filters;

    private int resultCount;
}
