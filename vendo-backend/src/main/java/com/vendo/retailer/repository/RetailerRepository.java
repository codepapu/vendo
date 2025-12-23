package com.vendo.retailer.repository;

import com.vendo.retailer.entity.Retailer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RetailerRepository extends JpaRepository<Retailer, Long> {
    
    Optional<Retailer> findByStoreSlug(String storeSlug);
    
    Optional<Retailer> findByUserId(Long userId);
    
    boolean existsByStoreSlug(String storeSlug);
}

