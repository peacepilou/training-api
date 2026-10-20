package org.greta.eshop_api.exposition.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Les tailles max suivent les colonnes de database/schema.sql
public record CandleRequestDTO(

        @NotBlank(message = "Le nom de la bougie ne peut pas être vide")
        @Size(max = 80, message = "Le nom de la bougie ne doit pas dépasser 80 caractères")
        String name,

        @NotBlank(message = "Le parfum ne peut pas être vide")
        @Size(max = 40, message = "Le parfum ne doit pas dépasser 40 caractères")
        String scent,

        @NotBlank(message = "La taille ne peut pas être vide")
        @Size(max = 10, message = "La taille ne doit pas dépasser 10 caractères")
        @Pattern(regexp = "^[0-9]+ g$", message = "La taille s'écrit en grammes, par exemple 200 g")
        String size,

        @NotNull(message = "Le prix est obligatoire")
        @Positive(message = "Le prix doit être positif")
        BigDecimal price,

        // Integer et pas int : un int n'est jamais null, @NotNull n'y vérifierait rien.
        // Avec un int absent du JSON, Jackson 3 répond 400 "JSON parse error", sans message par champ
        @NotNull(message = "Le stock est obligatoire")
        @PositiveOrZero(message = "Le stock ne peut pas être négatif")
        Integer stock,

        // Facultatif : absent, la bougie est en vente (DEFAULT TRUE en base)
        Boolean onSale
) {}
