package org.greta.eshop_api.services;

import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandleService {

    private final CandleRepository candleRepository;

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
        System.out.println("CandleService construit");
    }

    public List<String> findAll() {
        return candleRepository.findAll();
    }
}
