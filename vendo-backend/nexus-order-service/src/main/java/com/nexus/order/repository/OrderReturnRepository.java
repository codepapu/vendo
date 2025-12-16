package com.nexus.order.repository;

import com.nexus.order.entity.OrderReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderReturnRepository extends JpaRepository<OrderReturn, Long> {
    
    Optional<OrderReturn> findByCode(String code);
    
    List<OrderReturn> findByOrderId(Long orderId);
    
    List<OrderReturn> findByUserId(Long userId);
    
    List<OrderReturn> findByReturnStatus(String returnStatus);
    
    @Query("SELECT r FROM OrderReturn r WHERE r.order.id = :orderId ORDER BY r.createdAt DESC")
    List<OrderReturn> findReturnsByOrderId(@Param("orderId") Long orderId);
}
