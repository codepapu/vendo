package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.response.WishlistResponse;
import com.vendo.product.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing wishlist items.
 * Provides endpoints to add, remove, retrieve, and clear wishlist items.
 */
@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    
    private final WishlistService wishlistService;
    
    /**
     * Add a product to the user's wishlist.
     * If the product is already in the wishlist, returns the existing item.
     * 
     * @param productId Product ID to add
     * @param authentication Authentication object containing user information
     * @return WishlistResponse with product details
     */
    @PostMapping("/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        WishlistResponse response = wishlistService.addToWishlist(customerId, productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product added to wishlist", response));
    }
    
    /**
     * Remove a product from the user's wishlist.
     * 
     * @param productId Product ID to remove
     * @param authentication Authentication object containing user information
     * @return Success message
     */
    @DeleteMapping("/products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        wishlistService.removeFromWishlist(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Product removed from wishlist"));
    }
    
    /**
     * Get all wishlist items for the authenticated user.
     * Returns list of WishlistResponse with product details.
     * 
     * @param authentication Authentication object containing user information
     * @return List of WishlistResponse with product details
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<WishlistResponse>>> getWishlist(
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        List<WishlistResponse> wishlist = wishlistService.getWishlist(customerId);
        return ResponseEntity.ok(ApiResponse.success(wishlist));
    }
    
    /**
     * Clear all wishlist items for the authenticated user.
     * 
     * @param authentication Authentication object containing user information
     * @return Success message
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> clearWishlist(
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        wishlistService.clearWishlist(customerId);
        return ResponseEntity.ok(ApiResponse.success("Wishlist cleared"));
    }
    
    /**
     * Check if a product is in the user's wishlist.
     * 
     * @param productId Product ID to check
     * @param authentication Authentication object containing user information
     * @return Boolean indicating if product is in wishlist
     */
    @GetMapping("/products/{productId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> isInWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        Long customerId = Long.parseLong(authentication.getName());
        boolean isInWishlist = wishlistService.isInWishlist(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success(isInWishlist));
    }
}
