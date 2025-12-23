package com.vendo.order.repository;

import com.vendo.order.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    
    List<PurchaseOrder> findByBusinessAccountId(Long businessAccountId);
    
    List<PurchaseOrder> findByOrderId(Long orderId);
    
    List<PurchaseOrder> findByStatus(String status);
    
    Page<PurchaseOrder> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    Page<PurchaseOrder> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.businessAccount.id = :businessAccountId AND po.status = :status")
    List<PurchaseOrder> findByBusinessAccountIdAndStatus(@Param("businessAccountId") Long businessAccountId, 
                                                          @Param("status") String status);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.order.id = :orderId AND po.status = :status")
    Optional<PurchaseOrder> findByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") String status);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.requestedBy.id = :userId")
    List<PurchaseOrder> findByRequestedBy(@Param("userId") Long userId);
    
    @Query("SELECT po FROM PurchaseOrder po WHERE po.approvedBy.id = :userId")
    List<PurchaseOrder> findByApprovedBy(@Param("userId") Long userId);
}

