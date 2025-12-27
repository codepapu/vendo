package com.vendo.tax.repository;

import com.vendo.tax.entity.TaxExemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaxExemptionRepository extends JpaRepository<TaxExemption, Long> {
    
    @Query("SELECT e FROM TaxExemption e WHERE e.isActive = true " +
           "AND (e.businessAccountId = :businessAccountId OR e.userId = :userId) " +
           "AND (e.validFrom IS NULL OR e.validFrom <= :now) " +
           "AND (e.validUntil IS NULL OR e.validUntil >= :now)")
    List<TaxExemption> findActiveExemptions(
        @Param("businessAccountId") Long businessAccountId,
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );
    
    @Query("SELECT e FROM TaxExemption e WHERE e.isActive = true " +
           "AND e.businessAccountId = :businessAccountId " +
           "AND (e.validFrom IS NULL OR e.validFrom <= :now) " +
           "AND (e.validUntil IS NULL OR e.validUntil >= :now)")
    List<TaxExemption> findByBusinessAccountId(
        @Param("businessAccountId") Long businessAccountId,
        @Param("now") LocalDateTime now
    );
    
    @Query("SELECT e FROM TaxExemption e WHERE e.isActive = true " +
           "AND e.userId = :userId " +
           "AND (e.validFrom IS NULL OR e.validFrom <= :now) " +
           "AND (e.validUntil IS NULL OR e.validUntil >= :now)")
    List<TaxExemption> findByUserId(
        @Param("userId") Long userId,
        @Param("now") LocalDateTime now
    );
    
    @Query("SELECT e FROM TaxExemption e WHERE e.isActive = true " +
           "AND (e.businessAccountId = :businessAccountId OR e.userId = :userId) " +
           "AND (e.country = :country OR e.country IS NULL) " +
           "AND (e.state = :state OR e.state IS NULL) " +
           "AND (e.productType = :productType OR e.productType IS NULL) " +
           "AND (e.validFrom IS NULL OR e.validFrom <= :now) " +
           "AND (e.validUntil IS NULL OR e.validUntil >= :now)")
    List<TaxExemption> findMatchingExemptions(
        @Param("businessAccountId") Long businessAccountId,
        @Param("userId") Long userId,
        @Param("country") String country,
        @Param("state") String state,
        @Param("productType") String productType,
        @Param("now") LocalDateTime now
    );
}

