package com.vendo.product.repository;

import com.vendo.product.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRepository extends JpaRepository<Tax, Long> {
    
    List<Tax> findByStatus(String status);
    
    @Query("SELECT t FROM Tax t WHERE t.status = 'published' ORDER BY t.priority ASC")
    List<Tax> findActiveTaxes();
    
    @Query("SELECT t FROM Tax t JOIN t.products p WHERE p.id = :productId AND t.status = 'published'")
    List<Tax> findByProductId(@Param("productId") Long productId);
}
