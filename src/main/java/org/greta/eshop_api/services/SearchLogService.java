package org.greta.eshop_api.services;

import org.greta.eshop_api.exposition.dtos.SearchLogResponseDTO;
import org.greta.eshop_api.mappers.SearchLogMapper;
import org.greta.eshop_api.persistence.documents.SearchLogDocument;
import org.greta.eshop_api.persistence.repositories.SearchLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchLogService {

    private static final Logger log = LoggerFactory.getLogger(SearchLogService.class);

    private final SearchLogRepository searchLogRepository;

    public SearchLogService(SearchLogRepository searchLogRepository) {
        this.searchLogRepository = searchLogRepository;
    }

    // Le journal est secondaire : MongoDB injoignable ne doit pas faire échouer la recherche elle-même
    public void record(Map<String, Object> filters, int resultCount) {
        try {
            searchLogRepository.save(SearchLogMapper.toDocument(filters, resultCount));
        } catch (DataAccessException e) {
            log.warn("Recherche non journalisée : {}", e.getMessage());
        }
    }

    public List<SearchLogResponseDTO> findLatest() {
        List<SearchLogResponseDTO> dtos = new ArrayList<>();
        for (SearchLogDocument document : searchLogRepository.findTop20ByOrderBySearchedAtDesc()) {
            dtos.add(SearchLogMapper.toDto(document));
        }
        return dtos;
    }
}
