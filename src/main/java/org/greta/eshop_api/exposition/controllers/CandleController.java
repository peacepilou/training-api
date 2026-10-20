package org.greta.eshop_api.exposition.controllers;

import org.greta.eshop_api.persistence.entities.CandleEntity;
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
    public ResponseEntity<List<CandleEntity>> getAllCandles() {
        List<CandleEntity> candles = candleService.findAll();
        return ResponseEntity.ok(candles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandleEntity> getCandleById(@PathVariable Integer id) {
        CandleEntity candle = candleService.findById(id);
        if (candle == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(candle);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CandleEntity>> searchCandles(@RequestParam String keyword) {
        List<CandleEntity> candles = candleService.search(keyword);
        return ResponseEntity.ok(candles);
    }

    @GetMapping("/max-price")
    public ResponseEntity<List<CandleEntity>> getCandlesByMaxPrice(@RequestParam BigDecimal price) {
        List<CandleEntity> candles = candleService.findByMaxPrice(price);
        return ResponseEntity.ok(candles);
    }

    @PostMapping
    public ResponseEntity<CandleEntity> createCandle(@RequestBody CandleEntity candle) {
        CandleEntity saved = candleService.create(candle);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandleEntity> updateCandle(@PathVariable Integer id, @RequestBody CandleEntity newData) {
        CandleEntity updated = candleService.update(id, newData);
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
