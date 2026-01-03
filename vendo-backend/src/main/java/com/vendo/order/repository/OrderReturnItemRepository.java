package com.vendo.order.repository;

import com.vendo.order.entity.OrderReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderReturnItemRepository extends JpaRepository<OrderReturnItem, Long> {
    
    @Query("SELECT ori FROM OrderReturnItem ori WHERE ori.orderReturn.id = :returnId")
    List<OrderReturnItem> findByReturnId(@Param("returnId") Long returnId);
}
