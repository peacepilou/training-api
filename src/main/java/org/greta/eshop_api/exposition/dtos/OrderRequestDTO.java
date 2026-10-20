package org.greta.eshop_api.exposition.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// Pas de customerId : la commande est toujours passée pour la cliente connectée
public record OrderRequestDTO(
        // Facultative : sans adresse, la commande est retirée à l'atelier
        Integer addressId,

        @NotEmpty(message = "La commande doit contenir au moins une ligne")
        List<@Valid OrderLineRequestDTO> lines
) {}
