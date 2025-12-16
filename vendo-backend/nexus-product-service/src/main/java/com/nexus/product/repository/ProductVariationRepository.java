package com.nexus.product.repository;

import com.nexus.product.entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariationRepository extends JpaRepository<ProductVariation, Long> {
    
    List<ProductVariation> findByConfigurableProductId(Long configurableProductId);
    
    List<ProductVariation> findByProductId(Long productId);
    
    @Query("SELECT v FROM ProductVariation v WHERE v.configurableProduct.id = :productId AND v.isDefault = true")
    Optional<ProductVariation> findDefaultVariationByConfigurableProductId(@Param("productId") Long productId);
    
    @Query("SELECT v FROM ProductVariation v WHERE v.configurableProduct.id = :productId")
    List<ProductVariation> findAllVariationsByConfigurableProductId(@Param("productId") Long productId);
}
