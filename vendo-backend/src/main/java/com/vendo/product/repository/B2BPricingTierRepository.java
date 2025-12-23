package com.vendo.product.repository;

import com.vendo.product.entity.B2BPricingTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface B2BPricingTierRepository extends JpaRepository<B2BPricingTier, Long> {
    
    List<B2BPricingTier> findByProductId(Long productId);
    
    List<B2BPricingTier> findByBusinessType(String businessType);
    
    List<B2BPricingTier> findByIsActive(Boolean isActive);
    
    @Query("SELECT pt FROM B2BPricingTier pt WHERE pt.product.id = :productId AND pt.businessType = :businessType AND pt.isActive = true")
    List<B2BPricingTier> findByProductIdAndBusinessType(@Param("productId") Long productId, 
                                                         @Param("businessType") String businessType);
    
    @Query("SELECT pt FROM B2BPricingTier pt WHERE pt.product.id = :productId AND pt.businessType = :businessType " +
           "AND pt.isActive = true AND :quantity >= pt.minQuantity " +
           "AND (pt.maxQuantity IS NULL OR :quantity <= pt.maxQuantity) " +
           "ORDER BY pt.minQuantity DESC")
    Optional<B2BPricingTier> findApplicableTier(@Param("productId") Long productId, 
                                                  @Param("businessType") String businessType, 
                                                  @Param("quantity") Integer quantity);
    
    @Query("SELECT pt FROM B2BPricingTier pt WHERE pt.product.id = :productId AND pt.isActive = true " +
           "AND :quantity >= pt.minQuantity AND (pt.maxQuantity IS NULL OR :quantity <= pt.maxQuantity) " +
           "ORDER BY pt.minQuantity DESC")
    List<B2BPricingTier> findApplicableTiersByProductAndQuantity(@Param("productId") Long productId, 
                                                                   @Param("quantity") Integer quantity);
}

