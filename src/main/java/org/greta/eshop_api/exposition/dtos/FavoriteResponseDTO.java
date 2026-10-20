package org.greta.eshop_api.exposition.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FavoriteResponseDTO(
        Integer candleId,
        String candleName,
        BigDecimal price,
        LocalDateTime addedAt
) {}
