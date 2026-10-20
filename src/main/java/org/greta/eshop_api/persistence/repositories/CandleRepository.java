package org.greta.eshop_api.persistence.repositories;

import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CandleRepository extends JpaRepository<CandleEntity, Integer> {

    List<CandleEntity> findByNameContainingIgnoreCase(String keyword);

    List<CandleEntity> findByPriceLessThanEqual(BigDecimal price);

    // Lecture du stock et écriture en une seule requête : MySQL verrouille la ligne,
    // deux commandes simultanées ne peuvent plus vendre le même dernier exemplaire.
    // Renvoie le nombre de lignes modifiées : 0 si le stock ne suffit pas
    @Modifying
    @Query("UPDATE CandleEntity c SET c.stock = c.stock - :quantity WHERE c.id = :id AND c.stock >= :quantity")
    int decrementStock(@Param("id") Integer id, @Param("quantity") int quantity);
}
