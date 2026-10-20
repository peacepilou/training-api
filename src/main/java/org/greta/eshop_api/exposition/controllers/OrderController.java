package org.greta.eshop_api.exposition.controllers;

import jakarta.validation.Valid;
import org.greta.eshop_api.exposition.dtos.OrderRequestDTO;
import org.greta.eshop_api.exposition.dtos.OrderResponseDTO;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<String> getOrders(@AuthenticationPrincipal CustomerEntity customer) {
        return orderService.findSummariesVisibleTo(customer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Integer id,
                                                     @AuthenticationPrincipal CustomerEntity customer) {
        return ResponseEntity.ok(orderService.findByIdVisibleTo(id, customer));
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@AuthenticationPrincipal CustomerEntity customer,
                                                        @Valid @RequestBody OrderRequestDTO request) {
        // La cliente vient du jeton, jamais du corps : personne ne commande au nom d'une autre
        OrderResponseDTO created = orderService.create(customer.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
