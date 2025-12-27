package com.vendo.retailer.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.product.dto.response.ProductRetailerResponse;
import com.vendo.retailer.dto.request.StorefrontUpdateRequest;
import com.vendo.retailer.dto.response.StorefrontResponse;
import com.vendo.retailer.service.RetailerStorefrontService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/storefronts")
@RequiredArgsConstructor
public class RetailerStorefrontController {
    
    private final RetailerStorefrontService storefrontService;
    
    /**
     * Get public storefront by slug
     */
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<StorefrontResponse>> getStorefront(@PathVariable String slug) {
        StorefrontResponse storefront = storefrontService.getStorefront(slug);
        return ResponseEntity.ok(ApiResponse.success(storefront));
    }
    
    /**
     * Get public storefront by retailer ID
     */
    @GetMapping("/retailer/{retailerId}")
    public ResponseEntity<ApiResponse<StorefrontResponse>> getStorefrontById(@PathVariable Long retailerId) {
        StorefrontResponse storefront = storefrontService.getStorefrontById(retailerId);
        return ResponseEntity.ok(ApiResponse.success(storefront));
    }
    
    /**
     * Update storefront (retailer owners only)
     */
    @PutMapping("/{retailerId}")
    @PreAuthorize("hasRole('RETAILER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<StorefrontResponse>> updateStorefront(
            @PathVariable Long retailerId,
            @Valid @RequestBody StorefrontUpdateRequest request,
            Authentication authentication) {
        // Validate ownership for RETAILER role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_RETAILER"))) {
            Long userId = Long.parseLong(authentication.getName());
            // Validate ownership through service
            storefrontService.validateRetailerOwnership(retailerId, userId);
        }
        StorefrontResponse storefront = storefrontService.updateStorefront(retailerId, request);
        return ResponseEntity.ok(ApiResponse.success("Storefront updated successfully", storefront));
    }
    
    /**
     * Get products for a storefront
     */
    @GetMapping("/{slug}/products")
    public ResponseEntity<ApiResponse<Page<ProductRetailerResponse>>> getStorefrontProducts(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<ProductRetailerResponse> products = storefrontService.getStorefrontProducts(slug, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }
    
    /**
     * Search retailers
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<StorefrontResponse>>> searchRetailers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "storeName") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        Page<StorefrontResponse> retailers = storefrontService.searchRetailers(q, pageable);
        return ResponseEntity.ok(ApiResponse.success(retailers));
    }
}

