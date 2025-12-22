package com.vendo.product.repository;

import com.vendo.product.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    
    Optional<Discount> findByCode(String code);
    
    List<Discount> findByStatus(String status);
    
    @Query("SELECT d FROM Discount d WHERE d.status = :status AND " +
           "(d.startDate IS NULL OR d.startDate <= :now) AND " +
           "(d.endDate IS NULL OR d.endDate >= :now)")
    List<Discount> findActiveDiscounts(@Param("status") String status, @Param("now") LocalDateTime now);
    
    @Query("SELECT d FROM Discount d WHERE d.code = :code AND d.status = 'published' AND " +
           "(d.startDate IS NULL OR d.startDate <= :now) AND " +
           "(d.endDate IS NULL OR d.endDate >= :now)")
    Optional<Discount> findValidDiscountByCode(@Param("code") String code, @Param("now") LocalDateTime now);
    
    boolean existsByCode(String code);
}
