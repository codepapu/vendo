package com.vendo.order.repository;

import com.vendo.order.entity.OrderReturnHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReturnHistoryRepository extends JpaRepository<OrderReturnHistory, Long> {
    
    @Query("SELECT orh FROM OrderReturnHistory orh WHERE orh.orderReturn.id = :returnId")
    List<OrderReturnHistory> findByReturnId(@Param("returnId") Long returnId);
    
    @Query("SELECT orh FROM OrderReturnHistory orh WHERE orh.orderReturn.id = :returnId ORDER BY orh.createdAt DESC")
    List<OrderReturnHistory> findByReturnIdOrderByCreatedAtDesc(@Param("returnId") Long returnId);
}
