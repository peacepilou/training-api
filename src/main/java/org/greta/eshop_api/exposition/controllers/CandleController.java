package org.greta.eshop_api.exposition.controllers;

import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.greta.eshop_api.services.CandleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/candles")
public class CandleController {

    private final CandleService candleService;

    public CandleController() {
        this.candleService = new CandleService(new CandleRepository());
        System.out.println("CandleController construit");
    }

    @GetMapping
    public List<String> getAllCandles() {
        return candleService.findAll();
    }
}
