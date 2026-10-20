package org.greta.eshop_api.exposition.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequestDTO(
        @NotNull(message = "La cliente est obligatoire")
        Integer customerId,

        // Facultative : sans adresse, la commande est retirée à l'atelier
        Integer addressId,

        @NotEmpty(message = "La commande doit contenir au moins une ligne")
        List<@Valid OrderLineRequestDTO> lines
) {}
