package com.vendo.order.repository;

import com.vendo.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findByProductId(Long productId);
    
    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH oi.retailer LEFT JOIN FETCH oi.order WHERE oi.order.id = :orderId AND oi.product.id = :productId")
    Optional<OrderItem> findByOrderIdAndProductId(@Param("orderId") Long orderId, @Param("productId") Long productId);
    
    @Query("SELECT DISTINCT oi FROM OrderItem oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH oi.order o WHERE o.user.id = :userId AND oi.product.id = :productId AND o.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED')")
    List<OrderItem> findPurchasedItemsByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);
    
    // Marketplace/retailer queries
    @Query("SELECT DISTINCT oi FROM OrderItem oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH oi.order WHERE oi.retailer.id = :retailerId")
    List<OrderItem> findByRetailerId(@Param("retailerId") Long retailerId);
    
    @Query("SELECT DISTINCT oi FROM OrderItem oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH oi.retailer LEFT JOIN FETCH oi.order WHERE oi.order.id = :orderId AND oi.retailer.id = :retailerId")
    List<OrderItem> findByOrderIdAndRetailerId(@Param("orderId") Long orderId, @Param("retailerId") Long retailerId);
    
    @Query("SELECT DISTINCT oi FROM OrderItem oi LEFT JOIN FETCH oi.product LEFT JOIN FETCH oi.order o WHERE oi.retailer.id = :retailerId AND o.status = :status")
    List<OrderItem> findByRetailerIdAndOrderStatus(@Param("retailerId") Long retailerId, @Param("status") String status);
}
