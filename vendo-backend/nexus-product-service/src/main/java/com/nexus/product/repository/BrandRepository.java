package com.nexus.product.repository;

import com.nexus.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    Optional<Brand> findByName(String name);
    
    List<Brand> findByStatus(String status);
    
    List<Brand> findByIsFeaturedTrue();
    
    @Query("SELECT b FROM Brand b WHERE b.status = :status ORDER BY b.order ASC")
    List<Brand> findByStatusOrderByOrderAsc(String status);
    
    boolean existsByName(String name);
}
