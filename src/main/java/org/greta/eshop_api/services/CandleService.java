package org.greta.eshop_api.services;

import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CandleService {

    private final CandleRepository candleRepository;

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public List<CandleEntity> findAll() {
        return candleRepository.findAll();
    }

    public List<CandleEntity> search(String keyword) {
        return candleRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<CandleEntity> findByMaxPrice(BigDecimal price) {
        return candleRepository.findByPriceLessThanEqual(price);
    }
}
