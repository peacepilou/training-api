package org.greta.eshop_api.exposition.controllers;

import jakarta.validation.Valid;
import org.greta.eshop_api.exposition.dtos.FavoriteRequestDTO;
import org.greta.eshop_api.exposition.dtos.FavoriteResponseDTO;
import org.greta.eshop_api.services.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Sans authentification pour l'instant, la cliente est désignée dans l'adresse
@RestController
@RequestMapping("/customers/{customerId}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponseDTO>> getFavorites(@PathVariable Integer customerId) {
        List<FavoriteResponseDTO> favorites = favoriteService.findByCustomer(customerId);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    public ResponseEntity<FavoriteResponseDTO> addFavorite(@PathVariable Integer customerId, @Valid @RequestBody FavoriteRequestDTO request) {
        FavoriteResponseDTO saved = favoriteService.add(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/{candleId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable Integer customerId, @PathVariable Integer candleId) {
        favoriteService.remove(customerId, candleId);
        return ResponseEntity.noContent().build();
    }
}
