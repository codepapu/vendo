package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.request.RetailerReviewRequest;
import com.vendo.product.dto.response.RetailerReviewResponse;
import com.vendo.product.service.RetailerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retailer-reviews")
@RequiredArgsConstructor
public class RetailerReviewController {
    
    private final RetailerReviewService retailerReviewService;
    
    @GetMapping("/retailer/{retailerId}")
    public ResponseEntity<ApiResponse<Page<RetailerReviewResponse>>> getReviewsByRetailer(
            @PathVariable Long retailerId,
            @RequestParam(required = false) Boolean isApproved,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RetailerReviewResponse> reviews = retailerReviewService.getReviewsByRetailer(retailerId, isApproved, pageable);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
    
    @GetMapping("/retailer/{retailerId}/approved")
    public ResponseEntity<ApiResponse<List<RetailerReviewResponse>>> getApprovedReviews(
            @PathVariable Long retailerId) {
        List<RetailerReviewResponse> reviews = retailerReviewService.getApprovedReviewsByRetailer(retailerId);
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
    
    @GetMapping("/retailer/{retailerId}/rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(@PathVariable Long retailerId) {
        Double rating = retailerReviewService.getAverageRating(retailerId);
        return ResponseEntity.ok(ApiResponse.success(rating));
    }
    
    @GetMapping("/retailer/{retailerId}/count")
    public ResponseEntity<ApiResponse<Long>> getReviewCount(@PathVariable Long retailerId) {
        Long count = retailerReviewService.getReviewCount(retailerId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RetailerReviewResponse>> getReviewById(@PathVariable Long id) {
        RetailerReviewResponse review = retailerReviewService.getReviewById(id);
        return ResponseEntity.ok(ApiResponse.success(review));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<RetailerReviewResponse>> createReview(
            @Valid @RequestBody RetailerReviewRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        RetailerReviewResponse review = retailerReviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", review));
    }
    
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RetailerReviewResponse>> approveReview(@PathVariable Long id) {
        RetailerReviewResponse review = retailerReviewService.approveReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review approved successfully", review));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable Long id) {
        retailerReviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully"));
    }
}

