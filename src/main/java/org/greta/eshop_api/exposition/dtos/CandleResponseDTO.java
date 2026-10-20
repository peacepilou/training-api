package org.greta.eshop_api.exposition.dtos;

import java.math.BigDecimal;

public record CandleResponseDTO(
        Integer id,
        String name,
        String scent,
        String size,
        BigDecimal price,
        int stock,
        boolean onSale
) {}
