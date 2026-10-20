package org.greta.eshop_api.exposition.controllers;

import org.greta.eshop_api.exposition.dtos.FavoriteResponseDTO;
import org.greta.eshop_api.services.FavoriteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
}
