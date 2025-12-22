package com.vendo.content.repository;

import com.vendo.content.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    
    Optional<BlogPost> findBySlug(String slug);
    
    Page<BlogPost> findByStatus(String status, Pageable pageable);
    
    Page<BlogPost> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<BlogPost> findByStatusAndCategoryId(String status, Long categoryId, Pageable pageable);
    
    @Query("SELECT bp FROM BlogPost bp WHERE bp.status = :status AND (bp.title LIKE %:keyword% OR bp.content LIKE %:keyword%)")
    Page<BlogPost> searchByKeyword(@Param("status") String status, @Param("keyword") String keyword, Pageable pageable);
}
