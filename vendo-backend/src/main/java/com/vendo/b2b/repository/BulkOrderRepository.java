package com.vendo.b2b.repository;

import com.vendo.b2b.entity.BulkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkOrderRepository extends JpaRepository<BulkOrder, Long> {
    
    List<BulkOrder> findByBusinessAccountId(Long businessAccountId);
    
    Page<BulkOrder> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    List<BulkOrder> findByStatus(String status);
    
    @Query("SELECT bo FROM BulkOrder bo WHERE bo.businessAccount.id = :businessAccountId AND bo.status = :status")
    List<BulkOrder> findByBusinessAccountIdAndStatus(@Param("businessAccountId") Long businessAccountId, 
                                                      @Param("status") String status);
    
    @Query("SELECT bo FROM BulkOrder bo WHERE bo.createdBy.id = :userId")
    List<BulkOrder> findByCreatedBy(@Param("userId") Long userId);
}

