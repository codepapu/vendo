package com.vendo.product.repository;

import com.vendo.product.entity.ProductVariationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariationItemRepository extends JpaRepository<ProductVariationItem, Long> {
    
    List<ProductVariationItem> findByVariationId(Long variationId);
    
    List<ProductVariationItem> findByAttributeId(Long attributeId);
    
    @Query("SELECT vi FROM ProductVariationItem vi WHERE vi.variation.id = :variationId")
    List<ProductVariationItem> findAllByVariationId(@Param("variationId") Long variationId);
    
    @Query("SELECT vi FROM ProductVariationItem vi WHERE vi.attribute.id = :attributeId")
    List<ProductVariationItem> findAllByAttributeId(@Param("attributeId") Long attributeId);
    
    @Query("SELECT vi FROM ProductVariationItem vi WHERE vi.variation.id = :variationId AND vi.attribute.id = :attributeId")
    List<ProductVariationItem> findByVariationIdAndAttributeId(@Param("variationId") Long variationId, @Param("attributeId") Long attributeId);
    
    void deleteByVariationId(Long variationId);
}
