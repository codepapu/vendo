package com.vendo.product.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.repository.OrderItemRepository;
import com.vendo.order.repository.OrderRepository;
import com.vendo.product.dto.request.ReviewRequest;
import com.vendo.product.dto.response.ReviewResponse;
import com.vendo.product.dto.response.ReviewStatsResponse;
import com.vendo.product.entity.Product;
import com.vendo.product.entity.ProductReview;
import com.vendo.product.repository.ProductRepository;
import com.vendo.product.repository.ProductReviewRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductReviewService {
    
    private final ProductReviewRepository productReviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    
    /**
     * Add a new review for a product.
     * Validates that the user has purchased the product if verified purchase is required.
     * 
     * @param userId User ID of the reviewer
     * @param request Review request with product ID, rating, and review text
     * @return ReviewResponse with review details
     */
    @Transactional
    public ReviewResponse addReview(Long userId, ReviewRequest request) {
        log.info("Adding review for product {} by user {}", request.getProductId(), userId);
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));
        
        // Check if user already reviewed this product
        productReviewRepository.findByProductIdAndUserId(request.getProductId(), userId)
                .ifPresent(review -> {
                    throw new BadRequestException("You have already reviewed this product");
                });
        
        // Verify purchase if orderId is provided or if verified purchase is required
        Boolean isVerifiedPurchase = false;
        Order order = null;
        
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));
            
            // Verify order belongs to user
            if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
                throw new BadRequestException("Order does not belong to the user");
            }
            
            // Verify product is in the order
            OrderItem orderItem = orderItemRepository.findByOrderIdAndProductId(request.getOrderId(), request.getProductId())
                    .orElseThrow(() -> new BadRequestException("Product is not in the specified order"));
            
            // Only mark as verified if order is in a completed state
            if (order.getStatus() != null && 
                (order.getStatus().equals("CONFIRMED") || 
                 order.getStatus().equals("PROCESSING") || 
                 order.getStatus().equals("SHIPPED") || 
                 order.getStatus().equals("DELIVERED"))) {
                isVerifiedPurchase = true;
            }
        } else {
            // Check if user has purchased this product in any order
            List<OrderItem> purchasedItems = orderItemRepository.findPurchasedItemsByUserAndProduct(userId, request.getProductId());
            if (!purchasedItems.isEmpty()) {
                isVerifiedPurchase = true;
                // Use the most recent order
                order = purchasedItems.get(0).getOrder();
            }
        }
        
        // Require verified purchase for reviews
        if (!isVerifiedPurchase) {
            throw new BadRequestException("You must purchase this product before leaving a review");
        }
        
        // Create review
        ProductReview review = ProductReview.builder()
                .product(product)
                .user(user)
                .order(order)
                .rating(request.getRating())
                .title(request.getTitle())
                .reviewText(request.getReviewText())
                .isVerifiedPurchase(isVerifiedPurchase)
                .isApproved(false) // Reviews need admin approval by default
                .helpfulCount(0)
                .build();
        
        review = productReviewRepository.save(review);
        log.info("Review created with ID: {}", review.getId());
        
        return mapToResponse(review);
    }
    
    /**
     * Update an existing review.
     * Validates review ownership.
     * 
     * @param userId User ID of the reviewer
     * @param reviewId Review ID to update
     * @param request Updated review data
     * @return ReviewResponse with updated review details
     */
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewRequest request) {
        log.info("Updating review {} by user {}", reviewId, userId);
        
        // Find review
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        
        // Validate review ownership
        if (review.getUser() == null || !review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own reviews");
        }
        
        // Update review fields
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getReviewText() != null) {
            review.setReviewText(request.getReviewText());
        }
        
        // Reset approval status when review is updated
        review.setIsApproved(false);
        
        review = productReviewRepository.save(review);
        log.info("Review {} updated successfully", reviewId);
        
        return mapToResponse(review);
    }
    
    /**
     * Delete a review.
     * Validates review ownership.
     * 
     * @param userId User ID of the reviewer
     * @param reviewId Review ID to delete
     */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        log.info("Deleting review {} by user {}", reviewId, userId);
        
        // Find review
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        
        // Validate review ownership
        if (review.getUser() == null || !review.getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own reviews");
        }
        
        productReviewRepository.delete(review);
        log.info("Review {} deleted successfully", reviewId);
    }
    
    /**
     * Get all reviews for a product with pagination.
     * 
     * @param productId Product ID
     * @param includeUnapproved Whether to include unapproved reviews (admin only)
     * @param pageable Pagination parameters
     * @return Page of ReviewResponse
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getProductReviews(Long productId, Boolean includeUnapproved, Pageable pageable) {
        log.info("Fetching reviews for product {}", productId);
        
        // Validate product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        Page<ProductReview> reviews;
        if (Boolean.TRUE.equals(includeUnapproved)) {
            reviews = productReviewRepository.findByProductIdAndIsApproved(productId, null, pageable);
        } else {
            reviews = productReviewRepository.findByProductIdAndIsApproved(productId, true, pageable);
        }
        
        return reviews.map(this::mapToResponse);
    }
    
    /**
     * Get review statistics for a product.
     * Calculates average rating, total reviews, and rating distribution.
     * 
     * @param productId Product ID
     * @return ReviewStatsResponse with statistics
     */
    @Transactional(readOnly = true)
    public ReviewStatsResponse getReviewStats(Long productId) {
        log.info("Fetching review stats for product {}", productId);
        
        // Validate product exists
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        
        // Calculate average rating
        Double averageRating = productReviewRepository.calculateAverageRating(productId);
        
        // Get total review count
        Long totalReviews = productReviewRepository.countByProductId(productId);
        
        // Get all approved reviews for rating distribution
        List<ProductReview> reviews = productReviewRepository.findByProductIdAndIsApproved(productId, true);
        
        // Calculate rating distribution
        Map<Integer, Long> ratingDistribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            ratingDistribution.put(i, 0L);
        }
        
        long verifiedCount = 0;
        for (ProductReview review : reviews) {
            int rating = review.getRating();
            ratingDistribution.put(rating, ratingDistribution.get(rating) + 1);
            if (Boolean.TRUE.equals(review.getIsVerifiedPurchase())) {
                verifiedCount++;
            }
        }
        
        return ReviewStatsResponse.builder()
                .productId(productId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .verifiedReviews(verifiedCount)
                .ratingDistribution(ratingDistribution)
                .build();
    }
    
    /**
     * Map ProductReview entity to ReviewResponse DTO.
     * 
     * @param review ProductReview entity
     * @return ReviewResponse DTO
     */
    private ReviewResponse mapToResponse(ProductReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .productName(review.getProduct() != null ? review.getProduct().getName() : null)
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? 
                        (review.getUser().getFirstName() != null && review.getUser().getLastName() != null ?
                         review.getUser().getFirstName() + " " + review.getUser().getLastName() :
                         review.getUser().getUsername()) : null)
                .userEmail(review.getUser() != null ? review.getUser().getEmail() : null)
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .rating(review.getRating())
                .title(review.getTitle())
                .reviewText(review.getReviewText())
                .isVerifiedPurchase(review.getIsVerifiedPurchase())
                .isApproved(review.getIsApproved())
                .helpfulCount(review.getHelpfulCount())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
