package com.nexus.product.repository;

import com.nexus.product.entity.ProductAttributeSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributeSetRepository extends JpaRepository<ProductAttributeSet, Long> {
    
    Optional<ProductAttributeSet> findBySlug(String slug);
    
    List<ProductAttributeSet> findByStatus(String status);
    
    boolean existsBySlug(String slug);
}
