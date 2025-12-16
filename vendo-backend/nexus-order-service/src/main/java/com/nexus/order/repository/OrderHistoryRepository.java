package com.nexus.order.repository;

import com.nexus.order.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {
    
    List<OrderHistory> findByOrderId(Long orderId);
    
    List<OrderHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    @Query("SELECT h FROM OrderHistory h WHERE h.order.id = :orderId ORDER BY h.createdAt DESC")
    List<OrderHistory> findOrderHistoryByOrderId(@Param("orderId") Long orderId);
}
