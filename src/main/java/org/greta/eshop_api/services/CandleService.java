package org.greta.eshop_api.services;

import org.greta.eshop_api.exceptions.BusinessRuleException;
import org.greta.eshop_api.exceptions.ResourceNotFoundException;
import org.greta.eshop_api.exposition.dtos.CandleRequestDTO;
import org.greta.eshop_api.exposition.dtos.CandleResponseDTO;
import org.greta.eshop_api.exposition.dtos.PageResponseDTO;
import org.greta.eshop_api.mappers.CandleMapper;
import org.greta.eshop_api.persistence.entities.CandleEntity;
import org.greta.eshop_api.persistence.repositories.CandleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CandleService {

    // DECIMAL(6,2) dans schema.sql : 9999.99 au plus
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999.99");

    private final CandleRepository candleRepository;
    private final SearchLogService searchLogService;

    public CandleService(CandleRepository candleRepository, SearchLogService searchLogService) {
        this.candleRepository = candleRepository;
        this.searchLogService = searchLogService;
    }

    /**
     * Renvoie une page du catalogue, triée.
     * @param page le numéro de la page, à partir de 0
     * @param size le nombre de bougies par page
     * @param sortBy le champ de tri, par exemple price
     * @param direction asc ou desc
     * @return les bougies de la page et le total
     */
    // findAll(Pageable) vient de JpaRepository : rien à déclarer dans CandleRepository
    public PageResponseDTO<CandleResponseDTO> findPage(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<CandleEntity> result = candleRepository.findAll(PageRequest.of(page, size, sort));
        return new PageResponseDTO<>(
                toDtos(result.getContent()),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * Renvoie une bougie.
     * @param id l'id de la bougie
     * @return la bougie
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si elle n'existe pas
     */
    public CandleResponseDTO findById(Integer id) {
        CandleEntity candle = findExisting(id);
        return CandleMapper.toDto(candle);
    }

    /**
     * Cherche les bougies dont le nom contient ce mot, majuscules ignorées, et journalise la recherche.
     * @param keyword le mot cherché
     * @return les bougies trouvées, éventuellement aucune
     */
    public List<CandleResponseDTO> search(String keyword) {
        List<CandleResponseDTO> results = toDtos(candleRepository.findByNameContainingIgnoreCase(keyword));
        searchLogService.record(Map.of("keyword", keyword), results.size());
        return results;
    }

    /**
     * Cherche les bougies qui coûtent ce prix ou moins, et journalise la recherche.
     * @param price le prix maximum
     * @return les bougies trouvées, éventuellement aucune
     */
    public List<CandleResponseDTO> findByMaxPrice(BigDecimal price) {
        List<CandleResponseDTO> results = toDtos(candleRepository.findByPriceLessThanEqual(price));
        searchLogService.record(Map.of("maxPrice", price), results.size());
        return results;
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
     * @return la bougie modifiée
     * @throws org.greta.eshop_api.exceptions.BusinessRuleException si une règle métier est violée
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si elle n'existe pas
     */
    public CandleResponseDTO update(Integer id, CandleRequestDTO dto) {
        findExisting(id);
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
     * @throws org.greta.eshop_api.exceptions.ResourceNotFoundException si elle n'existe pas
     */
    public void delete(Integer id) {
        if (!candleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Impossible de supprimer : la bougie " + id + " n'existe pas.");
        }
        candleRepository.deleteById(id);
    }

    private CandleEntity findExisting(Integer id) {
        return candleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La bougie " + id + " n'existe pas."));
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
