package org.greta.eshop_api.persistence.repositories;

import java.util.List;

public class CandleRepository {

    // Catalogue en mémoire : la base de données arrive au chapitre 03
    private final List<String> candleNames = List.of(
            "Nuit d'ambre",
            "Feu de bois",
            "Jardin de Grasse"
    );

    public CandleRepository() {
        System.out.println("CandleRepository construit");
    }

    public List<String> findAll() {
        return candleNames;
    }
}
