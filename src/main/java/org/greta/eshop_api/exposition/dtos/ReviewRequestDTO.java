package org.greta.eshop_api.exposition.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Pas de customerId : l'avis est toujours écrit par la cliente connectée
public record ReviewRequestDTO(
        @NotNull(message = "La note est obligatoire")
        @Min(value = 1, message = "La note va de 1 à 5")
        @Max(value = 5, message = "La note va de 1 à 5")
        Integer rating,

        @NotBlank(message = "Le commentaire est obligatoire")
        @Size(max = 1000, message = "Le commentaire ne doit pas dépasser 1000 caractères")
        String comment
) {}
