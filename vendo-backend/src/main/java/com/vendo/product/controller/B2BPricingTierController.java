package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.B2BPricingTierRequest;
import com.vendo.product.dto.response.B2BPricingTierResponse;
import com.vendo.product.entity.B2BPricingTier;
import com.vendo.product.service.B2BPricingTierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing B2B Pricing Tiers
 * Only accessible by ADMIN role
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products/pricing-tiers")
@RequiredArgsConstructor
public class B2BPricingTierController {
    
    private final B2BPricingTierService b2BPricingTierService;
    
    /**
     * Create a new B2B pricing tier
     * Only accessible by ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<B2BPricingTierResponse>> createPricingTier(
            @Valid @RequestBody B2BPricingTierRequest request) {
        log.info("Creating B2B pricing tier for product: {}", request.getProductId());
        
        B2BPricingTier tier = b2BPricingTierService.createPricingTier(
                request.getProductId(),
                request.getBusinessType(),
                request.getMinQuantity(),
                request.getMaxQuantity(),
                request.getPrice(),
                request.getDiscountPercentage()
        );
        
        B2BPricingTierResponse response = mapToResponse(tier);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("B2B pricing tier created successfully", response));
    }
    
    /**
     * Update an existing B2B pricing tier
     * Only accessible by ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<B2BPricingTierResponse>> updatePricingTier(
            @PathVariable Long id,
            @Valid @RequestBody B2BPricingTierRequest request) {
        log.info("Updating B2B pricing tier: {}", id);
        
        B2BPricingTier tier = b2BPricingTierService.updatePricingTier(
                id,
                request.getBusinessType(),
                request.getMinQuantity(),
                request.getMaxQuantity(),
                request.getPrice(),
                request.getDiscountPercentage(),
                request.getIsActive()
        );
        
        B2BPricingTierResponse response = mapToResponse(tier);
        return ResponseEntity.ok(ApiResponse.success("B2B pricing tier updated successfully", response));
    }
    
    /**
     * Delete a B2B pricing tier
     * Only accessible by ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePricingTier(@PathVariable Long id) {
        log.info("Deleting B2B pricing tier: {}", id);
        
        b2BPricingTierService.deletePricingTier(id);
        return ResponseEntity.ok(ApiResponse.success("B2B pricing tier deleted successfully"));
    }
    
    /**
     * Get a B2B pricing tier by ID
     * Only accessible by ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<B2BPricingTierResponse>> getPricingTier(@PathVariable Long id) {
        B2BPricingTier tier = b2BPricingTierService.getPricingTier(id);
        B2BPricingTierResponse response = mapToResponse(tier);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all pricing tiers for a product
     * Only accessible by ADMIN
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<B2BPricingTierResponse>>> getPricingTiersByProduct(
            @PathVariable Long productId) {
        List<B2BPricingTier> tiers = b2BPricingTierService.getPricingTiersByProduct(productId);
        List<B2BPricingTierResponse> responses = tiers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    /**
     * Get all pricing tiers for a business type
     * Only accessible by ADMIN
     */
    @GetMapping("/business-type/{businessType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<B2BPricingTierResponse>>> getPricingTiersByBusinessType(
            @PathVariable String businessType) {
        List<B2BPricingTier> tiers = b2BPricingTierService.getPricingTiersByBusinessType(businessType);
        List<B2BPricingTierResponse> responses = tiers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    /**
     * Exception handler for BadRequestException
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        log.error("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * Exception handler for ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }
    
    /**
     * Map B2BPricingTier entity to B2BPricingTierResponse
     */
    private B2BPricingTierResponse mapToResponse(B2BPricingTier tier) {
        return B2BPricingTierResponse.builder()
                .id(tier.getId())
                .productId(tier.getProduct() != null ? tier.getProduct().getId() : null)
                .productName(tier.getProduct() != null ? tier.getProduct().getName() : null)
                .businessType(tier.getBusinessType())
                .minQuantity(tier.getMinQuantity())
                .maxQuantity(tier.getMaxQuantity())
                .price(tier.getPrice())
                .discountPercentage(tier.getDiscountPercentage())
                .isActive(tier.getIsActive())
                .createdAt(tier.getCreatedAt())
                .build();
    }
}

