package com.vendo.product.repository;

import com.vendo.product.entity.ProductCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCollectionRepository extends JpaRepository<ProductCollection, Long> {
    
    Optional<ProductCollection> findBySlug(String slug);
    
    List<ProductCollection> findByStatus(String status);
    
    List<ProductCollection> findByIsFeaturedTrue();
    
    boolean existsBySlug(String slug);
}
