package org.greta.eshop_api.mappers;

import org.greta.eshop_api.exposition.dtos.SearchLogResponseDTO;
import org.greta.eshop_api.persistence.documents.SearchLogDocument;

import java.time.LocalDateTime;
import java.util.Map;

public class SearchLogMapper {

    // La date vient de l'horloge du serveur, jamais du client
    public static SearchLogDocument toDocument(Map<String, Object> filters, int resultCount) {
        SearchLogDocument document = new SearchLogDocument();
        document.setSearchedAt(LocalDateTime.now());
        document.setFilters(filters);
        document.setResultCount(resultCount);
        return document;
    }

    public static SearchLogResponseDTO toDto(SearchLogDocument document) {
        return new SearchLogResponseDTO(
                document.getId(),
                document.getSearchedAt(),
                document.getFilters(),
                document.getResultCount()
        );
    }
}
