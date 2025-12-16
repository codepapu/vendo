package com.nexus.product.repository;

import com.nexus.product.entity.ProductReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {
    
    List<ProductReview> findByProductId(Long productId);
    
    List<ProductReview> findByProductIdAndStatus(Long productId, String status);
    
    Page<ProductReview> findByProductIdAndStatus(Long productId, String status, Pageable pageable);
    
    Optional<ProductReview> findByProductIdAndCustomerId(Long productId, Long customerId);
    
    @Query("SELECT AVG(r.star) FROM ProductReview r WHERE r.product.id = :productId AND r.status = 'published'")
    Double calculateAverageRating(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.status = 'published'")
    Long countByProductId(@Param("productId") Long productId);
    
    @Query("SELECT r FROM ProductReview r WHERE r.product.id = :productId AND r.status = 'published' ORDER BY r.createdAt DESC")
    List<ProductReview> findPublishedReviewsByProductId(@Param("productId") Long productId);
}
