package com.vendo.content.repository;

import com.vendo.content.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {
    
    Optional<BlogCategory> findBySlug(String slug);
    
    List<BlogCategory> findByParentId(Long parentId);
    
    List<BlogCategory> findByParentIdIsNull();
}
