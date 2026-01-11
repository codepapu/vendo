package com.vendo.search.repository;

import com.vendo.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for ProductDocument
 */
@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {
    
    Page<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description, Pageable pageable);
    
    List<ProductDocument> findByNameContaining(String name);
    
    Page<ProductDocument> findByStatus(String status, Pageable pageable);
}

