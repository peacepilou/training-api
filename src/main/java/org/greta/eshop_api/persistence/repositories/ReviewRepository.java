package org.greta.eshop_api.persistence.repositories;

import org.greta.eshop_api.persistence.entities.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {

    // Seuls les avis relus par Camille sont publics. L'autrice vient dans la même requête (pas de N+1)
    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.customer WHERE r.candle.id = :candleId AND r.approved = true ORDER BY r.reviewedAt DESC")
    List<ReviewEntity> findApprovedByCandleId(@Param("candleId") Integer candleId);
}
