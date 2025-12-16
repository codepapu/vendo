package com.nexus.order.repository;

import com.nexus.order.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {
    
    List<OrderProduct> findByOrderId(Long orderId);
    
    List<OrderProduct> findByProductId(Long productId);
}
