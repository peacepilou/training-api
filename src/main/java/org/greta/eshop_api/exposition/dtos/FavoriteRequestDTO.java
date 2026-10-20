package org.greta.eshop_api.exposition.dtos;

import jakarta.validation.constraints.NotNull;

// La cliente vient de l'adresse : le corps ne porte que la bougie
public record FavoriteRequestDTO(

        @NotNull(message = "La bougie est obligatoire")
        Integer candleId
) {}
