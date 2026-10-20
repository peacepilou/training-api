package org.greta.eshop_api.unit;

import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.CandleRequestDTO;
import org.greta.eshop_api.exposition.dtos.CandleResponseDTO;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.services.CandleService;
import org.greta.eshop_api.services.SearchLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Aucun Spring ici : le repository est un faux objet piloté par Mockito
@ExtendWith(MockitoExtension.class)
class CandleServiceTest {

    @Mock
    private CandleRepository candleRepository;

    @Mock
    private SearchLogService searchLogService;

    @InjectMocks
    private CandleService candleService;

    private CandleRequestDTO request(String price, int stock) {
        return new CandleRequestDTO("Sapin des Vosges", "Sapin", "500 g", new BigDecimal(price), stock, true);
    }

    @Test
    @DisplayName("Une bougie valide est enregistrée puis renvoyée")
    void createsValidCandle() {
        // Arrange : le repository renvoie la bougie qu'on lui donne, avec un id
        when(candleRepository.save(any(CandleEntity.class))).thenAnswer(invocation -> {
            CandleEntity saved = invocation.getArgument(0);
            saved.setId(42);
            return saved;
        });

        // Act
        CandleResponseDTO response = candleService.create(request("48.00", 5));

        // Assert
        assertEquals(42, response.id());
        assertEquals(new BigDecimal("48.00"), response.price());
        verify(candleRepository).save(any(CandleEntity.class));
    }

    @Test
    @DisplayName("Un prix à 0 est refusé et rien n'est enregistré")
    void rejectsZeroPrice() {
        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> candleService.create(request("0", 5)));

        assertEquals("Le prix doit être supérieur à 0.", exception.getMessage());
        verify(candleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Un prix au-dessus de 9 999,99 € est refusé")
    void rejectsPriceAboveLimit() {
        assertThrows(BusinessRuleException.class, () -> candleService.create(request("10000.00", 5)));
        verify(candleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Une modification qui rend le stock négatif est refusée")
    void rejectsNegativeStockOnUpdate() {
        CandleEntity existing = new CandleEntity();
        existing.setId(1);
        when(candleRepository.findById(1)).thenReturn(Optional.of(existing));

        assertThrows(BusinessRuleException.class, () -> candleService.update(1, request("48.00", -1)));
        verify(candleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Une bougie introuvable lève ResourceNotFoundException")
    void throwsWhenCandleIsMissing() {
        when(candleRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> candleService.findById(99));

        assertEquals("La bougie 99 n'existe pas.", exception.getMessage());
    }
}
