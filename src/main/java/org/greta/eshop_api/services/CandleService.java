package org.greta.eshop_api.services;

import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CandleService {

    // DECIMAL(6,2) dans schema.sql : 9999.99 au plus
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999.99");

    private final CandleRepository candleRepository;

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    public List<CandleEntity> findAll() {
        return candleRepository.findAll();
    }

    public CandleEntity findById(Integer id) {
        return candleRepository.findById(id).orElse(null);
    }

    public List<CandleEntity> search(String keyword) {
        return candleRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<CandleEntity> findByMaxPrice(BigDecimal price) {
        return candleRepository.findByPriceLessThanEqual(price);
    }

    public CandleEntity create(CandleEntity candle) {
        checkBusinessRules(candle);
        return candleRepository.save(candle);
    }

    public CandleEntity update(Integer id, CandleEntity newData) {
        CandleEntity existing = candleRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setName(newData.getName());
        existing.setScent(newData.getScent());
        existing.setSize(newData.getSize());
        existing.setPrice(newData.getPrice());
        existing.setStock(newData.getStock());
        existing.setOnSale(newData.isOnSale());
        checkBusinessRules(existing);
        return candleRepository.save(existing);
    }

    public boolean delete(Integer id) {
        if (!candleRepository.existsById(id)) {
            return false;
        }
        candleRepository.deleteById(id);
        return true;
    }

    private void checkBusinessRules(CandleEntity candle) {
        if (candle.getPrice() == null || candle.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Le prix doit être supérieur à 0.");
        }
        if (candle.getPrice().compareTo(MAX_PRICE) > 0) {
            throw new BusinessRuleException("Le prix ne peut pas dépasser 9 999,99 €.");
        }
        if (candle.getStock() < 0) {
            throw new BusinessRuleException("Le stock ne peut pas être négatif.");
        }
    }
}
