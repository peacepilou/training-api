package org.greta.eshop_api.exposition.dtos;

import java.math.BigDecimal;

public record CandleRequestDTO(
        String name,
        String scent,
        String size,
        BigDecimal price,
        Integer stock,
        Boolean onSale
) {}
