package org.greta.eshop_api.exposition.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record SearchLogResponseDTO(
        String id,
        LocalDateTime searchedAt,
        Map<String, Object> filters,
        int resultCount
) {}
