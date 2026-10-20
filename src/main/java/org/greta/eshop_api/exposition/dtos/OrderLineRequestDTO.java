package org.greta.eshop_api.exposition.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderLineRequestDTO(
        @NotNull(message = "La bougie est obligatoire")
        Integer candleId,

        @NotNull(message = "La quantité est obligatoire")
        @Positive(message = "La quantité doit être positive")
        Integer quantity
) {}
