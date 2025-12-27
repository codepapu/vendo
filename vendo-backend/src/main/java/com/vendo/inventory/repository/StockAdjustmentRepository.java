package com.vendo.inventory.repository;

import com.vendo.inventory.entity.StockAdjustment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, Long> {
    
    List<StockAdjustment> findByInventoryId(Long inventoryId);
    
    List<StockAdjustment> findByStatus(String status);
    
    Page<StockAdjustment> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.inventory.id = :inventoryId ORDER BY sa.createdAt DESC")
    List<StockAdjustment> findAdjustmentHistoryByInventoryId(@Param("inventoryId") Long inventoryId);
    
    @Query("SELECT sa FROM StockAdjustment sa WHERE sa.status = 'PENDING' AND sa.requiresApproval = true")
    List<StockAdjustment> findPendingApprovals();
}

