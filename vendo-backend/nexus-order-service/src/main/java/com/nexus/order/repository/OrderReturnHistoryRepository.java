package com.nexus.order.repository;

import com.nexus.order.entity.OrderReturnHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReturnHistoryRepository extends JpaRepository<OrderReturnHistory, Long> {
    
    List<OrderReturnHistory> findByReturnId(Long returnId);
    
    List<OrderReturnHistory> findByReturnIdOrderByCreatedAtDesc(Long returnId);
}
