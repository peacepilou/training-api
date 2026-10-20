package org.greta.eshop_api.mappers;

import org.greta.eshop_api.exposition.dtos.FavoriteResponseDTO;
import org.greta.eshop_api.persistence.entities.FavoriteEntity;

public class FavoriteMapper {

    public static FavoriteResponseDTO toDto(FavoriteEntity entity) {
        return new FavoriteResponseDTO(
                entity.getCandle().getId(),
                entity.getCandle().getName(),
                entity.getCandle().getPrice(),
                entity.getAddedAt()
        );
    }
}
