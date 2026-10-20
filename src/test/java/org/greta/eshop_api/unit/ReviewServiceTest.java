package org.greta.eshop_api.unit;

import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.ReviewRequestDTO;
import org.greta.eshop_api.exposition.dtos.ReviewResponseDTO;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.entities.ReviewEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.persistence.repositories.ReviewRepository;
import org.greta.eshop_api.services.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Aucun Spring ici : les trois repositories sont des faux objets pilotés par Mockito
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CandleRepository candleRepository;

    @InjectMocks
    private ReviewService reviewService;

    private CustomerEntity ines() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1);
        customer.setFirstName("Inès");
        customer.setLastName("Moreau");
        return customer;
    }

    private CandleEntity lavender() {
        CandleEntity candle = new CandleEntity();
        candle.setId(6);
        candle.setName("Lavande de Provence");
        return candle;
    }

    @Test
    @DisplayName("Un avis valide est enregistré au nom de la cliente reçue, en attente de relecture")
    void createsReviewForTheGivenCustomer() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(ines()));
        when(candleRepository.findById(6)).thenReturn(Optional.of(lavender()));
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(invocation -> {
            ReviewEntity saved = invocation.getArgument(0);
            saved.setId(26);
            return saved;
        });

        ReviewResponseDTO response = reviewService.create(1, 6, new ReviewRequestDTO(5, "Elle sent la Provence."));

        assertEquals(26, response.id());
        assertEquals(5, response.rating());
        assertEquals("Inès", response.author());
        // Un nouvel avis attend la relecture de Camille avant d'être public
        assertFalse(response.approved());
        verify(reviewRepository).save(any(ReviewEntity.class));
    }

    @Test
    @DisplayName("Une bougie introuvable lève ResourceNotFoundException et rien n'est enregistré")
    void throwsWhenCandleIsMissing() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(ines()));
        when(candleRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reviewService.create(1, 99, new ReviewRequestDTO(4, "Introuvable.")));

        assertEquals("La bougie 99 n'existe pas.", exception.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Les avis d'une bougie introuvable lèvent ResourceNotFoundException")
    void listingThrowsWhenCandleIsMissing() {
        when(candleRepository.existsById(99)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> reviewService.findApprovedByCandle(99));

        assertEquals("La bougie 99 n'existe pas.", exception.getMessage());
        verify(reviewRepository, never()).findApprovedByCandleId(any());
    }
}
