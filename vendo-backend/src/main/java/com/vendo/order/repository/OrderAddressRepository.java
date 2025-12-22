package com.vendo.order.repository;

import com.vendo.order.entity.OrderAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, Long> {
    
    List<OrderAddress> findByOrderId(Long orderId);
    
    Optional<OrderAddress> findByOrderIdAndType(Long orderId, String type);
    
    List<OrderAddress> findByOrderIdOrderByType(Long orderId);
}
