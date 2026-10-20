package org.greta.eshop_api.unit;

import org.greta.eshop_api.persistence.documents.SearchLogDocument;
import org.greta.eshop_api.persistence.repositories.SearchLogRepository;
import org.greta.eshop_api.services.SearchLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Aucun MongoDB ici : le repository est un faux objet piloté par Mockito
@ExtendWith(MockitoExtension.class)
class SearchLogServiceTest {

    @Mock
    private SearchLogRepository searchLogRepository;

    @InjectMocks
    private SearchLogService searchLogService;

    @Test
    @DisplayName("Une recherche est journalisée avec ses seuls filtres et une date posée par le serveur")
    void recordsFiltersAndServerDate() {
        searchLogService.record(Map.of("keyword", "figue"), 1);

        ArgumentCaptor<SearchLogDocument> saved = ArgumentCaptor.forClass(SearchLogDocument.class);
        verify(searchLogRepository).save(saved.capture());
        assertEquals(Map.of("keyword", "figue"), saved.getValue().getFilters());
        assertEquals(1, saved.getValue().getResultCount());
        assertNotNull(saved.getValue().getSearchedAt());
    }

    @Test
    @DisplayName("MongoDB injoignable : l'écriture échoue sans faire échouer la recherche")
    void unreachableMongoDoesNotBreakTheSearch() {
        when(searchLogRepository.save(any(SearchLogDocument.class)))
                .thenThrow(new DataAccessResourceFailureException("Timed out while waiting for a server"));

        assertDoesNotThrow(() -> searchLogService.record(Map.of("keyword", "figue"), 1));
    }
}
