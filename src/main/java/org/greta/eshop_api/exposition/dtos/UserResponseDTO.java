package org.greta.eshop_api.exposition.dtos;

public record UserResponseDTO(
        Integer id,
        String email,
        String role
) {}
