package com.vendo.retailer.repository;

import com.vendo.retailer.entity.RetailerPayout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RetailerPayoutRepository extends JpaRepository<RetailerPayout, Long> {
    
    List<RetailerPayout> findByRetailerId(Long retailerId);
    
    Page<RetailerPayout> findByRetailerId(Long retailerId, Pageable pageable);
    
    List<RetailerPayout> findByRetailerIdAndStatus(Long retailerId, String status);
    
    Page<RetailerPayout> findByRetailerIdAndStatus(Long retailerId, String status, Pageable pageable);
    
    @Query("SELECT p FROM RetailerPayout p WHERE p.status = :status")
    List<RetailerPayout> findByStatus(@Param("status") String status);
    
    @Query("SELECT p FROM RetailerPayout p WHERE p.status = :status")
    Page<RetailerPayout> findByStatus(@Param("status") String status, Pageable pageable);
    
    @Query("SELECT p FROM RetailerPayout p WHERE p.retailer.id = :retailerId " +
           "AND p.periodStart >= :startDate AND p.periodEnd <= :endDate")
    List<RetailerPayout> findByRetailerIdAndPeriod(
            @Param("retailerId") Long retailerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

