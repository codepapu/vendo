package com.vendo.product.controller;

import com.vendo.common.dto.ApiResponse;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.product.dto.request.B2BPricingTierRequest;
import com.vendo.product.dto.response.B2BPricingTierResponse;
import com.vendo.product.entity.B2BPricingTier;
import com.vendo.product.service.B2BPricingTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "B2B Pricing Tiers", description = "B2B pricing tier management endpoints for configuring product pricing by business type and quantity")
@SecurityRequirement(name = "bearer-jwt")
public class B2BPricingTierController {
    
    private final B2BPricingTierService b2BPricingTierService;
    
    /**
     * Create a new B2B pricing tier
     * Only accessible by ADMIN
     */
    @Operation(
            summary = "Create a new B2B pricing tier",
            description = "Creates a new B2B pricing tier for a product with specific pricing based on business type and quantity range. " +
                    "Only accessible by ADMIN role.",
            tags = {"B2B Pricing Tiers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "B2B pricing tier created successfully",
                    content = @Content(schema = @Schema(implementation = B2BPricingTierResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<B2BPricingTierResponse>> createPricingTier(
            @Parameter(description = "B2B pricing tier details", required = true)
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
    @Operation(
            summary = "Update B2B pricing tier",
            description = "Updates an existing B2B pricing tier. Only accessible by ADMIN role.",
            tags = {"B2B Pricing Tiers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "B2B pricing tier updated successfully",
                    content = @Content(schema = @Schema(implementation = B2BPricingTierResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pricing tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<B2BPricingTierResponse>> updatePricingTier(
            @Parameter(description = "Pricing tier ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "B2B pricing tier update request", required = true)
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
    @Operation(
            summary = "Delete B2B pricing tier",
            description = "Deletes a B2B pricing tier. Only accessible by ADMIN role.",
            tags = {"B2B Pricing Tiers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "B2B pricing tier deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pricing tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePricingTier(
            @Parameter(description = "Pricing tier ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("Deleting B2B pricing tier: {}", id);
        
        b2BPricingTierService.deletePricingTier(id);
        return ResponseEntity.ok(ApiResponse.success("B2B pricing tier deleted successfully"));
    }
    
    /**
     * Get a B2B pricing tier by ID
     * Only accessible by ADMIN
     */
    @Operation(
            summary = "Get B2B pricing tier by ID",
            description = "Retrieves a B2B pricing tier by its ID. Only accessible by ADMIN role.",
            tags = {"B2B Pricing Tiers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pricing tier retrieved successfully",
                    content = @Content(schema = @Schema(implementation = B2BPricingTierResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Pricing tier not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<B2BPricingTierResponse>> getPricingTier(
            @Parameter(description = "Pricing tier ID", required = true, example = "1")
            @PathVariable Long id) {
        B2BPricingTier tier = b2BPricingTierService.getPricingTier(id);
        B2BPricingTierResponse response = mapToResponse(tier);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Get all pricing tiers for a product
     * Only accessible by ADMIN
     */
    @Operation(
            summary = "Get pricing tiers by product",
            description = "Retrieves all B2B pricing tiers for a specific product. Only accessible by ADMIN role.",
            tags = {"B2B Pricing Tiers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pricing tiers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Product not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<B2BPricingTierResponse>>> getPricingTiersByProduct(
            @Parameter(description = "Product ID", required = true, example = "1")
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
    @Operation(
            summary = "Get pricing tiers by business type",
            description = "Retrieves all B2B pricing tiers for a specific business type (CORPORATE, SME, ENTERPRISE). " +
                    "Only accessible by ADMIN role.",
            tags = {"B2B Pricing Tiers"}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Pricing tiers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid business type"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    @GetMapping("/business-type/{businessType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<B2BPricingTierResponse>>> getPricingTiersByBusinessType(
            @Parameter(description = "Business type (CORPORATE, SME, ENTERPRISE)", required = true, example = "CORPORATE")
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

