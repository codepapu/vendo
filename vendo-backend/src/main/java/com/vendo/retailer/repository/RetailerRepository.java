package com.vendo.retailer.repository;

import com.vendo.retailer.entity.Retailer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RetailerRepository extends JpaRepository<Retailer, Long> {
    
    Optional<Retailer> findByStoreSlug(String storeSlug);
    
    Optional<Retailer> findByUserId(Long userId);
    
    boolean existsByStoreSlug(String storeSlug);
    
    @Query("SELECT r FROM Retailer r WHERE " +
           "(LOWER(r.storeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND r.status = 'ACTIVE'")
    Page<Retailer> findByStoreNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            @Param("searchTerm") String searchTerm,
            Pageable pageable);
}

