package org.greta.eshop_api.repository;

import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

// Seule la couche JPA démarre, sur H2 : ni controller, ni sécurité. Chaque test est annulé à la fin
@DataJpaTest
@ActiveProfiles("integration")
class CandleRepositoryTest {

    @Autowired
    private CandleRepository candleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Integer candleId;

    @BeforeEach
    void setUp() {
        CandleEntity candle = new CandleEntity();
        candle.setName("Encens d'église");
        candle.setScent("Encens");
        candle.setSize("500 g");
        candle.setPrice(new BigDecimal("55.00"));
        candle.setStock(1);
        candleId = entityManager.persistFlushFind(candle).getId();
    }

    // L'UPDATE écrit en base sans passer par les entités chargées : clear() force à relire la ligne
    private int stockInDatabase() {
        entityManager.clear();
        return candleRepository.findById(candleId).orElseThrow().getStock();
    }

    @Test
    @DisplayName("Assez de stock : une ligne modifiée, le stock baisse")
    void decrementsWhenStockIsEnough() {
        assertThat(candleRepository.decrementStock(candleId, 1)).isEqualTo(1);
        assertThat(stockInDatabase()).isZero();
    }

    @Test
    @DisplayName("Stock insuffisant : zéro ligne modifiée, le stock ne bouge pas")
    void changesNothingWhenStockIsShort() {
        assertThat(candleRepository.decrementStock(candleId, 2)).isZero();
        assertThat(stockInDatabase()).isEqualTo(1);
    }
}
