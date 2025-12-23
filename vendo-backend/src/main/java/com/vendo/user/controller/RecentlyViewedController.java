package com.vendo.user.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.user.dto.response.RecentlyViewedResponse;
import com.vendo.user.service.RecentlyViewedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing recently viewed products.
 * Provides endpoints to add, retrieve, and clear recently viewed products.
 */
@RestController
@RequestMapping("/api/v1/users/recently-viewed")
@RequiredArgsConstructor
public class RecentlyViewedController {
    
    private final RecentlyViewedService recentlyViewedService;
    
    /**
     * Add a product to the user's recently viewed list.
     * 
     * @param productId Product ID to add
     * @param authentication Authentication object containing user information
     * @return RecentlyViewedResponse with product details and view date
     */
    @PostMapping("/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RecentlyViewedResponse>> addToRecentlyViewed(
            @PathVariable Long productId,
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        RecentlyViewedResponse response = recentlyViewedService.addToRecentlyViewed(customerId, productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added to recently viewed", response));
    }
    
    /**
     * Get the user's recently viewed products.
     * Returns up to 20 products, ordered by most recent first.
     * 
     * @param authentication Authentication object containing user information
     * @return List of RecentlyViewedResponse with product details and view dates
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RecentlyViewedResponse>>> getRecentlyViewed(
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        List<RecentlyViewedResponse> recentlyViewed = recentlyViewedService.getRecentlyViewed(customerId);
        return ResponseEntity.ok(ApiResponse.success(recentlyViewed));
    }
    
    /**
     * Clear all recently viewed products for the authenticated user.
     * 
     * @param authentication Authentication object containing user information
     * @return Success response
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> clearRecentlyViewed(Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        recentlyViewedService.clearRecentlyViewed(customerId);
        return ResponseEntity.ok(ApiResponse.success("Recently viewed products cleared successfully", null));
    }
}
