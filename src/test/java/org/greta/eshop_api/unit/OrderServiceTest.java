package org.greta.eshop_api.unit;

import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.OrderLineRequestDTO;
import org.greta.eshop_api.exposition.dtos.OrderRequestDTO;
import org.greta.eshop_api.exposition.dtos.OrderResponseDTO;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.persistence.entities.CustomerOrderEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.persistence.repositories.CustomerOrderRepository;
import org.greta.eshop_api.persistence.repositories.CustomerRepository;
import org.greta.eshop_api.services.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Aucun Spring ici : les trois repositories sont des faux objets pilotés par Mockito
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CustomerOrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CandleRepository candleRepository;

    @InjectMocks
    private OrderService orderService;

    // Sans adresse : retrait à l'atelier
    private OrderRequestDTO request(int candleId, int quantity) {
        return new OrderRequestDTO(null, List.of(new OrderLineRequestDTO(candleId, quantity)));
    }

    private CustomerEntity ines() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(1);
        customer.setFirstName("Inès");
        customer.setLastName("Moreau");
        return customer;
    }

    private CandleEntity nuitDAmbre() {
        CandleEntity candle = new CandleEntity();
        candle.setId(1);
        candle.setName("Nuit d'ambre");
        candle.setPrice(new BigDecimal("24.90"));
        return candle;
    }

    @Test
    @DisplayName("Une commande valide baisse le stock, est enregistrée puis renvoyée")
    void createsValidOrder() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(ines()));
        when(candleRepository.findById(1)).thenReturn(Optional.of(nuitDAmbre()));
        when(candleRepository.decrementStock(1, 3)).thenReturn(1);
        when(orderRepository.save(any(CustomerOrderEntity.class))).thenAnswer(invocation -> {
            CustomerOrderEntity saved = invocation.getArgument(0);
            saved.setId(36);
            return saved;
        });

        OrderResponseDTO response = orderService.create(1, request(1, 3));

        assertEquals(36, response.id());
        assertEquals(1, response.customerId());
        assertEquals(3, response.lines().get(0).quantity());
        // Le prix unitaire vient de la bougie en base, jamais du client
        assertEquals(new BigDecimal("24.90"), response.lines().get(0).unitPrice());
        verify(candleRepository).decrementStock(1, 3);
        verify(orderRepository).save(any(CustomerOrderEntity.class));
    }

    @Test
    @DisplayName("Un stock insuffisant est refusé et la commande n'est pas enregistrée")
    void rejectsInsufficientStock() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(ines()));
        when(candleRepository.findById(1)).thenReturn(Optional.of(nuitDAmbre()));
        when(candleRepository.decrementStock(1, 30)).thenReturn(0);

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> orderService.create(1, request(1, 30)));

        assertEquals("Stock insuffisant pour Nuit d'ambre (quantité demandée : 30).", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("La même bougie sur deux lignes est refusée avant toute baisse de stock")
    void rejectsSameCandleTwice() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(ines()));
        when(candleRepository.findById(1)).thenReturn(Optional.of(nuitDAmbre()));
        when(candleRepository.decrementStock(1, 1)).thenReturn(1);
        OrderRequestDTO twice = new OrderRequestDTO(null,
                List.of(new OrderLineRequestDTO(1, 1), new OrderLineRequestDTO(1, 2)));

        BusinessRuleException exception = assertThrows(BusinessRuleException.class,
                () -> orderService.create(1, twice));

        assertEquals("La bougie 1 apparaît deux fois : regroupe les quantités sur une seule ligne.", exception.getMessage());
        verify(candleRepository, never()).decrementStock(1, 2);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Une cliente introuvable lève ResourceNotFoundException, aucun stock ne bouge")
    void throwsWhenCustomerIsMissing() {
        when(customerRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.create(99, request(1, 3)));

        assertEquals("La cliente 99 n'existe pas.", exception.getMessage());
        verify(candleRepository, never()).decrementStock(anyInt(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Une bougie introuvable lève ResourceNotFoundException")
    void throwsWhenCandleIsMissing() {
        when(customerRepository.findById(1)).thenReturn(Optional.of(ines()));
        when(candleRepository.findById(99)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> orderService.create(1, request(99, 3)));

        assertEquals("La bougie 99 n'existe pas.", exception.getMessage());
        verify(orderRepository, never()).save(any());
    }
}
