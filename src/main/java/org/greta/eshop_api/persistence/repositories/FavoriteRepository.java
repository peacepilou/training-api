package org.greta.eshop_api.persistence.repositories;

import org.greta.eshop_api.persistence.entities.FavoriteEntity;
import org.greta.eshop_api.persistence.entities.FavoriteId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FavoriteRepository extends JpaRepository<FavoriteEntity, FavoriteId> {

    // customer puis son id : les favoris d'une cliente. La bougie vient dans la même requête (pas de N+1)
    @EntityGraph(attributePaths = {"candle"})
    List<FavoriteEntity> findByCustomerId(Integer customerId);
}
