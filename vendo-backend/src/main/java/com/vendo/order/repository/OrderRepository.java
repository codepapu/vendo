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
    
    Optional<Order> findByCode(String code);
    
    List<Order> findByUserId(Long userId);
    
    List<Order> findByStatus(String status);
    
    Page<Order> findByUserId(Long userId, Pageable pageable);
    
    Page<Order> findByStatus(String status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
    
    @Query("SELECT o FROM Order o WHERE o.storeId = :storeId AND o.status = :status")
    Page<Order> findByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") String status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT SUM(o.amount) FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    Double sumAmountByStatusAndDateRange(@Param("status") String status, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
}
