package org.greta.eshop_api.exposition.dtos;

import java.math.BigDecimal;

public record OrderLineResponseDTO(
        Integer candleId,
        String candleName,
        int quantity,
        BigDecimal unitPrice
) {}
