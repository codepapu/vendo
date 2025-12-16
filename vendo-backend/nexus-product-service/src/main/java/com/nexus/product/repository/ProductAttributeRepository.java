package com.nexus.product.repository;

import com.nexus.product.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    
    List<ProductAttribute> findByAttributeSetId(Long attributeSetId);
    
    List<ProductAttribute> findByAttributeSetIdOrderByOrderAsc(Long attributeSetId);
}
