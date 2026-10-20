package org.greta.eshop_api.exposition.dtos;

import java.time.LocalDateTime;

// L'autrice n'est montrée que par son prénom : la liste des avis est publique
public record ReviewResponseDTO(
        Integer id,
        Integer rating,
        String comment,
        String author,
        LocalDateTime reviewedAt,
        boolean approved
) {}
