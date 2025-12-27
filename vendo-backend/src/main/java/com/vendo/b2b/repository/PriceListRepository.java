package com.vendo.b2b.repository;

import com.vendo.b2b.entity.PriceList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {
    
    List<PriceList> findByBusinessAccountId(Long businessAccountId);
    
    Page<PriceList> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    List<PriceList> findByIsActive(Boolean isActive);
    
    @Query("SELECT pl FROM PriceList pl WHERE pl.businessAccount.id = :businessAccountId AND pl.isActive = true " +
           "AND pl.effectiveFrom <= :date AND (pl.effectiveUntil IS NULL OR pl.effectiveUntil >= :date)")
    List<PriceList> findActivePriceListsForBusinessAccount(@Param("businessAccountId") Long businessAccountId, 
                                                            @Param("date") LocalDate date);
    
    @Query("SELECT pl FROM PriceList pl WHERE pl.businessAccount.id = :businessAccountId AND pl.isDefault = true")
    Optional<PriceList> findDefaultPriceListForBusinessAccount(@Param("businessAccountId") Long businessAccountId);
}

