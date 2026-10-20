package org.greta.eshop_api.exposition.controllers;

import jakarta.validation.Valid;
import org.greta.eshop_api.exposition.dtos.OrderRequestDTO;
import org.greta.eshop_api.exposition.dtos.OrderResponseDTO;
import org.greta.eshop_api.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<String> getAllOrders() {
        return orderService.findAllSummaries();
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO created = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
