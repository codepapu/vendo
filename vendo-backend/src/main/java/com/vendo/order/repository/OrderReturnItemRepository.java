package com.vendo.order.repository;

import com.vendo.order.entity.OrderReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReturnItemRepository extends JpaRepository<OrderReturnItem, Long> {
    
    List<OrderReturnItem> findByReturnId(Long returnId);
}
