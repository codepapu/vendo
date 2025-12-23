package com.vendo.inventory.repository;

import com.vendo.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    
    List<StockMovement> findByInventoryId(Long inventoryId);
    
    List<StockMovement> findByInventoryIdOrderByCreatedAtDesc(Long inventoryId);
    
    List<StockMovement> findByMovementType(String movementType);
    
    List<StockMovement> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
    
    List<StockMovement> findByCreatedBy(Long createdBy);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.inventory.id = :inventoryId ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByInventoryId(@Param("inventoryId") Long inventoryId, Pageable pageable);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementType = :movementType AND " +
           "sm.createdAt BETWEEN :startDate AND :endDate")
    List<StockMovement> findByMovementTypeAndDateRange(@Param("movementType") String movementType,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.inventory.product.id = :productId AND " +
           "(:retailerId IS NULL OR sm.inventory.retailer.id = :retailerId) AND " +
           "(:warehouseId IS NULL OR sm.inventory.warehouse.id = :warehouseId) " +
           "ORDER BY sm.createdAt DESC")
    Page<StockMovement> findByProductAndOptionalRetailerAndWarehouse(@Param("productId") Long productId,
                                                                      @Param("retailerId") Long retailerId,
                                                                      @Param("warehouseId") Long warehouseId,
                                                                      Pageable pageable);
    
    @Query("SELECT sm FROM StockMovement sm WHERE sm.referenceType = :referenceType AND " +
           "sm.referenceId = :referenceId")
    List<StockMovement> findByReference(@Param("referenceType") String referenceType,
                                        @Param("referenceId") Long referenceId);
}

