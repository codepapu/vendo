package com.vendo.product.repository;

import com.vendo.product.entity.RetailerReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RetailerReviewRepository extends JpaRepository<RetailerReview, Long> {
    
    List<RetailerReview> findByRetailerId(Long retailerId);
    
    Page<RetailerReview> findByRetailerId(Long retailerId, Pageable pageable);
    
    List<RetailerReview> findByRetailerIdAndIsApprovedTrue(Long retailerId);
    
    Page<RetailerReview> findByRetailerIdAndIsApproved(Long retailerId, Boolean isApproved, Pageable pageable);
    
    List<RetailerReview> findByUserId(Long userId);
    
    Optional<RetailerReview> findByRetailerIdAndUserIdAndOrderId(Long retailerId, Long userId, Long orderId);
    
    @Query("SELECT AVG(r.rating) FROM RetailerReview r WHERE r.retailer.id = :retailerId AND r.isApproved = true")
    Double getAverageRatingByRetailerId(@Param("retailerId") Long retailerId);
    
    @Query("SELECT COUNT(r) FROM RetailerReview r WHERE r.retailer.id = :retailerId AND r.isApproved = true")
    Long countApprovedReviewsByRetailerId(@Param("retailerId") Long retailerId);
    
    @Query("SELECT r FROM RetailerReview r WHERE r.retailer.id = :retailerId AND r.isApproved = true ORDER BY r.createdAt DESC")
    Page<RetailerReview> findApprovedReviewsByRetailerIdOrderByCreatedAtDesc(@Param("retailerId") Long retailerId, Pageable pageable);
}

