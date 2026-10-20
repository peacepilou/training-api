package org.greta.eshop_api.exposition.controllers;

import org.greta.eshop_api.exposition.dtos.SearchLogResponseDTO;
import org.greta.eshop_api.services.SearchLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Sous /admin/** : la SecurityConfig la réserve déjà au rôle ADMIN
@RestController
@RequestMapping("/admin/search-logs")
public class SearchLogController {

    private final SearchLogService searchLogService;

    public SearchLogController(SearchLogService searchLogService) {
        this.searchLogService = searchLogService;
    }

    @GetMapping
    public ResponseEntity<List<SearchLogResponseDTO>> getLatestSearches() {
        return ResponseEntity.ok(searchLogService.findLatest());
    }
}
