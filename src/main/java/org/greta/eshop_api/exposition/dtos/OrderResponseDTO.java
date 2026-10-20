package org.greta.eshop_api.exposition.dtos;

import java.util.List;

public record OrderResponseDTO(
        Integer id,
        String status,
        Integer customerId,
        Integer addressId,
        List<OrderLineResponseDTO> lines
) {}
