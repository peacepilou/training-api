package org.greta.eshop_api.mappers;

import org.greta.eshop_api.exposition.dtos.ReviewResponseDTO;
import org.greta.eshop_api.persistence.entities.ReviewEntity;

public class ReviewMapper {

    public static ReviewResponseDTO toDto(ReviewEntity entity) {
        return new ReviewResponseDTO(
                entity.getId(),
                entity.getRating(),
                entity.getComment(),
                entity.getCustomer().getFirstName(),
                entity.getReviewedAt(),
                entity.isApproved()
        );
    }
}
