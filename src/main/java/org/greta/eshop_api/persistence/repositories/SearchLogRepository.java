package org.greta.eshop_api.persistence.repositories;

import org.greta.eshop_api.persistence.documents.SearchLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchLogRepository extends MongoRepository<SearchLogDocument, String> {

    List<SearchLogDocument> findTop20ByOrderBySearchedAtDesc();
}
