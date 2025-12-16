package com.nexus.product.repository;

import com.nexus.product.entity.ProductFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductFileRepository extends JpaRepository<ProductFile, Long> {
    
    List<ProductFile> findByProductId(Long productId);
}
