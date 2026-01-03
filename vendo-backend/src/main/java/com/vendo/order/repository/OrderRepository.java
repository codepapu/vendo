package com.vendo.order.repository;

import com.vendo.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    @Query("SELECT o FROM Order o WHERE o.orderNumber = :code")
    Optional<Order> findByCode(@Param("code") String code);
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    List<Order> findByUserId(@Param("userId") Long userId);
    
    List<Order> findByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId")
    Page<Order> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    Page<Order> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.user.id = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    Double sumAmountByStatusAndDateRange(@Param("status") String status, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    // Enhanced order queries for B2B, B2C, and Retailer orders
    Page<Order> findByBusinessAccountId(Long businessAccountId, Pageable pageable);
    
    Page<Order> findByOrderType(String orderType, Pageable pageable);
    
    Page<Order> findByPaymentStatus(String paymentStatus, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.businessAccount.id = :businessAccountId AND o.orderType = :orderType")
    Page<Order> findByBusinessAccountIdAndOrderType(@Param("businessAccountId") Long businessAccountId, 
                                                     @Param("orderType") String orderType, 
                                                     Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.orderType = :orderType AND o.status = :status")
    Page<Order> findByOrderTypeAndStatus(@Param("orderType") String orderType, 
                                          @Param("status") String status, 
                                          Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.businessAccount.id = :businessAccountId AND o.orderType = :orderType AND o.status = :status")
    Page<Order> findByBusinessAccountIdAndOrderTypeAndStatus(@Param("businessAccountId") Long businessAccountId,
                                                              @Param("orderType") String orderType,
                                                              @Param("status") String status,
                                                              Pageable pageable);
}
