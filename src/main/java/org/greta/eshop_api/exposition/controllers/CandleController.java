package org.greta.eshop_api.exposition.controllers;

import org.greta.eshop_api.exposition.dtos.CandleRequestDTO;
import org.greta.eshop_api.exposition.dtos.CandleResponseDTO;
import org.greta.eshop_api.services.CandleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/candles")
public class CandleController {

    private final CandleService candleService;

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }

    @GetMapping
    public ResponseEntity<List<CandleResponseDTO>> getAllCandles() {
        List<CandleResponseDTO> candles = candleService.findAll();
        return ResponseEntity.ok(candles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandleResponseDTO> getCandleById(@PathVariable Integer id) {
        CandleResponseDTO candle = candleService.findById(id);
        if (candle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(candle);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CandleResponseDTO>> searchCandles(@RequestParam String keyword) {
        List<CandleResponseDTO> candles = candleService.search(keyword);
        return ResponseEntity.ok(candles);
    }

    @GetMapping("/max-price")
    public ResponseEntity<List<CandleResponseDTO>> getCandlesByMaxPrice(@RequestParam BigDecimal price) {
        List<CandleResponseDTO> candles = candleService.findByMaxPrice(price);
        return ResponseEntity.ok(candles);
    }

    @PostMapping
    public ResponseEntity<CandleResponseDTO> createCandle(@RequestBody CandleRequestDTO request) {
        CandleResponseDTO saved = candleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandleResponseDTO> updateCandle(@PathVariable Integer id, @RequestBody CandleRequestDTO request) {
        CandleResponseDTO updated = candleService.update(id, request);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandle(@PathVariable Integer id) {
        if (!candleService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
