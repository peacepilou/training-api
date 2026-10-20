package org.greta.eshop_api.exposition.controllers;

import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.services.CandleService;
import org.springframework.web.bind.annotation.GetMapping;
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
    public List<CandleEntity> getAllCandles() {
        return candleService.findAll();
    }

    @GetMapping("/search")
    public List<CandleEntity> searchCandles(@RequestParam String keyword) {
        return candleService.search(keyword);
    }

    @GetMapping("/max-price")
    public List<CandleEntity> getCandlesByMaxPrice(@RequestParam BigDecimal price) {
        return candleService.findByMaxPrice(price);
    }
}
