package org.greta.eshop_api.mappers;

import org.greta.eshop_api.exposition.dtos.CandleRequestDTO;
import org.greta.eshop_api.exposition.dtos.CandleResponseDTO;
import org.greta.eshop_api.persistence.entities.CandleEntity;

public class CandleMapper {

    public static CandleEntity toEntity(CandleRequestDTO dto) {
        CandleEntity entity = new CandleEntity();
        entity.setName(dto.name());
        entity.setScent(dto.scent());
        entity.setSize(dto.size());
        entity.setPrice(dto.price());
        entity.setStock(dto.stock());
        // En vente par défaut, comme le DEFAULT TRUE de la colonne on_sale
        entity.setOnSale(dto.onSale() == null || dto.onSale());
        return entity;
    }

    public static CandleResponseDTO toDto(CandleEntity entity) {
        return new CandleResponseDTO(
                entity.getId(),
                entity.getName(),
                entity.getScent(),
                entity.getSize(),
                entity.getPrice(),
                entity.getStock(),
                entity.isOnSale()
        );
    }
}
