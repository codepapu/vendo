package com.vendo.product.repository;

import com.vendo.product.entity.ProductTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTagRepository extends JpaRepository<ProductTag, Long> {
    
    Optional<ProductTag> findByName(String name);
    
    List<ProductTag> findByStatus(String status);
    
    boolean existsByName(String name);
}
