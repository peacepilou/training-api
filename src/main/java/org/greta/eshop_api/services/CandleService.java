package org.greta.eshop_api.services;

import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.exposition.dtos.CandleRequestDTO;
import org.greta.eshop_api.exposition.dtos.CandleResponseDTO;
import org.greta.eshop_api.mappers.CandleMapper;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CandleService {

    // DECIMAL(6,2) dans schema.sql : 9999.99 au plus
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999.99");

    private final CandleRepository candleRepository;

    public CandleService(CandleRepository candleRepository) {
        this.candleRepository = candleRepository;
    }

    /**
     * Renvoie toutes les bougies du catalogue.
     * @return toutes les bougies
     */
    public List<CandleResponseDTO> findAll() {
        return toDtos(candleRepository.findAll());
    }

    /**
     * Renvoie une bougie.
     * @param id l'id de la bougie
     * @return la bougie, ou null si elle n'existe pas
     */
    public CandleResponseDTO findById(Integer id) {
        CandleEntity candle = candleRepository.findById(id).orElse(null);
        if (candle == null) {
            return null;
        }
        return CandleMapper.toDto(candle);
    }

    /**
     * Cherche les bougies dont le nom contient ce mot, majuscules ignorées.
     * @param keyword le mot cherché
     * @return les bougies trouvées, éventuellement aucune
     */
    public List<CandleResponseDTO> search(String keyword) {
        return toDtos(candleRepository.findByNameContainingIgnoreCase(keyword));
    }

    /**
     * Cherche les bougies qui coûtent ce prix ou moins.
     * @param price le prix maximum
     * @return les bougies trouvées, éventuellement aucune
     */
    public List<CandleResponseDTO> findByMaxPrice(BigDecimal price) {
        return toDtos(candleRepository.findByPriceLessThanEqual(price));
    }

    /**
     * Enregistre une nouvelle bougie, après les règles métier.
     * @param dto la bougie à créer
     * @return la bougie enregistrée, avec son id
     * @throws org.greta.eshop_api.exceptions.BusinessRuleException si une règle métier est violée
     */
    public CandleResponseDTO create(CandleRequestDTO dto) {
        CandleEntity candle = CandleMapper.toEntity(dto);
        checkBusinessRules(candle);
        CandleEntity saved = candleRepository.save(candle);
        return CandleMapper.toDto(saved);
    }

    /**
     * Remplace les champs d'une bougie, après les règles métier.
     * @param id l'id de la bougie
     * @param dto les nouvelles valeurs
     * @return la bougie modifiée, ou null si elle n'existe pas
     * @throws org.greta.eshop_api.exceptions.BusinessRuleException si une règle métier est violée
     */
    public CandleResponseDTO update(Integer id, CandleRequestDTO dto) {
        if (!candleRepository.existsById(id)) {
            return null;
        }
        CandleEntity candle = CandleMapper.toEntity(dto);
        // L'id du chemin : save fait un UPDATE au lieu d'un INSERT
        candle.setId(id);
        checkBusinessRules(candle);
        CandleEntity saved = candleRepository.save(candle);
        return CandleMapper.toDto(saved);
    }

    /**
     * Supprime une bougie.
     * @param id l'id de la bougie
     * @return false si elle n'existe pas
     */
    public boolean delete(Integer id) {
        if (!candleRepository.existsById(id)) {
            return false;
        }
        candleRepository.deleteById(id);
        return true;
    }

    private List<CandleResponseDTO> toDtos(List<CandleEntity> candles) {
        List<CandleResponseDTO> dtos = new ArrayList<>();
        for (CandleEntity candle : candles) {
            dtos.add(CandleMapper.toDto(candle));
        }
        return dtos;
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
