package com.vendo.retailer.repository;

import com.vendo.retailer.entity.ProductImportHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImportHistoryRepository extends JpaRepository<ProductImportHistory, Long> {
    
    List<ProductImportHistory> findByRetailerId(Long retailerId);
    
    Page<ProductImportHistory> findByRetailerId(Long retailerId, Pageable pageable);
    
    Page<ProductImportHistory> findByRetailerIdOrderByCreatedAtDesc(Long retailerId, Pageable pageable);
}

