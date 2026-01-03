package com.vendo.product.repository;

import com.vendo.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByStatus(String status);
    
    List<Category> findByParentId(Long parentId);
    
    List<Category> findByParentIsNull();
    
    List<Category> findByIsFeaturedTrue();
    
    @Query("SELECT c FROM Category c WHERE c.status = :status ORDER BY c.sortOrder ASC")
    List<Category> findByStatusOrderByOrderAsc(String status);
    
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.status = 'published' ORDER BY c.sortOrder ASC")
    List<Category> findRootCategories();
    
    boolean existsByName(String name);
}
