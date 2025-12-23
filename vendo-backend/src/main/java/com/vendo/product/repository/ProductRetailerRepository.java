package com.vendo.product.repository;

import com.vendo.product.entity.ProductRetailer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRetailerRepository extends JpaRepository<ProductRetailer, Long> {
    
    Optional<ProductRetailer> findByProductIdAndRetailerId(Long productId, Long retailerId);
    
    List<ProductRetailer> findByProductId(Long productId);
    
    List<ProductRetailer> findByRetailerId(Long retailerId);
    
    Page<ProductRetailer> findByRetailerId(Long retailerId, Pageable pageable);
    
    List<ProductRetailer> findByRetailerIdAndIsActiveTrue(Long retailerId);
    
    Page<ProductRetailer> findByRetailerIdAndIsActive(Long retailerId, Boolean isActive, Pageable pageable);
    
    @Query("SELECT pr FROM ProductRetailer pr WHERE pr.retailer.id = :retailerId AND " +
           "pr.stockQuantity <= pr.lowStockThreshold AND pr.isActive = true")
    List<ProductRetailer> findLowStockProductsByRetailer(@Param("retailerId") Long retailerId);
    
    @Query("SELECT pr FROM ProductRetailer pr WHERE pr.product.id = :productId AND pr.isActive = true")
    List<ProductRetailer> findActiveRetailersByProduct(@Param("productId") Long productId);
    
    boolean existsByProductIdAndRetailerId(Long productId, Long retailerId);
}

