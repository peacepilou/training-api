package org.greta.eshop_api.exposition.dtos;

public record LoginResponseDTO(
        String token,
        String email,
        String role
) {}
