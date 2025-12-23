package com.vendo.product.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.RetailerReviewRequest;
import com.vendo.product.dto.response.RetailerReviewResponse;
import com.vendo.product.entity.RetailerReview;
import com.vendo.product.repository.RetailerReviewRepository;
import com.vendo.retailer.entity.Retailer;
import com.vendo.retailer.repository.RetailerRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetailerReviewService {
    
    private final RetailerReviewRepository retailerReviewRepository;
    private final RetailerRepository retailerRepository;
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<RetailerReviewResponse> getReviewsByRetailer(Long retailerId, Boolean isApproved, Pageable pageable) {
        Page<RetailerReview> reviews;
        if (isApproved != null) {
            reviews = retailerReviewRepository.findByRetailerIdAndIsApproved(retailerId, isApproved, pageable);
        } else {
            reviews = retailerReviewRepository.findByRetailerId(retailerId, pageable);
        }
        return reviews.map(this::mapToResponse);
    }
    
    @Transactional(readOnly = true)
    public List<RetailerReviewResponse> getApprovedReviewsByRetailer(Long retailerId) {
        List<RetailerReview> reviews = retailerReviewRepository.findByRetailerIdAndIsApprovedTrue(retailerId);
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RetailerReviewResponse getReviewById(Long id) {
        RetailerReview review = retailerReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RetailerReview", "id", id));
        return mapToResponse(review);
    }
    
    @Transactional(readOnly = true)
    public Double getAverageRating(Long retailerId) {
        Double average = retailerReviewRepository.getAverageRatingByRetailerId(retailerId);
        return average != null ? average : 0.0;
    }
    
    @Transactional(readOnly = true)
    public Long getReviewCount(Long retailerId) {
        return retailerReviewRepository.countApprovedReviewsByRetailerId(retailerId);
    }
    
    @Transactional
    public RetailerReviewResponse createReview(Long userId, RetailerReviewRequest request) {
        // Validate retailer exists
        Retailer retailer = retailerRepository.findById(request.getRetailerId())
                .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", request.getRetailerId()));
        
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if user already reviewed this retailer for the same order
        if (request.getOrderId() != null) {
            if (retailerReviewRepository.findByRetailerIdAndUserIdAndOrderId(
                    request.getRetailerId(), userId, request.getOrderId()).isPresent()) {
                throw new BadRequestException("Review already exists for this order");
            }
        }
        
        RetailerReview review = RetailerReview.builder()
                .retailer(retailer)
                .user(user)
                .rating(request.getRating())
                .reviewText(request.getReviewText())
                .isApproved(false) // Reviews need approval
                .build();
        
        // Set order if provided
        if (request.getOrderId() != null) {
            com.vendo.order.entity.Order order = new com.vendo.order.entity.Order();
            order.setId(request.getOrderId());
            review.setOrder(order);
        }
        
        RetailerReview savedReview = retailerReviewRepository.save(review);
        log.info("Retailer review created with ID: {}", savedReview.getId());
        
        return mapToResponse(savedReview);
    }
    
    @Transactional
    public RetailerReviewResponse approveReview(Long id) {
        RetailerReview review = retailerReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RetailerReview", "id", id));
        
        review.setIsApproved(true);
        RetailerReview updatedReview = retailerReviewRepository.save(review);
        
        // Update retailer rating
        updateRetailerRating(review.getRetailer().getId());
        
        log.info("Retailer review approved with ID: {}", updatedReview.getId());
        
        return mapToResponse(updatedReview);
    }
    
    @Transactional
    public RetailerReviewResponse rejectReview(Long id) {
        RetailerReview review = retailerReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RetailerReview", "id", id));
        
        retailerReviewRepository.delete(review);
        log.info("Retailer review rejected and deleted with ID: {}", id);
        
        // Update retailer rating if review was approved
        if (review.getIsApproved()) {
            updateRetailerRating(review.getRetailer().getId());
        }
        
        return mapToResponse(review);
    }
    
    @Transactional
    public void deleteReview(Long id) {
        RetailerReview review = retailerReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RetailerReview", "id", id));
        
        Long retailerId = review.getRetailer().getId();
        boolean wasApproved = review.getIsApproved();
        
        retailerReviewRepository.delete(review);
        
        // Update retailer rating if review was approved
        if (wasApproved) {
            updateRetailerRating(retailerId);
        }
        
        log.info("Retailer review deleted with ID: {}", id);
    }
    
    private void updateRetailerRating(Long retailerId) {
        Double averageRating = retailerReviewRepository.getAverageRatingByRetailerId(retailerId);
        if (averageRating != null) {
            Retailer retailer = retailerRepository.findById(retailerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Retailer", "id", retailerId));
            retailer.setRating(java.math.BigDecimal.valueOf(averageRating));
            retailerRepository.save(retailer);
        }
    }
    
    private RetailerReviewResponse mapToResponse(RetailerReview review) {
        RetailerReviewResponse.RetailerReviewResponseBuilder builder = RetailerReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt());
        
        if (review.getRetailer() != null) {
            builder.retailerId(review.getRetailer().getId());
            Retailer retailer = review.getRetailer();
            builder.retailer(RetailerReviewResponse.RetailerInfo.builder()
                    .id(retailer.getId())
                    .storeName(retailer.getStoreName())
                    .storeSlug(retailer.getStoreSlug())
                    .build());
        }
        
        if (review.getUser() != null) {
            builder.userId(review.getUser().getId());
            User user = review.getUser();
            builder.user(RetailerReviewResponse.UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build());
        }
        
        if (review.getOrder() != null) {
            builder.orderId(review.getOrder().getId());
        }
        
        return builder.build();
    }
}

