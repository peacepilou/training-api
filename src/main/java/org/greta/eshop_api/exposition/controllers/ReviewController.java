package org.greta.eshop_api.exposition.controllers;

import jakarta.validation.Valid;
import org.greta.eshop_api.exposition.dtos.ReviewRequestDTO;
import org.greta.eshop_api.exposition.dtos.ReviewResponseDTO;
import org.greta.eshop_api.persistence.entities.CustomerEntity;
import org.greta.eshop_api.services.ReviewService;
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
@RequestMapping("/candles/{candleId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDTO>> getReviews(@PathVariable Integer candleId) {
        List<ReviewResponseDTO> reviews = reviewService.findApprovedByCandle(candleId);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@PathVariable Integer candleId,
                                                          @AuthenticationPrincipal CustomerEntity customer,
                                                          @Valid @RequestBody ReviewRequestDTO request) {
        // L'autrice vient du jeton, jamais du corps : personne n'écrit au nom d'une autre
        ReviewResponseDTO created = reviewService.create(customer.getId(), candleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
