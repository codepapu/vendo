package com.vendo.retailer.repository;

import com.vendo.retailer.entity.CommissionTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CommissionTrackingRepository extends JpaRepository<CommissionTracking, Long> {
    
    List<CommissionTracking> findByRetailerId(Long retailerId);
    
    Page<CommissionTracking> findByRetailerId(Long retailerId, Pageable pageable);
    
    List<CommissionTracking> findByRetailerIdAndStatus(Long retailerId, String status);
    
    List<CommissionTracking> findByOrderId(Long orderId);
    
    List<CommissionTracking> findByOrderItemId(Long orderItemId);
    
    @Query("SELECT SUM(ct.commissionAmount) FROM CommissionTracking ct WHERE ct.retailer.id = :retailerId")
    BigDecimal getTotalCommissionsByRetailerId(@Param("retailerId") Long retailerId);
    
    @Query("SELECT SUM(ct.commissionAmount) FROM CommissionTracking ct WHERE ct.retailer.id = :retailerId AND ct.status = :status")
    BigDecimal getTotalCommissionsByRetailerIdAndStatus(@Param("retailerId") Long retailerId, @Param("status") String status);
    
    @Query("SELECT COUNT(ct) FROM CommissionTracking ct WHERE ct.retailer.id = :retailerId")
    Long getCommissionCountByRetailerId(@Param("retailerId") Long retailerId);
    
    @Query("SELECT ct FROM CommissionTracking ct WHERE ct.retailer.id = :retailerId " +
           "AND (:status IS NULL OR ct.status = :status) " +
           "AND (:startDate IS NULL OR ct.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR ct.createdAt <= :endDate)")
    Page<CommissionTracking> findCommissionsByRetailerWithFilters(
            @Param("retailerId") Long retailerId,
            @Param("status") String status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);
    
    @Query("SELECT SUM(ct.commissionAmount) FROM CommissionTracking ct " +
           "WHERE ct.retailer.id = :retailerId " +
           "AND ct.settlementDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCommissionsByRetailerAndDateRange(
            @Param("retailerId") Long retailerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

