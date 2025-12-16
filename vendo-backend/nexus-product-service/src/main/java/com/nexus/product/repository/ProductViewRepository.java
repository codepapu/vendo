package com.nexus.product.repository;

import com.nexus.product.entity.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {
    
    List<ProductView> findByProductId(Long productId);
    
    Optional<ProductView> findByProductIdAndDate(Long productId, LocalDate date);
    
    @Query("SELECT SUM(pv.views) FROM ProductView pv WHERE pv.product.id = :productId")
    Long getTotalViewsByProductId(@Param("productId") Long productId);
    
    @Query("SELECT pv FROM ProductView pv WHERE pv.product.id = :productId ORDER BY pv.date DESC")
    List<ProductView> findViewsByProductIdOrderByDateDesc(@Param("productId") Long productId);
}
