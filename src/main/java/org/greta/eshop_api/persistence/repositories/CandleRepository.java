package org.greta.eshop_api.persistence.repositories;

import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, Integer> {

    List<CandleEntity> findByNameContainingIgnoreCase(String keyword);

    List<CandleEntity> findByPriceLessThanEqual(BigDecimal price);
}
